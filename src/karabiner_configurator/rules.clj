(ns karabiner-configurator.rules
  (:require
   [clojure.tools.logging :as log]
   [karabiner-configurator.conditions :as conditions]
   [karabiner-configurator.keys :as keys]
   [karabiner-configurator.froms :as froms]
   [karabiner-configurator.tos :as tos]
   [karabiner-configurator.conditions :as condis]
   [karabiner-configurator.data :refer :all]
   [karabiner-configurator.misc :refer :all]))

;; <from> section
;; :a      | normal key or predefined froms
;; :from-a | predefined froms
;; :!Ca    | special modifier key
;; [:a :b] | simultaneous key
;; {...}   | fallback to `froms` definition
(defn from-key
  "generate normal from key config"
  [des from]
  (let [result nil
        validate-from (massert (or (and (vector? from) (= 2 (count from)) (k? (first from)) (k? (second from)))
                                   (and (keyword? from) (or (k? from) (special-modi-k? from) (contains? (:froms conf-data) from)))
                                   (map? from))
                               (str "invalid <from> in main section's " des))
        result (if (vector? from)
                 {:from (froms/parse-from des {:sim from})}
                 result)
        result (if (and (nil? result) (keyword? from) (or (k? from) (special-modi-k? from)))
                 {:from (froms/parse-from des {:key from})}
                 result)
        result (if (and (nil? result) (contains? (:froms conf-data) from))
                 {:from (from (:froms conf-data))}
                 result)
        result (if (and (map? from) (nil? result))
                 {:from (froms/parse-from des from)}
                 result)]
    (massert (nn? result) (str "something wrong while parsing main rule " des))
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
        (massert false (str
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
               (massert (or (contains? (:tos conf-data) v)
                            (k? v)
                            (special-modi-k? v)
                            (vector? v)
                            (string? v)
                            (map? v))
                        (str "invalid to definition in main section's " des))
               (cond (keyword? v)
                     (rule-parse-keyword des v)
                     (and (vector? v) (conditions/is-simple-set-variable? v))
                     (parse-simple-set-variable des v)
                     (string? v)
                     (tos/parse-to des [{:shell v}])
                     (map? v)
                     (tos/parse-to des [v]))))))))

(defn process-to-shell-template-vector [to]
  (into []
        (for [x to]
          (if (and (vector? x) (templates? x))
            {:shell x}
            x))))

;; <to> section
;; :a                        | normal key or predefined tos
;; :to-a                     | predefined tos
;; :!Ca                      | special modifier key
;; "ls"                      | shell command
;; [:a :b]                   | multiple normal key
;; ["vi-mode" 1]             | set variable, second element in vector isn't keyword
;; ["vi-mode" :a]            | shell command then insert a
;; [:launch-template "Mail"] | shell command then insert a
;; ["cd" "ls"]               | multeple shell command
;; [["vi-mode" 1] :a]        | set variable then insert a
;; [{...}]                   | fallback to `tos` definition

;; conflict
;; ["cd" "ls"]
;; set_variable to string or two shell_command
;; two shell_command cd & ls, cd && ls
;; ["cd" "ls"] | multeple shell command
;; [{:set ["variable name" "variable value"]}] | set variable's value to string (fallback to `tos` definition)
(defn to-key
  "generate to config"
  [des to]
  (let [result nil
        validate-to (massert (or (and (keyword? to) (or (k? to)
                                                        (special-modi-k? to)
                                                        (contains? (:input-sources conf-data) to)
                                                        (contains? (:tos conf-data) to)))
                                 (string? to)
                                 (vector? to)
                                 (map? to))
                             (str "invalid <to> in main section's " des))
        result (if (contains? (:input-sources conf-data) to)
                 (into [] (tos/parse-to des [{:input to}]))
                 result)
        result (if (and (keyword? to) (not (contains? (:input-sources conf-data) to)))
                 (rule-parse-keyword des to)
                 result)
        result (if (nn? result)
                 (cond (vector? result)
                       result
                       :else
                       [result])
                 result)
        to (if (vector? to) (process-to-shell-template-vector to) to)
        result (cond (and (vector? to) (templates? to))
                     ;; (tos/parse-to des [{:shell (apply format (flatten [((first to) (:templates conf-data)) [rest to]]))}])
                     (tos/parse-to des [{:shell to}])
                     (vector? to)
                     (to-key-vector des to result)
                     (string? to)
                     (into [] (tos/parse-to des [{:shell to}]))
                     (map? to)
                     (into [] (tos/parse-to des [to]))
                     :else result)]
    result))

