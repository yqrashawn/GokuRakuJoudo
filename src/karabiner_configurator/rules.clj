(ns karabiner-configurator.rules
  (:require
   [clojure.tools.logging :as log]
   [karabiner-configurator.conditions :as conditions]
   [karabiner-configurator.keys :as keys]
   [karabiner-configurator.froms :as froms]
   [karabiner-configurator.tos :as tos]
   [karabiner-configurator.data :refer :all]
   [karabiner-configurator.misc :refer :all]))

;; <from> section
;; :a      | normal key or predefined froms
;; :from-a | predefined froms
;; :!Ca    | special modifier key
;; [:a :b] | simultaneous key
;; {...}   | fallback to `froms` defination
(defn from-key
  "generate normal from key config"
  [des from]
  (let [result nil
        validate-from (assert (or (and (vector? from) (= 2 (count from)) (k? (first from)) (k? (second from)))
                                  (and (keyword? from) (or (k? from) (special-modi-k? from) (nn? (from (:froms conf-data)))))
                                  (map? from))
                              (str "invalid <from> in main section's " des))
        result (if (vector? from)
                 {:from (froms/parse-from :tmp-from-defination-from-main {:sim from})}
                 result)
        result (if (and (nil? result) (keyword? from) (or (k? from) (special-modi-k? from)))
                 {:from (froms/parse-from :tmp-from-defination-from-main {:key from})}
                 result)
        result (if (nil? result)
                 {:from (from (:froms conf-data))}
                 result)
        result (if (and (map? from) (nil? result))
                 {:from (froms/parse-from :tmp-from-defination-from-main from)}
                 result)]
    (assert (nn? result) (str "something wrong while parsing main rule " des))
    result))

(defn parse-simple-set-variable
  [des vec & [from-condition?]]
  (first (tos/parse-to des [{:set vec}])))

(defn rule-parse-keyword [des to]
  (cond (pointing-k? to)
        (first (tos/parse-to des [{:pkey to}]))
        (consumer-k? to)
        (first (tos/parse-to des [{:ckey to}]))
        (or (special-modi-k? to) (k? to))
        (first (tos/parse-to des [{:key to}]))
        (contains? (:tos conf-data) to)
        (to (:tos conf-data))
        :else
        (assert false (str
                       "code shouldn't be here, something wrong while parsing <to> in main section's "
                       des
                       ". Please check your data and contact the author, so that the author can improve the error message."))))

(defn to-key-vector
  [des to prevresult]
  (if (conditions/is-simple-set-variable? to)
    [(parse-simple-set-variable des to)]
    (into []
          (flatten
           (for [v to] ;; this for only return flatten vector
             (do
               (assert (or (contains? (:tos conf-data) v)
                           (k? v)
                           (special-modi-k? v)
                           (and (vector? v) (= 2 (count v)) (string? (first v)) (number? (second v)))
                           (string? v)
                           (map? v))
                       (str "invalid to defination in main section's " des))
               (cond (keyword? v)
                     (rule-parse-keyword des v)
                     (vector? v)
                     (parse-simple-set-variable des v)
                     (string? v)
                     (tos/parse-to des [{:shell v}])
                     (map? v)
                     (tos/parse-to des [v]))))))))

;; <to> section
;; :a                 | normal key or predefined tos
;; :to-a              | predefined tos
;; :!Ca               | special modifier key
;; "ls"               | shell command
;; [:a :b]            | multiple normal key
;; ["vi-mode" 1]      | set variable, second element in vector isn't keyword
;; ["vi-mode" :a]     | shell command then insert a
;; ["cd" "ls"]        | multeple shell command
;; [["vi-mode" 1] :a] | set variable then insert a
;; [{...}]              | fallback to `tos` defination

;; conflict
;; ["cd" "ls"]
;; set_variable to string or two shell_command
;; two shell_command cd & ls, cd && ls
;; ["cd" "ls"] | multeple shell command
;; [{:set ["variable name" "variable value"]}] | set variable's value to string (fallback to `tos` defination)
(defn to-key
  "generate to config"
  [des to]
  (let [result nil
        validate-to (assert (or (and (keyword? to) (or (k? to) (special-modi-k? to) (contains? (:tos conf-data) to)))
                                (string? to)
                                (vector? to)
                                (map? to))
                            (str "invalid <to> in main section's " des))
        result (if (keyword? to)
                 (rule-parse-keyword des to))
        result (if (nn? result)
                 (cond (vector? result)
                       result
                       :else
                       [result])
                 result)
        result (cond (vector? to)
                     (to-key-vector des to result)
                     (string? to)
                     (into [] (tos/parse-to des [{:shell to}]))
                     (map? to)
                     (into [] (tos/parse-to des [to]))
                     :else result)]
    result))

