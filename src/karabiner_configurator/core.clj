(ns karabiner-configurator.core
  (:require
   [clojure.java.io :as io]
   [schema.core :as s]
   [clojure.edn :as edn])
  (:gen-class))

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

(def config (load-edn "resources/configurations/config.edn"))

(defn from-simu
  "generate simultaneous from key config"
  [from])

(defn from-key
  "generate normal from key config"
  [from]
  (cond))

(defn to-key
  "generate to config"
  [to])

(defn layer-cond
  "generate layer config"
  [layer])

(defn device-cond
  "generate layer config"
  [layer])

(defn new-conf
  "generate one configuration"
  ([from to]
   (str (from-key from) (to-key to)))
  ([from to layer]
   (str (from-key from) (to-key to) (layer-cond layer)))
  ([from to layer device]
   (str (from-key from) (to-key to) (layer-cond layer) (device-cond))))

(defn -main
 [])