(defn merge-multiple-device-conditions
  [vec]
  (update-conf-data (assoc conf-data :devices (dissoc (:devices conf-data) :temp-device)))
  (let [devices-list (for [item vec
                           :when (and (keyword? item) (devices? item))
                           :let [this-device-vec (item (:devices conf-data))
                                 temp-device-vec (if (devices? :temp-device)
                                                   (into [] (concat (:temp-device (:devices conf-data)) this-device-vec))
                                                   this-device-vec)
                                 update-temp-device-into-conf-data (update-conf-data (assoc-in conf-data [:devices :temp-device] temp-device-vec))]]
                       item)
        use-temp-device? (> (count devices-list) 0)
        new-conditions (if use-temp-device? (conj (into [] (reduce #(remove #{%2} %1) vec devices-list)) :temp-device)
                           vec)]
    new-conditions))

;; <conditions>
;; :chrome
;; [:vi-mode :hhkb :!chrome]
;; ["vi-mode" 1]
;; ["vi-mode" 0]
;; [:vi-mode ["w mode" 1] :!chrome]
(defn conditions-key
  [des conditions prev-result]
  (let [conditions (merge-multiple-device-conditions conditions)]
    (if (conditions/is-simple-set-variable? conditions)
      {:conditions (conditions/parse-conditions [conditions] (:from prev-result) (:to prev-result))}
      {:conditions (conditions/parse-conditions conditions (:from prev-result) (:to prev-result))})))

;; <other options> section
;; to_if_alone                                    | :alone
;; to_if_held_down                                | :held
;; to_after_key_up                                | :afterup
;; to_delayed_action                              | :delayed
;;   to_if_canceled                               |   :canceled
;;   to_if_invoked                                |   :invoked
;; parameters                                     | :params
;;   basic.to_if_alone_timeout_milliseconds       |   :alone
;;   basic.to_if_held_down_threshold_milliseconds |   :held
;;   to_delayed_action_delay_milliseconds         |   :delay  FIXME should there be a "basic.", there's none on the spec page
;;   basic.simultaneous_threshold_milliseconds    |   :sim
(defn additional-key
  "parse additional keys"
  [des additional prevresult]
  (let [result prevresult
        {:keys [alone held afterup delayed params]} additional
        {:keys [canceled invoked]} delayed
        result (if alone (assoc result :to_if_alone (to-key des alone)) result)
        result (if held (assoc result :to_if_held_down (to-key des held)) result)
        result (if afterup (assoc result :to_after_key_up (to-key des afterup)) result)
        result (if invoked (assoc-in result [:to_delayed_action :to_if_invoked] (to-key des invoked)) result)
        result (if canceled (assoc-in result [:to_delayed_action :to_if_canceled] (to-key des canceled)) result)
        {:keys [alone held delay sim]} params
        result (if (number? alone) (assoc-in result [:parameters :basic.to_if_alone_timeout_milliseconds] alone) result)
        result (if (number? held) (assoc-in result [:parameters :basic.to_if_held_down_threshold_milliseconds] held) result)
        result (if (number? delay) (assoc-in result [:parameters :basic.to_delayed_action_delay_milliseconds] delay) result)
        result (if (number? sim) (assoc-in result [:parameters :basic.simultaneous_threshold_milliseconds] sim) result)]
    result))


(defn parse-rule
  "generate one manipulator"
  ([des from to]
   (let [result {}
         ;; result {:goku-id (next-rule-id)}
         result (assoc result :from (:from (from-key des from)))
         result (assoc result :to (to-key des to))
         result (assoc result :type "basic")]
     result))
  ([des from to conditions]
   (let [result {}
         ;; result {:goku-id (next-rule-id)}
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
         ;; result {:goku-id (next-rule-id)}
         result (if additional
                  (additional-key des additional result)
                  {})
         result (assoc result :from (:from (from-key des from)))
         result (if to (assoc result :to (to-key des to)) result)
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
                        insert-simlayer (if (:to_if_held_down result)
                                          (assoc insert-simlayer :to_if_held_down (:to_if_held_down result))
                                          insert-simlayer)
                        insert-simlayer (if (:to_if_invoked (:to_delayed_action result))
                                          (assoc-in insert-simlayer
                                                    [:to_delayed_action :to_if_invoked]
                                                    (:to_if_invoked (:to_delayed_action result)))
                                          insert-simlayer)
                        insert-simlayer (if (:to_if_canceled (:to_delayed_action result))
                                          (assoc-in insert-simlayer
                                                    [:to_delayed_action :to_if_canceled]
                                                    (:to_if_canceled (:to_delayed_action result)))
                                          insert-simlayer)
                        cleanup-used-simlayers-config (conditions/cleanup-used-simlayers-config)]
                    [result insert-simlayer])
                  result)]
     result)))

