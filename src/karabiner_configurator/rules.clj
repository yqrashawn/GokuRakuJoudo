(ns karabiner-configurator.rules
  (:require
   [karabiner-configurator.conditions :as conditions]
   [karabiner-configurator.data :as d :refer [pkey? k? special-modi-k? conf-data pointing-k? consumer-k? noti? softf? templates? devices? assoc-conf-data update-conf-data profile? raw-rule?]]
   [karabiner-configurator.froms :as froms]
   [karabiner-configurator.keys-symbols :as keysym]
   [karabiner-configurator.misc :refer [massert contains??]]
   [karabiner-configurator.tos :as tos]))

;; <from> section
;; :a      | normal key or predefined froms
;; :from-a | predefined froms
;; :!Ca    | special modifier key
;; [:a :b] | simultaneous key
;; {...}   | fallback to `froms` definition
(defn from-key
  "generate normal from key config"
  [des from_]
  (let [result nil
        from (keysym/key-sym-to-key from_ :dbg "from")
        _validate-from (massert (or (and (vector? from) (>= (count from) 2) (not-any? #(not (or (pkey? %) (k? %))) from))
                                    (and (keyword? from) (or (k? from) (special-modi-k? from) (contains? (:froms @conf-data) from)))
                                    (map? from))
                                (str "invalid <from> in main section's " des))
        result (if (vector? from)
                 {:from (froms/parse-from des {:sim from})}
                 result)
        result (if (and (nil? result) (keyword? from) (or (k? from) (special-modi-k? from)))
                 {:from (froms/parse-from des {:key from})}
                 result)
        result (if (and (nil? result) (contains? (:froms @conf-data) from))
                 {:from (from (:froms @conf-data))}
                 result)
        result (if (and (map? from) (nil? result))
                 {:from (froms/parse-from des from)}
                 result)]
    (massert (some? result) (str "something wrong while parsing main rule " des))
    result))

(defn parse-simple-set-variable
  [des vec & [_from-condition?]]
  (first (tos/parse-to des [{:set vec}])))

(defn rule-parse-keyword [des to]
  (cond (pointing-k? to)
        (first (tos/parse-to des [{:pkey to}]))
        (consumer-k? to)
        (first (tos/parse-to des [{:ckey to}]))
        (or (special-modi-k? to) (k? to))
        (first (tos/parse-to des [{:key to}]))
        (contains? (:tos @conf-data) to)
        (to (:tos @conf-data))
        :else
        (massert false (str
                        "code shouldn't be here, something wrong while parsing <to> in main section's "
                        des
                        ". Please check your data and contact the author, so that the author can improve the error message."))))

(defn parse-noti-with-tos [des to]
  (let [[_ id text] to]
    (first (tos/parse-to des [{:noti {:id id :text text}}]))))
(defn parse-softf-with-tos [des to]
  (let [[_ m] to]
    (first (tos/parse-to des [{:softf m}]))))
(defn parse-op-with-tos [des to]
  (let [[_ path] to]
    (first (tos/parse-to des [{:softf {:open {:file_path path}}}]))))
(defn parse-oi-with-tos [des to]
  (let [[_ idx] to]
    (first
     (tos/parse-to
      des
      [{:softf {:open {:frontmost_application_history_index idx}}}]))))
(defn parse-oid-with-tos [des to]
  (let [[_ id] to]
    (first (tos/parse-to des [{:softf {:open {:bundle_identifier id}}}]))))

(defn to-key-vector
  [des to _prevresult]
  (cond (conditions/is-simple-set-variable? to)
        [(parse-simple-set-variable des to)]
        (noti? to)
        [(parse-noti-with-tos des to)]
        (d/op? to)
        [(parse-op-with-tos des to)]
        (d/oid? to)
        [(parse-oid-with-tos des to)]
        (d/oi? to)
        [(parse-oi-with-tos des to)]
        (d/softf? to)
        [(parse-softf-with-tos des to)]
        :else
        (vec
         (flatten
          (for [v_ to] ;; this for only return flatten vector
            (do
              (def v v_)
              (if (keyword? v) (def v (keysym/key-sym-to-key v :dbg "to[]")) )
              (massert (or (contains? (:tos @conf-data) v)
                           (k? v)
                           (noti? v)
                           (softf? v)
                           (special-modi-k? v)
                           (vector? v)
                           (string? v)
                           (map? v))
                       (str "invalid to definition in main section's " des))
              (cond (keyword? v)
                    (rule-parse-keyword des v)
                    (and (vector? v) (noti? v))
                    (parse-noti-with-tos des v)
                    (and (vector? v) (conditions/is-simple-set-variable? v))
                    (parse-simple-set-variable des v)
                    (string? v)
                    (tos/parse-to des [{:shell v}])
                    (map? v)
                    (tos/parse-to des [v]))))))))

(defn process-to-shell-template-vector [to]
  (vec
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
;; ["cd" "ls"]               | multiple shell command
;; [["vi-mode" 1] :a]        | set variable then insert a
;; [{...}]                   | fallback to `tos` definition

;; conflict
;; ["cd" "ls"]
;; set_variable to string or two shell_command
;; two shell_command cd & ls, cd && ls
;; ["cd" "ls"] | multiple shell command
;; [{:set ["variable name" "variable value"]}] | set variable's value to string (fallback to `tos` definition)
(defn to-key
  "generate to config"
  [des to_]
  (let [result nil
        to (keysym/key-sym-to-key to_ :dbg "to  ")
        _validate-to (massert (or (and (keyword? to) (or (k? to)
                                                         (special-modi-k? to)
                                                         (contains? (:input-sources @conf-data) to)
                                                         (contains? (:tos @conf-data) to)))
                                  (string? to)
                                  (vector? to)
                                  (map? to))
                              (str "invalid <to> in main section's " des))
        result (if (contains? (:input-sources @conf-data) to)
                 (vec (tos/parse-to des [{:input to}]))
                 result)
        result (if (and (keyword? to) (not (contains? (:input-sources @conf-data) to)))
                 (rule-parse-keyword des to)
                 result)
        result (if (some? result)
                 (cond (vector? result)
                       result
                       :else
                       [result])
                 result)
        to (if (vector? to) (process-to-shell-template-vector to) to)
        result (cond (and (vector? to) (templates? to))
                     ;; (tos/parse-to des [{:shell (apply format (flatten [((first to) (:templates @conf-data)) [rest to]]))}])
                     (tos/parse-to des [{:shell to}])
                     (vector? to)
                     (to-key-vector des to result)
                     (string? to)
                     (vec (tos/parse-to des [{:shell to}]))
                     (map? to)
                     (vec (tos/parse-to des [to]))
                     :else result)]
    result))

(defn merge-multiple-device-conditions
  [device-vec]
  (assoc-conf-data :devices (dissoc (:devices @conf-data) :temp-device))
  (let [devices-list
        (for [item device-vec
              :when (and (keyword? item) (devices? item))
              :let [this-device-vec (item (:devices @conf-data))
                    temp-device-vec (if (devices? :temp-device)
                                      (vec (concat (:temp-device (:devices @conf-data)) this-device-vec))
                                      this-device-vec)
                    _update-temp-device-into-conf-data (update-conf-data (assoc-in @conf-data [:devices :temp-device] temp-device-vec))]]
          item)
        use-temp-device? (pos? (count devices-list))
        new-conditions (if use-temp-device? (conj (vec (reduce #(remove #{%2} %1) device-vec devices-list)) :temp-device)
                           device-vec)]
    new-conditions))

(def profile-layer-condis (atom {}))

;; <conditions>
;; :chrome
;; [:vi-mode :hhkb :!chrome]
;; ["vi-mode" 1]
;; ["vi-mode" 0]
;; [:vi-mode ["w mode" 1] :!chrome]
(defn conditions-key
  [_des conditions prev-result profiles]
  (let [conditions (merge-multiple-device-conditions conditions)
        result-condi
        (if (conditions/is-simple-set-variable? conditions)
          {:conditions (conditions/parse-conditions [conditions] (:from prev-result) (:to prev-result))}
          {:conditions (conditions/parse-conditions conditions (:from prev-result) (:to prev-result))})
        layer-condis (filter #(:is-layer (meta %)) (:conditions result-condi))]
    (when (pos? (count layer-condis))
      (doseq [profile profiles]
        (doseq [layer-condi layer-condis]
          (when (not (contains?? (profile @profile-layer-condis) (keyword (:name layer-condi))))
            (reset! profile-layer-condis
                    (assoc @profile-layer-condis profile (conj (or (profile @profile-layer-condis) []) (keyword (:name layer-condi)))))))))
    result-condi))

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
;;   basic.to_delayed_action_delay_milliseconds   |   :delay
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

(defn parse-raw-rule [from profiles] (with-meta from {:profiles profiles}))
(defn parse-rule
  "generate one manipulator/rule"
  [des from to conditions additional profiles]
  (let [result {}
        result (if additional
                 (additional-key des additional result)
                 {})
        result (assoc result :from (:from (from-key des from)))
        result (if to (assoc result :to (to-key des to)) result)
        result (if conditions
                 (if (vector? conditions)
                   (assoc result :conditions (:conditions (conditions-key des conditions result profiles)))
                   (assoc result :conditions (:conditions (conditions-key des (vec (flatten [conditions])) result profiles))))
                 result)
        result (assoc result :type "basic")
        result (if @conditions/used-simlayers-config
                 (let [insert-simlayer @conditions/used-simlayers-config
                       insert-simlayer (assoc insert-simlayer :from
                                              (froms/parse-from des (:from insert-simlayer)))
                       insert-simlayer (assoc insert-simlayer :to
                                              (vec (concat (tos/parse-to des (:to insert-simlayer)) (:to result))))
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
                       additional-condis (vec (filter
                                               (fn [condi]
                                                 (not (meta condi)))
                                               (or (:conditions result) [])))
                       insert-simlayer (if (not (= (count additional-condis) 0))
                                         (assoc insert-simlayer :conditions (vec (concat additional-condis
                                                                                         (or (:conditions insert-simlayer) []))))
                                         insert-simlayer)
                       insert-simlayer (if (:parameters result)
                                         (assoc insert-simlayer :parameters (merge (or (:parameters insert-simlayer) {})
                                                                                   (:parameters result)))
                                         insert-simlayer)
                       result (if-let [simlayer-modi (-> insert-simlayer :from :modifiers)]
                                (assoc-in result [:from :modifiers] simlayer-modi)
                                result)
                       _cleanup-used-simlayers-config (conditions/cleanup-used-simlayers-config)]
                   [(with-meta result {:profiles profiles}) (with-meta insert-simlayer {:profiles profiles})])
                 (with-meta result {:profiles profiles}))]
    result))

(def current-in-rules-conditions (atom nil))
(defn define-current-in-rule-conditions
  "record current in rule conditions and used in following rules"
  [condis]
  (if (nil? condis)
    (reset! current-in-rules-conditions nil)
    (if (not (vector? condis))
      (reset! current-in-rules-conditions [condis])
      (reset! current-in-rules-conditions (pop (vec (reverse condis)))))))
(defn add-current-in-rule-conditions
  "add current in rule conditions into following rules"
  [rule]
  (let [[from to conditions other-options] rule
        ;; check condition format
        vector-conditions? (vector? conditions)
        simple-set-variable? (and vector-conditions? (conditions/is-simple-set-variable? conditions))
        keyword-conditions? (keyword? conditions)

        ;; add current in rule conditions
        conditions
        (cond (or simple-set-variable? keyword-conditions?)
              (conj @current-in-rules-conditions conditions)
              vector-conditions?
              (vec (concat @current-in-rules-conditions conditions))
              :else
              @current-in-rules-conditions)]
    ;; return results
    (cond (some? other-options) [from to conditions other-options]
          (some? conditions) [from to conditions]
          :else [from to])))

(def current-in-rules-profiles (atom nil))
(defn define-current-in-rule-profiles
  "record current in rule profiles and used in following rules"
  [profiles]
  (if (nil? profiles)
    (reset! current-in-rules-profiles nil)
    (if (not (vector? profiles))
      (reset! current-in-rules-profiles [profiles])
      (let [profiles (pop (vec (reverse profiles)))
            _assert-profiles (massert (= (count (filter profile? profiles)) (count profiles)) (str "invalid profile in " profiles))]
        (reset! current-in-rules-profiles profiles)))))

(defn add-current-in-rule-profiles
  "add current in rule profiles into following rules"
  [rule]
  (let [[from to conditions other-options] rule]
    [from to conditions other-options @current-in-rules-profiles]))

#_(def all-the-rules
    "all rules defined in edn config file, use index of vector as rule id

  [{:condis [:Emacs :Browsers :q-mode]}]"
    [])

#_(defn store-and-add-id-to-rule
    "store rule into vector of maps and add id to rule for later use"
    [rule]
    (let [[from to condition other-options] rule]))

(def multi-profile-rules
  "{:profile-1
    {:description \"some manipulators\"
     :manipulators [rule1 rule2]}}"
  (atom {}))

(defn add-rule-into-multi-profile-rules
  "update mtuli-profile-rules depends on rules' profiles"
  [des parsed-rules]
  (doseq [parsed-rule parsed-rules]
    (doseq [profile (-> parsed-rule meta :profiles)]
      (when (nil? (profile @multi-profile-rules))
        ;; (reset! multi-profile-rules (assoc @multi-profile-rules profile [{:description des :manipulators []}]))
        (reset! multi-profile-rules (assoc @multi-profile-rules profile [{:description des :manipulators []}])))
      (let [target-des (vec (keep-indexed #(when (= des (:description %2)) [%1 %2]) (profile @multi-profile-rules)))
            target-des (filter some? target-des)
            ;; target-des (filter #(= des (:description %)) (profile @multi-profile-rules))
            target-des (and (pos? (count target-des)) (first target-des))]
        (if target-des
          (reset! multi-profile-rules
                  (assoc-in @multi-profile-rules
                            [profile (first target-des)]
                            {:description  des
                             :manipulators (conj
                                            (:manipulators (second target-des))
                                            parsed-rule)}))
          (reset! multi-profile-rules
                  (assoc @multi-profile-rules
                         profile
                         (conj (profile @multi-profile-rules)
                               {:description  des
                                :manipulators [parsed-rule]}))))))))
(defn generate-one-rules
  "generate on rules (one object with des and manipulators in karabiner.json)"
  [des rules]
  (add-rule-into-multi-profile-rules
   des
   (vec
    (flatten
     ;; check in rule conditions
     (let [result-rules
           (vec
            (for [rule rules]
              (let [rule (if (raw-rule? rule) [rule] rule)]
                ;; if there's current in rule conditions define them
                (if (or (and (keyword? rule) (not (profile? rule))) (and (vector? rule) (or (= (first rule) :condis)
                                                                                            (= (first rule) :condi))))
                  ;; TODO: change it to the clojure way (don't use mutable data)
                  (do (define-current-in-rule-conditions rule) nil)
                  (if (and
                       (some? @current-in-rules-conditions)
                       (not (profile? rule))
                       (and (vector? rule) (not (or (= (first rule) :profiles)
                                                    (= (first rule) :profile)))))
                    (add-current-in-rule-conditions rule)
                    rule)))))
           _cleanup-circ (define-current-in-rule-conditions nil)
           result-rules
           (vec
            (for [rule result-rules
                  :when (some? rule)]
              ;; a rule is a keyword of profile name or a vector [:profile :profile-name]
              (if (or (and (keyword? rule) (profile? rule)) (and (vector? rule) (or (= (first rule) :profiles)
                                                                                    (= (first rule) :profile))))
                (do (define-current-in-rule-profiles rule) nil)
                (if (some? @current-in-rules-profiles)
                  (add-current-in-rule-profiles rule)
                  rule))))
           _cleanup-cirp (define-current-in-rule-profiles nil)]

       ;; parse rule
       (for [rule result-rules
             :when (some? rule)]
         (let [[from to condition other-options profiles] rule
               profiles (if (some? profiles) profiles [@d/user-default-profile-name])
               ;; a rule must have a from event defination and to event defination
               ;; from event defination is defined in <from> section
               ;; to event defination can be defined in <to> section or <other-options> section (as :alone :delayed :afterup)
               _validate-rule (massert
                               (and (some? from)
                                    (or
                                     (some? (:type from))
                                     (some? other-options)
                                     (some? to)))
                               (str "invalid rule: " des ", <from> or <to>/<other-options> is nil" from "\n" to "\n" other-options))]
           (cond
             (some? (:type from)) (parse-raw-rule from profiles)
             (and (nil? other-options) (nil? condition)) (parse-rule des from to nil nil profiles)
             (and (nil? other-options) (some? condition)) (parse-rule des from to condition nil profiles)
             (some? other-options) (parse-rule des from to condition other-options profiles)))))))))

(defn generate
  "parse mains and generate all rules for converting to json"
  [mains]
  (let [;; user-result (do
        ;;               (doseq [{:keys [des rules]} mains]
        ;;                 (generate-one-rules des rules))
        ;;               @multi-profile-rules)
        _update-multi-profile-rules
        (doseq [{:keys [des rules]} mains]
          (massert (some? des) "missing description key :des in one rule, please check your config file")
          (generate-one-rules des rules))
        _update-profile-layer-condis
        (doseq [[profile condis] @profile-layer-condis]
          ;; TODO: change it to the clojure way (don't use mutable data)
          (reset! profile-layer-condis (assoc @profile-layer-condis profile (vec (for [condi condis]  (condi (:layers @conf-data)))))))

        _add-layer-result (doseq [[profile condis] @profile-layer-condis]
                            (when (profile @multi-profile-rules)
                              ;; TODO: change it to the clojure way (don't use mutable data)
                              (reset! multi-profile-rules
                                      (assoc @multi-profile-rules
                                             profile
                                             (vec (cons
                                                   {:description  "Auto generated layer conditions"
                                                    :manipulators (into
                                                                   condis
                                                                   (:manipulators (profile @multi-profile-rules)))}
                                                   (profile @multi-profile-rules)))))))]
    @multi-profile-rules))

(defn parse-mains
  "parse main section to final edn format, ready to convert to json"
  [mains]
  ;; TODO: change it to the clojure way (don't use mutable data)
  (reset! multi-profile-rules {})
  (reset! profile-layer-condis {})
  (generate mains))
