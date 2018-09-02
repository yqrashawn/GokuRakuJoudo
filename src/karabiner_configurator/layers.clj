(ns karabiner-configurator.layers
  (:require
   [karabiner-configurator.conditions :refer :all]
   [karabiner-configurator.data :refer :all]
   [karabiner-configurator.misc :refer :all]))

(defn parse-layers [layers]
  (if (nn? layers)
    (for [[layer-name layer-info] layers]
      (let [{:keys [from-value]} layer-info]))))

(defn generate
  [simlayers]
  (assoc conf-data
         :simlayers
         (into {}
               (for [[k v] simlayers]
                 (let [validate-simlayer
                       (assert (and
                                (nn? v)
                                (nn? (:key v))
                                (k? (:key v)))
                               (str "invalid simlayer defination " k))
                       condi (:condi v)
                       validate-condition
                       (assert (or (nil? condi)
                                   (and (nn? condi) (vector? condi)))
                               (str "invalid condition defination in simlayer " k ", condition must be a vector"))
                       key (:key v)
                       result {k {:parameters {:basic.simultaneous_threshold_milliseconds (:simlayer-threshold conf-data)}
                                  :to [{:set [(name k) 1]}]
                                  :from {:sim [key]
                                         :simo {:dorder :strict
                                                :uorder :strict_inverse
                                                :afterup {:set [(name k) 0]}}}}}
                       result (if (nn? condi)
                                (assoc-in result [k :conditions] (parse-conditions condi))
                                result)]
                   result)))))

(defn parse-simlayers [simlayers]
  (if (nn? simlayers)
    (update-conf-data (generate simlayers))))