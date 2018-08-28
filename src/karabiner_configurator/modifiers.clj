(ns karabiner-configurator.modifiers
  (:require [karabiner-configurator.misc :refer :all]
            [karabiner-configurator.data :refer :all]))

;; (defn modifer-template {:name {:mandatory []} {:optional []}})

(defn parse-modifiers
  "parse modifires to string"
  [modifiers]
  (if (nn? modifiers)
    (update-conf-data (generate modifiers))))

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

(defn generate
  [modifiers]
  (assoc conf-data :modifiers
         (into
          {}
          (for [[modifier-name modifier-info] modifiers]
            {modifier-name
             (cond (vector? modifier-info)
                   {:mandatory
                    (into []
                          (for [modifier-key modifier-info]
                            (do (assert (modifier-k? modifier-key)
                                        (str "invliad modifer key: " modifier-key " in " modifier-name))
                                (name modifier-key))))}
                   (map? modifier-info)
                   (let [{:keys [mandatory optional]} modifier-info
                         result {}]
                     (if (nn? mandatory)
                       (assoc result :mandatory (parse-modifier-arr-or-keyword mandatory modifier-name)))
                     (if (nn? optional)
                       (assoc result :optional (parse-modifier-arr-or-keyword optional modifier-name))))
                   (keyword? modifier-info)
                   (do (assert (modifier-k? modifier-info) (str "invalid modifier " modifier-info " in " modifier-name))
                       {:mandatory [(name modifier-info)]}))}))))


(def example-modifers {:modifiers {
                                   ;; :111 [:left_command :left_control]
                                   :222 {:mandatory [:left_command :left_shift]}
                                   :3 {:mandatory :left_command}
                                   :444 {:optional :any}}})

(generate (:modifiers example-modifers))