;; conditions
;; :vi-mode or [:vi-mode]
(defn conditions-key
  [des conditions prev-result]
  (if (conditions/is-simple-set-variable? conditions)
    {:conditions (conditions/parse-conditions [conditions] (:from prev-result) (:to prev-result))}
    {:conditions (conditions/parse-conditions conditions (:from prev-result) (:to prev-result))}))

(def test-data
  {:alone :a
   :held :b
   :afterup :c
   :delayed {:invoked :d
             :cancled :e}})

(defn additional-key
  [des additional prevresult]
  (let [result prevresult
        {:keys [alone held afterup delayed]} additional
        {:keys [cancled invoked]} delayed
        result (if alone (assoc result :to_if_alone (to-key des alone)) result)
        result (if held (assoc result :to_if_held_down (to-key des held)) result)
        result (if afterup (assoc result :to_after_key_up (to-key des afterup)) result)
        result (if invoked (assoc-in result [:to_delayed_action :to_if_invoked] (to-key des invoked)) result)
        result (if cancled (assoc-in result [:to_delayed_action :to_if_cancled] (to-key des cancled)) result)]
    result))

(defn parse-rule
  "generate one configuration"
  ([des from to]
   (let [result {}
         result (assoc result :from (:from (from-key des from)))
         result (assoc result :to (to-key des to))
         result (assoc result :type "basic")]
     result))
  ([des from to conditions]
   (let [result {}
         result (assoc result :from (:from (from-key des from)))
         result (assoc result :to (to-key des to))
         result (if conditions
                  (if (vector? conditions)
                    (assoc result :conditions (:conditions (conditions-key des conditions result)))
                    (assoc result :conditions (:conditions (conditions-key des (into [] (flatten [conditions])) result))))
                  result)
         result (assoc result :type "basic")
         result (if conditions/used-simlayers-config
                  (let [insert-simlayer conditions/used-simlayers-config
                        insert-simlayer (assoc insert-simlayer :from
                                               (froms/parse-from des (:from insert-simlayer)))
                        insert-simlayer (assoc insert-simlayer :to
                                               (into [] (concat (tos/parse-to des (:to insert-simlayer)) (:to result))))
                        cleanup-used-simlayers-config (conditions/cleanup-used-simlayers-config)]
                    [result insert-simlayer])
                  result)]
     result))
  ([des from to conditions additional]
   (let [result {}
         result (if additional
                  (additional-key des additional result)
                  {})
         result (assoc result :from (:from (from-key des from)))
         result (if to (assoc result :to (to-key des to)) result)
         result (if conditions
                  (if (vector? conditions)
                    (assoc result :conditions (:conditions (conditions-key des conditions)) result)
                    (assoc result :conditions (:conditions (conditions-key des (into [] (flatten [conditions])) result))))
                  result)
         result (assoc result :type "basic")
         result (if conditions/used-simlayers-config
                  (let [insert-simlayer conditions/used-simlayers-config
                        insert-simlayer (assoc insert-simlayer :from
                                               (froms/parse-from des (:from insert-simlayer)))
                        insert-simlayer (assoc insert-simlayer :to
                                               (into [] (concat (tos/parse-to des (:to insert-simlayer)) (:to result))))
                        cleanup-used-simlayers-config (conditions/cleanup-used-simlayers-config)]
                    [result insert-simlayer])
                  result)]
     result)))

(defn generate
  [mains]
  (for [{:keys [des rules]} mains]
    {:description des
     :manipulators
     (into []
           (flatten
            (for [rule rules]
              (let [[from to condition other-options] rule]
                (do
                  (assert (and (nn? from) (or (nn? other-options) (nn? to))) (str "invalid rule: " des ", <from> or <to> is nil"))
                  (cond (and (nil? other-options) (nil? condition)) (parse-rule des from to)
                        (and (nil? other-options) (nn? condition)) (parse-rule des from to condition)
                        (nn? other-options) (parse-rule des from to condition other-options)))))))}))

(defn parse-mains [mains]
  (into [] (generate mains)))
