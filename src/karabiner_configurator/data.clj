(ns karabiner-configurator.data
  (:require
   [karabiner-configurator.keys-info :refer [keys-info]]
   [karabiner-configurator.misc :refer [contains?? massert]]))

(def rule-id (atom nil))

(defn init-rule-id []
  (reset! rule-id -1))

;; (defn next-rule-id
;;   "return the next rule id

;;    store each rule in a vector and use the index as their id
;;    the data structure will also store conditions used in the rule
;;    so that we know what condition used in each profile"
;;   []
;;   (swap! rule-id inc)
;;   @rule-id)

(def conf-data (atom {}))
(def user-default-profile-name (atom :Default))

(defn init-conf-data
  []
  (init-rule-id)
  (reset! user-default-profile-name :Default)
  (reset! conf-data {:profiles {:Default {:sim 50 ;; basic.simultaneous_threshold_milliseconds
                                          :delay 500 ;; basic.to_delayed_action_delay_milliseconds
                                          :alone 1000 ;; basic.to_if_alone_timeout_milliseconds
                                          :held 500 ;; basic.to_if_held_down_threshold_milliseconds
                                          :default true}}
                     :applications {}
                     :devices {}
                     :input-sources {}
                     :modifiers {}
                     :froms {}
                     :tos {}
                     :layers {}
                     :simlayers {}
                     :simlayer-threshold 250}))

;; (defn applications? [k] (some? (k (:applications @conf-data))))
(defn devices? [k] (some? (k (:devices @conf-data))))
(defn input-sources? [k] (some? (k (:input-sources @conf-data))))
;; (defn modifiers? [k] (some? (k (:modifiers @conf-data))))
;; (defn froms? [k] (some? (k (:froms @conf-data))))
;; (defn layers? [k] (some? (k (:layers @conf-data))))
(defn simlayers? [k] (some? (k (:simlayers @conf-data))))
(defn templates? [k-or-vec]
  (cond (keyword? k-or-vec)
        (contains? (:templates @conf-data) k-or-vec)
        (vector? k-or-vec)
        (contains? (:templates @conf-data) (first k-or-vec))))
(defn noti? [vec]
  (and (vector? vec)
       (let [[k id text] vec]
         (and (= k :noti)
              (or (keyword? id)
                  (string? id))
              (or (string? text)
                  (keyword? text)
                  (nil? text))))))
(defn softf? [vec]
  (and (vector? vec)
       (let [[k m] vec]
         (and (= k :softf)
              (map? m)
              (or (map? (:open_application m))
                  (map? (:set_mouse_cursor_position m))
                  (map? (:cg_event_double_click m))
                  (map? (:iokit_power_management_sleep_system m))
                  (map? (:open m))
                  (map? (:setmpos m))
                  (map? (:dbc m))
                  (map? (:sleep m)))))))
(defn op? [vec]
  (and (vector? vec)
       (let [[k p] vec]
         (and (= k :op)
              (string? p)))))
(defn oi? [vec]
  (and (vector? vec)
       (let [[k i] vec]
         (and (= k :oi)
              (int? i)))))

(defn oid? [vec]
  (and (vector? vec)
       (let [[k i] vec]
         (and (= k :oid)
              (string? i)))))

(defn raw-rule? [rule]
  (and (map? rule)
       (or (= :basic (:type rule))
           (= :mouse_motion_to_scroll (:type rule)))))

(defn profile? [k]
  (and (keyword? k) (k (:profiles @conf-data))))

;; (def default-profile {:Default {:sim 50 ;; basic.simultaneous_threshold_milliseconds
;;                                 :delay 500 ;; basic.to_delayed_action_delay_milliseconds
;;                                 :alone 1000 ;; basic.to_if_alone_timeout_milliseconds
;;                                 :held 500 ;; basic.to_if_held_down_threshold_milliseconds
;;                                 :default true}})

(defn update-user-default-profile-name [profile-name]
  (massert
   (keyword? profile-name)
   (str "invalid profile name " profile-name ", profile name must be a keyword"))
  (reset! user-default-profile-name profile-name))

(defn pkey?
  [pkeymap]
  (and (map? pkeymap)
       (->> pkeymap
            keys
            first
            (= :pkey))
       (->> pkeymap
            first
            second
            (get keys-info)
            (:button)
            true?)))

(defn k?
  [k]
  (when (keyword? k)
    (some? (k keys-info))))

(defn modifier-k?
  [k]
  (k? k)
  (true? (:modifier (k keys-info))))

(defn from-k?
  [k]
  (k? k)
  (nil? (:not-from (k keys-info))))

(defn to-k?
  [k]
  (k? k)
  (nil? (:not-to (k keys-info))))

(defn consumer-k?
  [k]
  (k? k)
  (true? (:consumer-key (k keys-info))))

(defn pointing-k?
  [k]
  (k? k)
  (true? (:button (k keys-info))))

(def mkey-keyword {:x {:name :x}
                   :y {:name :y}
                   :vwheel {:name :vertical_wheel}
                   :hwheel {:name :horizontal_wheel}
                   :speed {:name :speed_multiplier}})

(defn mouse-keyword?
  [k]
  (some? (k mkey-keyword)))

(defn mouse-key-name
  [k]
  (massert (mouse-keyword? k) (str "invalid mouse key keyword " k))
  (:name (k mkey-keyword)))

(defn special-modi-k? [k]
  (when (keyword? k)
    (contains?? [\! \#] (first (name k)))))

;; (defn find-condition-keyword
;;   [kw]
;;   (cond (contains? @conf-data :applications)
;;         {:name :application
;;          :value (kw (:applications @conf-data))}
;;         (contains? @conf-data :devices)
;;         {:name :devices
;;          :value (kw (:devices @conf-data))}
;;         (contains? @conf-data :input-sources)
;;         {:name :input-sources
;;          :value (kw (:input-sources @conf-data))}
;;         (contains? @conf-data :simlayers)
;;         {:name :simlayers
;;          :value (kw (:simlayers @conf-data))}))

(defn update-conf-data
  [data]
  (reset! conf-data data))

(defn assoc-conf-data
  [key data]
  (swap! conf-data assoc key data))

(defn assoc-in-conf-data
  [keys-vector data]
  (swap! conf-data assoc-in keys-vector data))

;; (def output "output data that will convert into json string" [])
