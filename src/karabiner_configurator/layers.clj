(ns karabiner-configurator.layers
  (:require
   [karabiner-configurator.conditions :refer :all]
   [karabiner-configurator.froms :as froms]
   [karabiner-configurator.tos :as tos]
   [karabiner-configurator.data :refer :all]
   [karabiner-configurator.misc :refer :all]))

(defn generate-simlayers
  [simlayers]
  (doseq [[k v] simlayers]
    (let [validate-simlayer
          (assert (and
                   (nn? v)
                   (nn? (:key v))
                   (k? (:key v)))
                  (str "invalid simlayer definition " k))
          condi (:condi v)
          condi (if (or (keyword? condi) (map? condi)) [condi] condi)
          validate-condition
          (assert (or (nil? condi)
                      (and (nn? condi) (vector? condi)))
                  (str "invalid condition definition in simlayer " k ", condition must be a vector or map or keyword"))
          afterup (:afterup v)
          key (:key v)
          result (:simlayers conf-data)
          result (assoc result k {:type "basic"
                                  :parameters {:basic.simultaneous_threshold_milliseconds (:simlayer-threshold conf-data)}
                                  :to [{:set [(name k) 1]}]
                                  :from {:sim [key]
                                         :simo {:interrupt true
                                                :dorder :strict
                                                :uorder :strict_inverse
                                                :afterup {:set [(name k) 0]}}}})
          result (if afterup
                   (assoc-in result [k :from :simo :afterup] (into [] (flatten [(get-in result [k :from :simo :afterup]) afterup])))
                   result)
          result (if (nn? condi)
                   (if (is-simple-set-variable? condi)
                     (assoc-in result [k :conditions] (parse-conditions [condi]))
                     (assoc-in result [k :conditions] (parse-conditions condi)))
                   result)]
      (update-conf-data (assoc conf-data :simlayers result)))))

(defn generate-layers
  [layers]
  (doseq [[k v] layers]
    (let [validate-layer
          (assert (and
                   (nn? v)
                   (nn? (:key v))
                   (k? (:key v)))
                  (str "invalid layer definition " k))
          condi (:condi v)
          condi (if (or (keyword? condi) (map? condi)) [condi] condi)
          validate-condition
          (assert (or (nil? condi)
                      (and (nn? condi) (vector? condi)))
                  (str "invalid condition definition in layer " k ", condition must be a vector or map or keyword"))
          afterup (:afterup v)
          key (:key v)
          result (:layers conf-data)
          result (assoc result k {:type "basic"
                                  :to [{:set [(name k) 1]}]
                                  :alone [{:key key}]
                                  :from {:key key}
                                  :afterup [{:set [(name k) 0]}]})
          result (if afterup
                   (assoc-in result [k :afterup] (into [] (flatten [(get-in result [k :afterup]) afterup])))
                   result)
          result (assoc-in result [k :to_after_key_up] (into [] (tos/parse-to (str "auto insert definition of layer" (name k)) (:afterup (k result)))))
          result (assoc-in result [k :to_if_alone] (into [] (tos/parse-to (str "auto insert definition of layer" (name k)) (:alone (k result)))))
          result (assoc-in result [k :to] (into [] (tos/parse-to (str "auto insert definition of layer" (name k)) (:to (k result)))))
          result (assoc-in result [k :from] (froms/parse-from (str "auto insert definition of layer" (name k)) (:from (k result))))
          result (dissoc-in result [k :afterup])
          result (dissoc-in result [k :alone])
          result (if (nn? condi)
                   (if (is-simple-set-variable? condi)
                     (assoc-in result [k :conditions] (parse-conditions [condi]))
                     (assoc-in result [k :conditions] (parse-conditions condi)))
                   result)]
      (update-conf-data (assoc conf-data :layers result)))))

(defn parse-simlayers [simlayers]
  (if (nn? simlayers)
    (generate-simlayers simlayers)))

(defn parse-layers [layers]
  (if (nn? layers)
    (generate-layers layers)))