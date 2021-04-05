(ns karabiner-configurator.core
  (:require
   [cheshire.core :as json]
   [clojure.string :as string]
   [clojure.java.shell :as shell]
   [karabiner-configurator.modifiers :as modifiers]
   [karabiner-configurator.misc :refer :all]
   [karabiner-configurator.data :refer :all]
   [karabiner-configurator.layers :as layers]
   [karabiner-configurator.froms :as froms]
   [karabiner-configurator.tos :as tos]
   [karabiner-configurator.rules :as rules]
   [karabiner-configurator.profiles :as profiles]
   [clojure.edn :as edn]
   [me.raynes.fs :as fs]
   [clojure.tools.cli :as cli]
   [environ.core :refer [env]])
  (:gen-class))

;; helper function
(defn update-static-conf
  "Helper function to update conf-data from reading rules"
  [key conf]
  (if (nn? conf)
    (assoc-conf-data key conf)))

(defn check-edn-syntax
  "Call joker to check syntax of karabiner.edn"
  [path]
  (let [;; intel mac
        joker-bin1 "/usr/local/opt/joker/bin/joker"
        ;; arm mac
        joker-bin2 "/opt/homebrew/opt/joker/bin/joker"
        ;; fallback to brew --prefix joker, it's really slow
        joker-bin (cond (fs/exists? joker-bin1) joker-bin1
                        (fs/exists? joker-bin2) joker-bin2
                        :else (-> (shell/sh "brew" "--prefix" "joker")
                                  :out
                                  (string/trim-newline)
                                  (str "/bin/joker")))]
    (shell/sh joker-bin "--lint" path)))

(defn exit [status & [msg]]
  (if msg (println msg))
  (if (not (env :is-dev)) (System/exit status)))

;; paths
(defn json-config-file-path
  "Return karabiner.json file location"
  []
  (if (System/getenv "XDG_CONFIG_HOME")
    (str (System/getenv "XDG_CONFIG_HOME") "/karabiner/karabiner.json")
    (str (System/getenv "HOME") "/.config/karabiner/karabiner.json")))

(defn edn-config-file-path
  "Return karabiner.edn file location"
  []
  (cond
    (System/getenv "GOKU_EDN_CONFIG_FILE")
    (System/getenv "GOKU_EDN_CONFIG_FILE")
    (System/getenv "XDG_CONFIG_HOME")
    (str (System/getenv "XDG_CONFIG_HOME") "/karabiner.edn")
    :else
    (str (System/getenv "HOME") "/.config/karabiner.edn")))

(defn log-file []
  (str (System/getenv "HOME") "/Library/Logs/goku.log"))
;; main logic

(defn parse-edn
  "Init conf data and return new rules based on karabiner.edn (main section in edn file)"
  [conf]
  (init-conf-data)
  (let [{:keys [applications devices keyboard-type input-sources tos froms modifiers layers simlayers raws main simlayer-threshold templates profiles]} conf]
    (if (nil? profiles)
      (profiles/parse-profiles (:profiles conf-data))
      (profiles/parse-profiles profiles))
    (update-static-conf :applications applications)
    (update-static-conf :devices devices)
    (update-static-conf :keyboard-type keyboard-type)
    (update-static-conf :input-sources input-sources)
    (update-static-conf :templates templates)
    (if (number? simlayer-threshold)
      (update-static-conf :simlayer-threshold simlayer-threshold))
    (modifiers/parse-modifiers modifiers)
    (layers/parse-layers layers)
    (layers/parse-simlayers simlayers)
    (froms/parse-froms froms)
    (tos/parse-tos tos)
    (profiles/parse-rules (rules/parse-mains main))))

(defn update-to-karabiner-json
  "Update karabiner.json depend on parsed karabiner.edn.
  `customized-profiles` {:profile1 {,,,} :profile2 {,,,}}"
  [customized-profiles & [dry-run dry-run-all]]
  (let [karabiner-config (load-json (json-config-file-path))
        user-profiles    (into
                          {}
                          (for [json-profile
                                (:profiles karabiner-config)]
                            {(keyword (:name json-profile))
                             json-profile}))]
    (doseq [[profile-k profile-v] customized-profiles]
      (let [profile-name-str (name profile-k)]
        (massert
         (nn? (profile-k user-profiles))
         (format
          "Can't find profile named \"%s\" in karabiner.json, please create a profile named \"%s\" using the Karabiner-Elements.app."
          profile-name-str
          profile-name-str))))
    (when dry-run
      (doseq [[profile-k profile-v] user-profiles]
        (when-let [customized-profile (profile-k customized-profiles)]
          (println (json/generate-string
                    (assoc profile-v :complex_modifications (:complex_modifications customized-profile))
                    {:pretty true}))))
      (exit 1))
    (let [result-config (assoc
                         karabiner-config
                         :profiles
                         (mapv
                          (fn [[profile-k profile-v]]
                            (if-let [customized-profile (profile-k customized-profiles)]
                              (assoc profile-v :complex_modifications (:complex_modifications customized-profile))
                              profile-v))
                          user-profiles))]
      (when dry-run-all
        (println (json/generate-string
                  result-config
                  {:pretty true}))
        (exit 1))
      (spit
       (json-config-file-path)
       (json/generate-string
        result-config
        {:pretty true})))))

