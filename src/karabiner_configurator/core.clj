(ns karabiner-configurator.core
  (:require
   [schema.core :as s]
   [cheshire.core :as json]
   [clojure.string :as string]
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

(def config (load-edn "resources/configurations/test/keytokey.edn"))

(defn update-static-conf
  "update conf-data from reading rules"
  [key conf]
  (if (nn? conf)
    (update-conf-data (assoc conf-data key conf))))

(defn generate
  "generate configuration"
  [conf]
  (let [{:keys [applications devices keyboard-type input-source tos froms modifiers layers simlayers raws main simlayer-threshold]} conf]
    (update-static-conf :applications applications)
    (update-static-conf :devices devices)
    (update-static-conf :keyboard-type keyboard-type)
    (update-static-conf :input-source tos)
    (if (number? simlayer-threshold)
      (update-static-conf :simlayer-threshold simlayer-threshold))
    (modifiers/parse-modifiers modifiers)
    (layers/parse-layers layers)
    (layers/parse-simlayers simlayers)
    (froms/parse-froms froms)
    (tos/parse-tos tos)
    (rules/parse-mains main)))

(defn parse [conf]
  ;; (spit "/Users/rashawnzhang/workspace/HOME/karabiner-configurator/src/karabiner_configurator/rules.json"
  ;;       (json/generate-string (generate conf) {:pretty true}))
  ;; (json/generate-string (generate conf) {:pretty true})
  (init-conf-data)
  (generate conf))

(def karabiner-json-path (str (System/getenv "XDG_CONFIG_HOME") "karabiner/karabiner.json"))

(def config-file
  (if (System/getenv "XDG_CONFIG_HOME")
    (str (System/getenv "XDG_CONFIG_HOME") "karabiner.edn")
    (str (System/getenv "HOME") "/.config/karabiner.edn")))

(defn update-to-karabiner-json [rules]
  (let [karabiner-config (load-json (str (System/getenv "XDG_CONFIG_HOME") "karabiner/karabiner.json"))
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
    (spit karabiner-json-path
     (json/generate-string updated-configs {:pretty true}))))

(defn parse-edn [path]
  (update-to-karabiner-json (parse (load-edn path))))

(defn watch [])

(def cli-opts
  [["-w" "--watch" "keep watching config file, update karabiner.json when config change (not implemented)"
    :parse-fn str]
   ["-d" "--deamon" "run watch in background (not implemented)"
    :parse-fn str]
   ["-h" "--help"]])

(defn help-message [options-summary]
  (->> ["GokuRakuJoudo -- karabiner configurator"
        ""
        "goku will read config file and update `Goku` profile in karabiner.json"
        (str "- goku config file location: " config-file)
        (str "- karabiner config file:  " karabiner-json-path)
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
      {:exit-message (help-message summary) :ok? true}
      errors
      {:exit-message (error-msg errors)}
      (and (= (count arguments) (or (= (first arguments) "--watch") (= (first arguments) "-w"))))
      {:action "watch"}
      (= (count arguments) 0)
      {:action "run"}
      :else
      (:exit-message (help-message summary)))))

(defn exit [status msg]
  (println msg))
;; (System/exit status))

(defn -main
  [& args]
  (let [{:keys [action options exit-message ok?]} (validate-args args)]
    (if exit-message
      (exit (if ok? 0 1) exit-message)
      (case action
        "run"  (parse-edn config-file)
        "watch" (watch)))))

;; (-main)
;; (-main "-h")