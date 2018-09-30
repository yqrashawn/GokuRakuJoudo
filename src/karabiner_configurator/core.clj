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
   [clojure.edn :as edn]
   [me.raynes.fs :as fs]
   [clojure.tools.cli :as cli])
  (:gen-class))

(defn update-static-conf
  "update conf-data from reading rules"
  [key conf]
  (if (nn? conf)
    (update-conf-data (assoc conf-data key conf))))

(defn generate
  "generate configuration"
  [conf]
  (let [{:keys [applications devices keyboard-type input-sources tos froms modifiers layers simlayers raws main simlayer-threshold templates]} conf]
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
    (layers/parse-layers layers)
    (froms/parse-froms froms)
    (tos/parse-tos tos)
    (rules/parse-mains main)))

(defn parse [conf]
  ;; (spit "/Users/rashawnzhang/workspace/HOME/karabiner-configurator/src/karabiner_configurator/rules.json"
  ;;       (json/generate-string (generate conf) {:pretty true}))
  ;; (json/generate-string (generate conf) {:pretty true})
  (init-conf-data)
  (generate conf))

(defn karabiner-json-path []
  (if (System/getenv "XDG_CONFIG_HOME")
    (str (System/getenv "XDG_CONFIG_HOME") "karabiner/karabiner.json")
    (str (System/getenv "HOME") "/.config/karabiner/karabiner.json")))

(defn config-file []
  (if (System/getenv "XDG_CONFIG_HOME")
    (str (System/getenv "XDG_CONFIG_HOME") "karabiner.edn")
    (str (System/getenv "HOME") "/.config/karabiner.edn")))

;; (println (str "$XDG_CONFIG_HOME: " (System/getenv "XDG_CONFIG_HOME")))
;; (println (str "$HOME: " (System/getenv "HOME")))
;; (println (str "karabiner json path: " (karabiner-json-path )))
;; (println (str "edn config file path: " (config-file )))

(defn update-to-karabiner-json [rules]
  (let [karabiner-config (load-json (karabiner-json-path))
        profile-indexed-list (map-indexed (fn [idx itm] [idx itm]) (:profiles karabiner-config))
        profile-to-update
        (first
         (for [[index {:keys [name] :as x}] profile-indexed-list
               :when (= name "Goku")]
           {:index index :profile x}))
        updated-rules rules
        updated-profile (:profile (assoc-in profile-to-update [:profile :complex_modifications :rules] updated-rules))
        updated-profiles (assoc (:profiles karabiner-config ) (:index profile-to-update) updated-profile)
        updated-configs (assoc karabiner-config :profiles updated-profiles)]
    (spit (karabiner-json-path)
          (json/generate-string updated-configs {:pretty true}))))

(defn parse-edn [path]
  (update-to-karabiner-json (parse (load-edn path)))
  (println "Done!"))

(defn watch []
  (println (str "watching " (config-file)))
  (shell/sh "watchexec" "-w" (config-file) "goku"))

;; cli things
(def cli-opts
  [["-w" "--watch" "keep watching config file, update karabiner.json when config change WIP"
    :parse-fn str]
   ["-d" "--deamon" "run watch in background WIP"
    :parse-fn str]
   ["-h" "--help"]])

(defn help-message [options-summary]
  (->> ["GokuRakuJoudo -- karabiner configurator"
        ""
        "goku will read config file and update `Goku` profile in karabiner.json"
        (str "- goku config file location: " (config-file))
        (str "- karabiner config file:  " (karabiner-json-path))
        ""
        "run without arg to update once, run with `-w` to update on .edn file change"
        ""
        "Usage: goku [options]"
        ""
        "Options:"
        options-summary]
       ;; ""
       ;; "Please refer to the manual page for more information."]
       (string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn validate-args [args]
  (let [{:keys [options arguments summary errors]} (cli/parse-opts args cli-opts)]
    (cond
      (:help options)
      {:action "help"
       :ok true
       :exit-message (help-message summary) :ok? true}
      errors
      {:action "errors"
       :exit-message (error-msg errors)}
      (= "true" (:watch options))
      {:action "watch"
       :ok true
       :exit-message "no exit"}
      (= (count arguments) 0)
      {:action "run"
       :ok? true
       :exit-message "finished!"}
      :else
      {:action "default"
       :ok? true
       :exit-message (help-message summary)})))

(defn exit [status & [msg]]
  (if msg (println msg))
  (System/exit status))

(defn -main
  [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (case action
        "run"  (do (parse-edn (config-file))
                   (exit (if ok? 0 1)))
        "watch" (watch)
        "help" (exit (if ok? 0 1) exit-message)
        "default" (exit (if ok? 0 1) exit-message)))))

;; (-main)
;; (-main "-h")
;; (-main "-w")
