(ns karabiner-configurator.modifiers
  (:require [karabiner-configurator.misc :refer :all]
            [karabiner-configurator.data :refer :all]))

;; this file parse modifier definition
;; spec https://pqrs.org/osx/karabiner/json.html#from-event-definition-modifiers

;; user can define modifier combination in advance and use it in `froms`, `tos` or `rules`
;; it will generate a {:mandatory ["modifiers or empty"] :optional ["modifiers or any or empty"]} data structure
;; when used in `tos` or `<to>` section in rules, it will use the :mandatory section as to key modifier

(defn parse-modifier-arr-or-keyword
  [modi modifier-name]
  (cond (keyword? modi)
        (do (assert (modifier-k? modi) (str "invalid modifier " modi " in " modifier-name))
            [(name modi)])
        (vector? modi)
        (into []
              (for [v modi]
                (do (assert (modifier-k? v) (str "invalid modifier " v " in " modifier-name))
                    (name v))))))

(defn parse-vector-modifiers
  [modifier-name modifier-info]
  {:mandatory
   (into []
         (for [modifier-key modifier-info]
           (do (assert (modifier-k? modifier-key)
                       (str "invliad modifer key: " modifier-key " in " modifier-name))
               (name modifier-key))))})

(defn parse-map-modifiers
  [modifier-name modifier-info]
  (let [{:keys [mandatory optional]} modifier-info
        result {}
        result (if (nn? mandatory) (assoc result :mandatory (parse-modifier-arr-or-keyword mandatory modifier-name)) result)
        result (if (nn? optional) (assoc result :optional (parse-modifier-arr-or-keyword optional modifier-name)) result)]
    result))

(defn parse-keyword-modifiers
  [modifier-name modifier-info]
  (assert (modifier-k? modifier-info) (str "invalid modifier " modifier-info " in " modifier-name))
  {:mandatory [(name modifier-info)]})

(defn parse-single-modifier-definition
  "parse a modifer definition into a map with mandatory and optional
  used both in here and parsing froms, tos as well"
  [modifier-info & modifier-name]
  (let [modifier-name (first modifier-name)
        mname (if (nn? modifier-name)
                modifier-name
                :anonymous-modifier)]
    (cond (vector? modifier-info)
          (parse-vector-modifiers mname modifier-info)
          (map? modifier-info)
          (parse-map-modifiers mname modifier-info)
          (keyword? modifier-info)
          (parse-keyword-modifiers mname modifier-info))))

(defn generate
  [modifiers]
  (assoc conf-data :modifiers
         (into
          {}
          (for [[modifier-name modifier-info] modifiers]
            {modifier-name
             (parse-single-modifier-definition modifier-info modifier-name)}))))

(defn parse-modifiers
  "parse modifires to string"
  [modifiers]
  (if (nn? modifiers)
    (update-conf-data (generate modifiers))))
