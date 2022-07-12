(ns karabiner-configurator.modifiers
  (:require [karabiner-configurator.data :as d]
            [karabiner-configurator.misc :refer [massert]]))

;; this file parses modifier definitions
;; spec https://pqrs.org/osx/karabiner/json.html#from-event-definition-modifiers

;; user can define modifier combination in advance and use it in `froms`, `tos` or `rules`
;; it will generate a {:mandatory ["modifiers or empty"] :optional ["modifiers or any or empty"]} data structure
;; when used in `tos` or `<to>` section in rules, it will use the :mandatory section as to key modifier

(defn parse-modifier-arr-or-keyword
  [modi modifier-name]
  (cond (keyword? modi)
        (do (massert (d/modifier-k? modi) (str "invalid modifier " modi " in " modifier-name))
            [(name modi)])
        (vector? modi)
        (mapv
         (fn [vec]
           (massert (d/modifier-k? vec) (str "invalid modifier " vec " in " modifier-name))
           (name vec)) modi)))

(defn parse-vector-modifiers
  [modifier-name modifier-info]
  {:mandatory
   (mapv
    (fn [modifier-key]
      (massert (d/modifier-k? modifier-key)
               (str "invliad modifer key: " modifier-key " in " modifier-name))
      (name modifier-key))
    modifier-info)})

(defn parse-map-modifiers
  [modifier-name modifier-info]
  (let [{:keys [mandatory optional]} modifier-info
        result {}
        result (if (some? mandatory)
                 (assoc result
                        :mandatory
                        (parse-modifier-arr-or-keyword mandatory modifier-name))
                 result)
        result (if (some? optional)
                 (assoc result
                        :optional
                        (parse-modifier-arr-or-keyword optional modifier-name)) result)]
    result))

(defn parse-keyword-modifiers
  [modifier-name modifier-info]
  (massert (d/modifier-k? modifier-info) (str "invalid modifier " modifier-info " in " modifier-name))
  {:mandatory [(name modifier-info)]})

(defn parse-single-modifier-definition
  "parse a modifer definition into a map with mandatory and optional
  used both in here and parsing froms, tos as well"
  [modifier-info & modifier-name]
  (let [modifier-name (first modifier-name)
        mname (if (some? modifier-name)
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
  (assoc @d/conf-data :modifiers
         (into
          {}
          (for [[modifier-name modifier-info] modifiers]
            {modifier-name
             (parse-single-modifier-definition modifier-info modifier-name)}))))

(defn parse-modifiers
  "parse modifires to string"
  [modifiers]
  (when (some? modifiers)
    (d/update-conf-data (generate modifiers))))
