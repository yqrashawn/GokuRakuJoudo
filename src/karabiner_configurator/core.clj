(ns karabiner-configurator.core
  (:require
   [babashka.process :as p]
   [cheshire.core :as json]
   [clojure.string :as string]
   [clojure.tools.cli :as cli]
   [karabiner-configurator.data :as d]
   [karabiner-configurator.froms :as froms]
   [karabiner-configurator.layers :as layers]
   [karabiner-configurator.misc :refer [load-edn load-json massert]]
   [karabiner-configurator.modifiers :as modifiers]
   [karabiner-configurator.profiles :as profiles]
   [karabiner-configurator.rules :as rules]
   [karabiner-configurator.tos :as tos]
   [me.raynes.fs :as fs])
  (:gen-class))

;; helper function
(defn update-static-conf
  "Helper function to update conf-data from reading rules"
  [key conf]
  (when (some? conf)
    (d/assoc-conf-data key conf)))

(defn check-edn-syntax
  "Call joker to check syntax of karabiner.edn"
  [path]
  (-> @(p/process [(System/getenv "SHELL") "-i" "-c" (format "joker --lint %s" path)])
      :err))

(defn exit
  ([status] (exit status nil))
  ([status msg]
   (when msg (println msg))
   (when-not (= (System/getenv "GOKU_IS_DEV") "1") (System/exit status))))

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
    (fs/expand-home (System/getenv "GOKU_EDN_CONFIG_FILE"))
    (System/getenv "XDG_CONFIG_HOME")
    (fs/expand-home (str (System/getenv "XDG_CONFIG_HOME") "/karabiner.edn"))
    :else
    (str (System/getenv "HOME") "/.config/karabiner.edn")))

(defn log-file []
  (str (System/getenv "HOME") "/Library/Logs/goku.log"))
;; main logic

(defn parse-edn
  "Init conf data and return new rules based on karabiner.edn (main section in edn file)"
  [conf]
  (d/init-conf-data)
  (let [{:keys [applications devices keyboard-type input-sources tos froms modifiers layers simlayers ;; raws
                main simlayer-threshold templates profiles]} conf]
    (if (nil? profiles)
      (profiles/parse-profiles (:profiles @d/conf-data))
      (profiles/parse-profiles profiles))
    (update-static-conf :applications applications)
    (update-static-conf :devices devices)
    (update-static-conf :keyboard-type keyboard-type)
    (update-static-conf :input-sources input-sources)
    (update-static-conf :templates templates)
    (when (number? simlayer-threshold)
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
    (doseq [[profile-k _] customized-profiles]
      (let [profile-name-str (name profile-k)]
        (massert
         (some? (profile-k user-profiles))
         (format
          "Can't find profile named \"%s\" in karabiner.json, please create a profile named \"%s\" using the Karabiner-Elements.app."
          profile-name-str
          profile-name-str))))
    (if dry-run
      (do (doseq [[profile-k profile-v] user-profiles]
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
        (if dry-run-all
          (do (println (json/generate-string
                        result-config
                        {:pretty true}))
              (exit 1))
          (spit
           (json-config-file-path)
           (json/generate-string
            result-config
            {:pretty true})))))))

;; actions
(defn parse
  "Root function to parse karabiner.edn and update karabiner.json."
  [path & [dry-run dry-run-all]]
  (let [edn-syntax-err-stream (check-edn-syntax path)]
    (def edn-syntax-err (slurp edn-syntax-err-stream))
    (when (> (count edn-syntax-err) 0)
      (println "Syntax error in config:")
      (println edn-syntax-err)
      (exit 1)))
  (update-to-karabiner-json (parse-edn (load-edn path)) dry-run dry-run-all))

(defn open-log-file []
  @(p/process "open" (log-file)))

;; cli stuff
(defn help-message [_]
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
  (.getPath (fs/expand-home path)))

(def cli-opts
  [["-h" "--help"]
   ["-V" "--version"]
   ["-l" "--log"]
   [nil "--where-is-my-config"]
   ["-c" "--config PATH" "Config PATH"
    :parse-fn abs-path
    :validate [(fn [path]
                 (and (fs/exists? path)
                      (fs/file? path)
                      (fs/readable? path)))
               "Make sure the file is exits and readable"]]
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
      (:where-is-my-config options)
      {:action "show-config-path"
       :ok?    true}
      ;; version
      (:version options)
      {:action       "exit-with-message"
       :ok?          true
       :exit-message "0.5.7"}
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
  (let [{:keys [action ;; options
                exit-message ok? config dry-run dry-run-all]} (validate-args args)]
    (case action
      "run"               (do (parse (or config (edn-config-file-path)) dry-run dry-run-all)
                              (exit (if ok? 0 1) exit-message))
      "show-config-path"  (exit 0 (or config (edn-config-file-path)))
      "log"               (do (open-log-file) (exit 0))
      "exit-with-message" (exit (if ok? 0 1) exit-message)
      "errors"            (exit (if ok? 0 1) exit-message)
      "default"           (exit (if ok? 0 1) exit-message))))

(comment
  (-main)
  (-main "-h")
  (-main "--help")
  (-main "-l")
  (-main "--log")
  (-main "--config" "./")
  (-main "--where-is-my-config")
  (-main "-c" "./")
  (-main "-dc" "./")
  (-main "-dc" "~/.config/karabiner.edn")
  (-main "-dc" "~/.config/karabiner.test.edn")
  (-main "-c" "~/.nixpkgs/modules/yqrashawn/home-manager/dotfiles/karabiner.edn")
  (-main "-d")
  (-main "-V")
  (-main "--version"))
