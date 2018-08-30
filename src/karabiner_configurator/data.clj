(ns karabiner-configurator.data
  (:require
   [karabiner-configurator.misc :refer :all]))

(def conf-data {:applications {}
                :devices {}
                :input-source {}
                :modifiers {}
                :froms {}
                :tos {}
                :layers {}
                :simlayers {}
                :swaps {}
                :raws {}})

(def keys-info (load-edn "resources/configurations/keycode.edn"))

(defn applications? [k] (nn? (k (:applications conf-data))))
(defn devices? [k] (nn? (k (:devices conf-data))))
(defn input-sources? [k] (nn? (k (:input-source conf-data))))
(defn modifiers? [k] (nn? (k (:modifiers conf-data))))
(defn froms? [k] (nn? (k (:froms conf-data))))
(defn layers? [k] (nn? (k (:layers conf-data))))
(defn simlayers? [k] (nn? (k (:simlayers conf-data))))


(defn k?
  [k]
  (assert (keyword? k) (str "invalid key keyword " k))
  (nn? (k keys-info)))

(defn modifier-k?
  [k]
  (k? k)
  (true? (:modifier (k keys-info))))

(defn from-k?
  [k]
  (k? k)
  (nil? (:not-from (k keys-info))))

(defn consumer-k?
  [k]
  (k? k)
  (true? (:consumer-key (k keys-info))))

(defn pointing-k?
  [k]
  (k? k)
  (true? (:button (k keys-info))))


(defn update-conf-data
  [data]
  (def conf-data data))

(defn assoc-conf-data
  [key data]
  (def conf-data (assoc conf-data key data)))

(defn assoc-in-conf-data
  [keys-vector data]
  (def conf-data (assoc-in conf-data keys-vector data)))

(def output "output data that will convert into json string" [])