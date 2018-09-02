(ns karabiner-configurator.conditions
  (:require
   [karabiner-configurator.misc :refer :all]
   [karabiner-configurator.data :refer :all]))

(def used-simlayers-config nil)

(defn update-used-simlayers-config [config]
  (def used-simlayers-config config))

(defn cleanup-used-simlayers-config []
  (def used-simlayers-config nil))

(defn parse-conditions [condis & [from to]]
  (if (not (vector? condis))
    []
    (into []
          (for [condi condis
                :let [validate-condi
                      (assert (keyword? condi)
                              (str "invalid condition " condi ", must be a keyword"))
                      condi!? (= \! (first (into [] (name condi))))
                      condi (if condi!? (keyword (subs (name condi) 1))
                                condi)
                      result nil
                      condi-type (if condi!?
                                   "frontmost_application_unless"
                                   "frontmost_application_if")
                      result (if (nn? (condi (:applications conf-data)))
                               {:bundle_identifiers (condi (:applications conf-data))
                                :type condi-type}
                               result)
                      condi-type (if condi!?
                                   "device_unless"
                                   "device_if")
                      result (if (nn? (condi (:devices conf-data)))
                               {:identifiers
                                (condi (:devices conf-data))
                                :type condi-type}
                               result)
                      condi-type (if condi!?
                                   "input_source_unless"
                                   "input_source_if")
                      result (if (nn? (condi (:input_sources conf-data)))
                               {:input_sources
                                [(condi (:applications conf-data))]
                                :type condi-type}
                               result)
                      condi-type (if condi!?
                                   "variable_unless"
                                   "variable_if")
                      result (if (nn? (condi (:simlayers conf-data)))
                               (do
                                 (if (and from to (not condi!?))
                                   (do
                                     (update-used-simlayers-config (condi (:simlayers conf-data)))
                                     (update-used-simlayers-config (assoc-in used-simlayers-config [:from :sim]
                                                                    (conj (:sim (:from used-simlayers-config))
                                                                         (keyword (:key_code from)))))))
                                 {:name (name condi)
                                  :value 1
                                  :type condi-type})
                               result)
                      validate-result (assert (nn? result)
                                              (str "invalid condition keyword " condi ", can't find in any predefined conditions"))]]
            result))))