(def current-in-rules-conditions nil)

(defn define-current-in-rule-conditions
  "record current in rule conditions and used in following rules"
  [condis]
  (if (nil? condis)
    (def current-in-rules-conditions nil)
    (if (not (vector? condis))
      (def current-in-rules-conditions [condis])
      (def current-in-rules-conditions (pop (into [] (reverse condis)))))))

(defn add-current-in-rule-conditions
  "add current in rule conditions into following rules"
  [rule]
  (let [[from to conditions other-options] rule
        ;; check condition format
        vector-conditions? (vector? conditions)
        simple-set-variable? (and vector-conditions? (condis/is-simple-set-variable? conditions))
        keyword-conditions? (keyword? conditions)

        ;; add current in rule conditions
        conditions
        (cond (or simple-set-variable? keyword-conditions?)
              (conj current-in-rules-conditions conditions)
              vector-conditions?
              (into [] (concat current-in-rules-conditions conditions))
              :else
              current-in-rules-conditions)]
    ;; return results
    (cond (nn? other-options) [from to conditions other-options]
          (nn? conditions) [from to conditions]
          :else [from to])))

(def all-the-rules
  "all rules defined in edn config file, use index of vector as rule id

  [{:condis [:Emacs :Browsers :q-mode]}]"
  [])

(defn store-and-add-id-to-rule
  "store rule into vector of maps and add id to rule for later use"
  [rule]
  (let [[from to condition other-options] rule]))

(defn generate-one-rules
  "generate on rules (one object with des and manipulators in karabiner.json)"
  [des rules]
  {:description des
   :manipulators
   (into []
         (flatten
          ;; check in rule conditions
          (let [rules-with-current-in-rule-conditions
                (into [] (for [rule rules]
                           (if (or (keyword? rule) (and (vector? rule) (= (first rule) :condi)))
                             (do (define-current-in-rule-conditions rule) nil)
                             (if (nn? current-in-rules-conditions)
                               (add-current-in-rule-conditions rule)
                               rule))))
                cleanup-circ (define-current-in-rule-conditions nil)]
            ;; parse rule
            (for [rule rules-with-current-in-rule-conditions
                  :when (nn? rule)]
              (let [[from to condition other-options] rule
                    ;; a rule must have a from event defination and to event defination
                    ;; from event defination is defined in <from> section
                    ;; to event defination can be defined in <to> section or <other-options> section (as :alone :delayed :afterup)
                    validate-rule (massert (and (nn? from) (or (nn? other-options) (nn? to))) (str "invalid rule: " des ", <from> or <to>/<other-options> is nil"))]
                    ;; current-profile (if (= from :profile) (into [] (rest rule)) [data/default-profile-name])]
                (cond (and (nil? other-options) (nil? condition)) (parse-rule des from to)
                      (and (nil? other-options) (nn? condition)) (parse-rule des from to condition)
                      (nn? other-options) (parse-rule des from to condition other-options)))))))})

(defn generate
  "parse mains and generate all rules for converting to json"
  [mains]
  (let [user-result (for [{:keys [des rules]} mains]
                      (generate-one-rules des rules))
        layer-result {:description "auto generated layer trigger key"
                      :manipulators (into [] (for [[layer-name layer-definition] (:layers conf-data)]
                                               layer-definition))}
        add-layer-result (if (> (count (:manipulators layer-result)) 0) (conj user-result layer-result) user-result)]
    (into [] add-layer-result)))

(defn parse-mains
  "parse main section to final edn format, ready to convert to json"
  [mains]
  (generate mains))