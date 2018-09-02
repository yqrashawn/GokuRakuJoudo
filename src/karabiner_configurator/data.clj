(ns karabiner-configurator.data
  (:require
   [karabiner-configurator.misc :refer :all]))

(declare conf-data)
(defn init-conf-data
  []
  (def conf-data {:applications {}
                  :devices {}
                  :input-source {}
                  :modifiers {}
                  :froms {}
                  :tos {}
                  :layers {}
                  :simlayers {}
                  :simlayer-threshold 250
                  :swaps {}
                  :raws {}}))

;; (init-conf-data)

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

(def mkey-keyword {:x {:name :x}
                   :y {:name :y}
                   :vwheel {:name :vertical_wheel}
                   :hwheel {:name :horizontal_wheel}
                   :speed {:name :speed_multiplier}})

(defn mouse-keyword?
  [k]
  (nn? (k mkey-keyword)))

(defn mouse-key-name
  [k]
  (assert (mouse-keyword? k) (str "invalid mouse key keyword " k))
  (:name (k mkey-keyword)))



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
