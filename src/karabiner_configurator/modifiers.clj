(ns karabiner-configurator.modifiers
  (:require [karabiner-configurator.misc :refer :all]
            [karabiner-configurator.data :refer :all]))

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


(defn generate
  [modifiers]
  (assoc conf-data :modifiers
         (into
          {}
          (for [[modifier-name modifier-info] modifiers]
            {modifier-name
             (cond (vector? modifier-info)
                   (parse-vector-modifiers modifier-name modifier-info)
                   (map? modifier-info)
                   (parse-map-modifiers modifier-name modifier-info)
                   (keyword? modifier-info)
                   (parse-keyword-modifiers modifier-name modifier-info))}))))

(defn parse-modifiers
  "parse modifires to string"
  [modifiers]
  (if (nn? modifiers)
    (update-conf-data (generate modifiers))))

(def example-modifers {:modifiers {
                                   :111 [:left_command :left_control]
                                   :222 {:mandatory [:left_command :left_shift]}
                                   :3 {:mandatory :left_command}
                                   :444 {:optional :any}}})

(generate (:modifiers example-modifers))