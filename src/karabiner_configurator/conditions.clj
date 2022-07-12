(ns karabiner-configurator.conditions
  (:require
   [karabiner-configurator.data :refer [conf-data simlayers? input-sources? devices?]]
   [karabiner-configurator.misc :refer [massert]]))

(def used-simlayers-config (atom nil))

(defn update-used-simlayers-config [config]
  (reset! used-simlayers-config config))

(defn cleanup-used-simlayers-config []
  (reset! used-simlayers-config nil))

(defn is-simple-set-variable? [vec]
  (and (= 2 (count vec)) (string? (first vec)) (number? (second vec))))

(defn parse-conditions [condis & [from to layers]]
  (if (not (vector? condis))
    []
    (vec
     (for [condi condis
           :let [_validate-condi
                 (massert (or (and (vector? condi) (is-simple-set-variable? condi)) (keyword? condi))
                          (str "invalid condition " condi ", must be a keyword or simple condition definition"))
                 condi!? (when (keyword? condi) (= \! (first (vec (name condi)))))
                 condi (if condi!? (keyword (subs (name condi) 1))
                           condi)
                 result nil
                 condi-type (if condi!?
                              "frontmost_application_unless"
                              "frontmost_application_if")
                 result (if (and (keyword? condi) (some? (condi (:applications @conf-data))))
                          (let [this-condi (condi (:applications @conf-data))
                                this-condi (if (= (first this-condi) :identifiers) this-condi (into [:identifiers] (condi (:applications @conf-data))))
                                [identifiers paths] [(into [] (rest (take-while (complement #{:paths}) this-condi))) (into [] (rest (drop-while (complement #{:paths}) this-condi)))]
                                [identifiers paths] [(or identifiers []) (or paths [])]
                                identifiers? (seq identifiers)
                                paths? (seq paths)
                                rst {:type condi-type}
                                rst (if identifiers? (assoc rst :bundle_identifiers identifiers) rst)
                                rst (if paths? (assoc rst :file_paths paths) rst)]
                            rst)
                          result)
                 condi-type (if condi!?
                              "device_unless"
                              "device_if")
                 result (if (and (keyword? condi) (devices? condi))
                          {:identifiers
                           (vec (condi (:devices @conf-data)))
                           :type condi-type}
                          result)
                 condi-type (if condi!?
                              "input_source_unless"
                              "input_source_if")
                 result (if (and (keyword? condi) (input-sources? condi))
                          {:input_sources
                           [(condi (:input-sources @conf-data))]
                           :type condi-type}
                          result)
                 condi-type (if condi!?
                              "variable_unless"
                              "variable_if")
                 result (if (and (keyword? condi) (simlayers? condi))
                          (do
                            (when (and from to (not condi!?))
                              (update-used-simlayers-config (condi (:simlayers @conf-data)))
                              (update-used-simlayers-config (assoc-in @used-simlayers-config [:from :sim]
                                                                      (vec (conj (:sim (:from @used-simlayers-config))
                                                                                 (keyword (:key_code from)))))))

                            ;; so that we can filter simlayer conditions in the
                            ;; none sim one and concat it into the sim one
                            (with-meta {:name (name condi)
                                        :value 1
                                        :type condi-type}
                              {:simlayer condi}))
                          result)
                 result (if (and (keyword? condi) (some? (or (condi (:layers @conf-data))
                                                             (condi layers))))
                          (with-meta {:name (name condi)
                                      :value 1
                                      :type condi-type}
                            {:is-layer true})
                          result)
                 result (if (vector? condi)
                          {:name (name (first condi))
                           :value (second condi)
                           :type "variable_if"}
                          result)
                 ;; if we still can't find the right condition, assume
                 ;; user's input is correct and the condition is just not
                 ;; defined yet
                 result (if (and (nil? result) (keyword? condi))
                          {:name (name condi)
                           :value 1
                           :type condi-type}
                          result)
                 _validate-result (massert (some? result)
                                           (str "invalid condition keyword " condi ", can't find in any predefined conditions"))]]
       result))))
