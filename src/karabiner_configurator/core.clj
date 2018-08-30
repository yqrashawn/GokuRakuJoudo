(ns karabiner-configurator.core
  (:require
   [clojure.data.json :as json]
   [schema.core :as s]
   [karabiner-configurator.modifiers :refer :all]
   [karabiner-configurator.misc :refer :all]
   [karabiner-configurator.data :refer :all]
   [karabiner-configurator.layers :refer :all]
   [karabiner-configurator.froms :refer :all]
   [karabiner-configurator.tos :refer :all]
   [karabiner-configurator.rules :refer :all]
   [clojure.edn :as edn]))

(def config (load-edn "resources/configurations/test/keytokey.edn"))

(defn update-static-conf
  "update conf-data from reading rules"
  [key conf]
  (if (nn? conf)
    (update-conf-data (assoc conf-data key conf))))

(defn parse
  "parse configuration"
  [conf]
  (let [{:keys [applications devices keyboard-type input-source tos froms modifiers layers simlayers raws rules]} conf]
    (update-static-conf :applications applications)
    (update-static-conf :devices devices)
    (update-static-conf :keyboard-type keyboard-type)
    (update-static-conf :input-source tos)
    (parse-modifiers modifiers)
    (parse-layers layers)
    (parse-simlayers simlayers)
    (parse-froms froms)
    (parse-tos tos)
    (parse-rules rules)))


(parse config)

(defn -main
  [])