;; actions
(defn parse
  "Root function to parse karabiner.edn and update karabiner.json."
  [path & [dry-run dry-run-all]]
  (let [edn-syntax-err (:err (check-edn-syntax path))]
    (if (> (count edn-syntax-err) 0)
      (do (println "Syntax error in config:")
          (println edn-syntax-err)
          (exit 1))))
  (update-to-karabiner-json (parse-edn (load-edn path)) dry-run dry-run-all))

(defn open-log-file []
  (shell/sh "open" (log-file)))
;; cli stuff

(defn help-message [options-summary]
  (->> ["GokuRakuJoudo -- karabiner configurator"
        ""
        "goku will read config file and update `Goku` profile in karabiner.json"
        (str "- goku config file location: " (edn-config-file-path))
        (str "- karabiner config file location:  " (json-config-file-path))
        "- you can also specify edn file path with env GOKU_EDN_CONFIG_FILE"
        ""
        "Usage: run goku without arg to process config once"
        ""
        "-l, --log, to open the log file"
        "-d, --dry-run, to spit the new config of modified profiles into stdout instead of update karabiner.json"
        "-A, --dry-run-all, to spit the new whole config into stdout instead of update karabiner.json"
        "-c, --config PATH, to specify edn config file from command line"
        "-h, --help, to show this message"
        "-V, --version, to show current version of goku"]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n"
       (string/join \newline errors)))

(defn abs-path [path]
  (if (= (first path) \~)
    (.getPath (fs/expand-home path))
    path))

(def cli-opts
  [["-h" "--help"]
   ["-V" "--version"]
   ["-l" "--log"]
   ["-c" "--config PATH" "Config PATH"
    :parse-fn abs-path
    :validate [(fn [path]
                 (let [path (abs-path path)]
                   (and (fs/exists? path)
                        (fs/file? path)
                        (fs/readable? path)))) "Make sure the file is exits and readable"]]
   ["-d" "--dry-run"]
   ["-A" "--dry-run-all"]])

(defn validate-args [args]
  (let [{:keys [options arguments summary errors]} (cli/parse-opts args cli-opts)]
    (cond
      ;; error
      errors
      {:action       "errors"
       :ok?          false
       :exit-message (error-msg errors)}
      ;; help
      (:help options)
      {:action       "exit-with-message"
       :ok?          true
       :exit-message (help-message summary)}
      ;; version
      (:version options)
      {:action "exit-with-message"
       :ok? true
       :exit-message "0.3.9"}
      ;; log
      (:log options)
      {:action       "log"
       :ok?          true
       :exit-message "open log file"}
      ;; run
      (= (count arguments) 0)
      {:action       "run"
       :ok?          true
       :dry-run-all  (:dry-run-all options)
       :dry-run      (:dry-run options)
       :config       (:config options)
       :exit-message "Done!"}
      :else
      {:action       "default"
       :ok?          true
       :exit-message (help-message summary)})))

;; main
(defn -main
  [& args]
  (let [{:keys [action options exit-message ok? config dry-run dry-run-all]} (validate-args args)]
    (if exit-message
      (case action
        "run" (do (parse (or config (edn-config-file-path)) dry-run dry-run-all)
                  (exit (if ok? 0 1) exit-message))
        "log" (do (open-log-file)
                  (exit 0))
        "exit-with-message" (exit (if ok? 0 1) exit-message)
        "errors" (exit (if ok? 0 1) exit-message)
        "default" (exit (if ok? 0 1) exit-message)))))

(comment
  (-main)
  (-main "-h")
  (-main "--help")
  (-main "-l")
  (-main "--log")
  (-main "--config" "./")
  (-main "-c" "./")
  (-main "-dc" "./")
  (-main "-dc" "~/.config/karabiner.edn")
  (-main "-d")
  (-main "-V")
  (-main "--version"))