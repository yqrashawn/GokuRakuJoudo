(ns karabiner-configurator.layers
  (:require
   [karabiner-configurator.conditions :refer :all]
   [karabiner-configurator.data :refer :all]
   [karabiner-configurator.misc :refer :all]))

(defn parse-layers [layers]
  (if (nn? layers)
    (for [[layer-name layer-info] layers]
      (let [{:keys [from-value]} layer-info]))))

(defn generate-simlayers
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
                       condi (if (or (keyword? condi) (map? condi) ) [condi] condi)
                       validate-condition
                       (assert (or (nil? condi)
                                   (and (nn? condi) (vector? condi)))
                               (str "invalid condition defination in simlayer " k ", condition must be a vector or map or keyword"))
                       key (:key v)
                       result {k {:type "basic"
                                  :parameters {:basic.simultaneous_threshold_milliseconds (:simlayer-threshold conf-data)}
                                  :to [{:set [(name k) 1]}]
                                  :from {:sim [key]
                                         :simo {:interrupt true
                                                :dorder :strict
                                                :uorder :strict_inverse
                                                :afterup {:set [(name k) 0]}}}}}
                       result (if (nn? condi)
                                (if (is-simple-set-variable? condi)
                                  (assoc-in result [k :conditions] (parse-conditions [condi]))
                                  (assoc-in result [k :conditions] (parse-conditions condi)))
                                result)]
                   result)))))

(defn parse-simlayers [simlayers]
  (if (nn? simlayers)
    (update-conf-data (generate-simlayers simlayers))))