(ns karabiner-configurator.core
  (:require
   [schema.core :as s]
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

(defn karabiner-json-path
  "Return karabiner.json file location"
  []
  (if (System/getenv "XDG_CONFIG_HOME")
    (str (System/getenv "XDG_CONFIG_HOME") "karabiner/karabiner.json")
    (str (System/getenv "HOME") "/.config/karabiner/karabiner.json")))

(defn config-file
  "Return karabiner.edn file location"
  []
  (if (System/getenv "XDG_CONFIG_HOME")
    (str (System/getenv "XDG_CONFIG_HOME") "karabiner.edn")
    (str (System/getenv "HOME") "/.config/karabiner.edn")))

(defn log-file []
  (str (System/getenv "HOME") "/Library/Logs/goku.log"))

(defn check-edn-syntax
  "Call joker to check syntax of karabiner.edn"
  [path]
  (shell/sh "/usr/local/opt/joker/bin/joker"  "--lint" path))

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

(defn update-karabiner-config
  [config]
  (def karabiner-config config)
  karabiner-config)

(defn update-to-karabiner-json
  "Find profile 'goku' in karabiner.json and update its rules"
  [profiles]
  (let [local-karabiner-config (update-karabiner-config (load-json (karabiner-json-path)))
        profile-indexed-list (map-indexed (fn [idx itm] [idx itm]) (:profiles local-karabiner-config))]
    (doseq [a profiles]
      (doseq [[profile-name profile-complex-modifications] a]
        (let [profile-name-str (name profile-name)
              profile-to-update
              (first
               (for [[index {:keys [name] :as x}] profile-indexed-list
                     :when (= name profile-name-str)]
                 {:index index :profile x}))
              updated-complex-modifications profile-complex-modifications
              validate-right-profile (massert (nn? profile-to-update) (format "Can't find profile named \"%s\" in karabiner.json, please create a profile named \"%s\" using the Karabiner-Elements.app." profile-name-str profile-name-str))
              updated-profile (:profile (assoc-in profile-to-update [:profile :complex_modifications] (:complex_modifications updated-complex-modifications)))
              updated-profiles (assoc-in (:profiles karabiner-config ) [(:index profile-to-update) :complex_modifications] (:complex_modifications updated-profile))
              updated-configs (update-karabiner-config (assoc karabiner-config :profiles updated-profiles))])))
    (spit (karabiner-json-path)
          (json/generate-string karabiner-config {:pretty true}))))

;; actions
(defn parse
  "Root function to parse karabiner.edn and update karabiner.json."
  [path]
  (let [edn-syntax-err (:err (check-edn-syntax path))]
    (if (> (count edn-syntax-err) 0)
      (do (println "Syntax error in config:")
          (println edn-syntax-err)
          (if (not (env :is-dev)) (System/exit 1)))))
  ;; (parse-edn (load-edn path)))
  (update-to-karabiner-json (parse-edn (load-edn path))))

(defn open-log-file []
  (shell/sh "open" (log-file)))

;; cli stuff
(def cli-opts
  [["-h" "--help"]
   ["-l" "--log"]])

(defn help-message [options-summary]
  (->> ["GokuRakuJoudo -- karabiner configurator"
        ""
        "goku will read config file and update `Goku` profile in karabiner.json"
        (str "- goku config file location: " (config-file))
        (str "- karabiner config file:  " (karabiner-json-path))
        ""
        "run without arg to update once, run with `-w` to update on .edn file change"
        ""
        "Usage: run goku without arg to process config once"
        ""
        "-l, --log, log  to open the log file"
        "-h, --help, help  to show this message"]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n"
       (string/join \newline errors)))

(defn validate-args [args]
  (let [{:keys [options arguments summary errors]} (cli/parse-opts args cli-opts)]
    (cond
      (or (:help options) (= "help" (first arguments)))
      {:action "help"
       :ok true
       :exit-message (help-message summary) :ok? true}
      errors
      {:action "errors"
       :ok? false
       :exit-message (error-msg errors)}
      (or (:log options) (= "log" (first arguments)))
      {:action "log"
       :ok? true
       :exit-message "open log file"}
      (= (count arguments) 0)
      {:action "run"
       :ok? true
       :exit-message "Done!"}
      :else
      {:action "default"
       :ok? true
       :exit-message (help-message summary)})))

(defn exit [status & [msg]]
  (if msg (println msg))
  (if (not (env :is-dev)) (System/exit status)))

;; main
(defn -main
  [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (case action
        "run"  (do (parse (config-file))
                   (exit (if ok? 0 1) exit-message))
        "log" (do (open-log-file)
                  (exit 0))
        "help" (exit (if ok? 0 1) exit-message)
        "errors" (exit (if ok? 0 1) exit-message)
        "default" (exit (if ok? 0 1) exit-message)))))

;; develop
;; (-main)
;; (-main "-h")
;; (-main "--help")
;; (-main "help")
;; (-main "-l")
;; (-main "--log")
;; (-main "log")
