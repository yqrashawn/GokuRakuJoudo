(ns karabiner-configurator.core
  (:require
   [clojure.java.io :as io]
   [clojure.data.json :as json]
   [schema.core :as s]
   [clojure.edn :as edn]))

(defn load-edn
  "Load edn from an io/reader source (filename or io/resource)."
  [source]
  (try
    (with-open [r (io/reader source)]
      (edn/read (java.io.PushbackReader. r)))
    (catch java.io.IOException e
      (printf "Couldn't open '%s': %s\n" source (.getMessage e)))
    (catch RuntimeException e
      (printf "Error parsing edn file '%s': %s\n" source (.getMessage e)))))

(def key-info (load-edn "resources/configurations/keycode.edn"))

(def config (load-edn "resources/configurations/test/keytokey.edn"))

(defn from-key
  "generate normal from key config"
  [from])

(defn to-key
  "generate to config"
  [to])

(defn layer-cond
  "generate layer config"
  [layer])

(defn device-cond
  "generate layer config"
  [layer])

(defn froms [f])
(defn tos [t])
(defn parse-optional-arg [arg])

(defn parse-rules
  "generate one configuration"
  ([from to]
   (str (from-key from) (to-key to)))
  ([from to arg3]
   (str (from-key from) (to-key to) (parse-optional-arg arg3)))
  ([from to arg3 arg4]
   (str (from-key from) (to-key to) (parse-optional-arg arg3) (parse-optional-arg arg4))))

(defn parse
  "parse configuration"
  [conf]
  (prn conf))

(parse config)

(defn -main
 [])