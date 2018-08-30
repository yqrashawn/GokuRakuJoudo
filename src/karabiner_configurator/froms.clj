(ns karabiner-configurator.froms
  (:require [karabiner-configurator.misc :refer :all]
            [karabiner-configurator.data :refer :all]
            [karabiner-configurator.modifiers :as kmodifier]))

;; this file parse from event definition
;; spec: https://pqrs.org/osx/karabiner/json.html#from-event-definition

;; user can define froms in advance and use it in `layers` or `rules`
;; edn_keyword | json_spec
;;
;;             | {
;; :key        |     "key_code": "The name of key_code",
;; :ckey       |     "consumer_key_code": "The name of consumer_key_code",
;; :pkey       |     "pointing_button": "The name of pointing_button",
;; :any        |     "any": "key_code or consumer_key_code or pointing_button",
;;
;; :modi       |     "modifiers": {
;;             |         "mandatory": [
;;             |             modifier,
;;             |             modifier,
;;             |             ...
;;             |         ],
;;             |         "optional": [
;;             |             modifier,
;;             |             modifier,
;;             |             ...
;;             |         ]
;;             |     },
;;
;; :sim        |     "simultaneous": [
;; use [] for  |         {
;; sim key     |            "key_code, consumer_key_code, pointing_button or any"
;;             |         },
;;             |         {
;;             |            "key_code, consumer_key_code, pointing_button or any"
;;             |         },
;;             |         ...
;;             |     ],
;; :simo       |     "simultaneous_options": {                                       default
;; :interrupt  |         "detect_key_down_uninterruptedly": false,                 |  false
;; :dorder     |         "key_down_order": "A restriction of input events order",  |  :insensitive
;; :uorder     |         "key_up_order": "A restriction of input events order",    |  :insensitive
;; :upwhen     |         "key_up_when": "When key_up events are posted",           |  :any
;; :afterup    |         "to_after_key_up": [
;;             |             to event definition,
;;             |             to event definition,
;;             |             ...
;;             |         ]
;;             |     }
;;             | }

(def any-key-keywords {:consumer_key_code {}
                       :pointing_button {}
                       :key_code {}})

(def simo-keywords
  "keyword while parsing froms, fisrt in vactor is the default value"
  {:interrupt {:values [false true]
               :json-values [false true]
               :name :detect_key_down_uninterruptedly}
   :uorder {:values [:insensitive :strict :strict_inverse]
            :json-values ["insensitive" "strict" "strict_inverse"]
            :name :key_up_order}
   :dorder {:values [:insensitive :strict :strict_inverse]
            :json-values ["insensitive" "strict" "strict_inverse"]
            :name :key_down_order}
   :upwhen {:values [:any :all]
            :json-values ["any" "all"]
            :name :key_up_when}})

(defn parse-keycode [keycode]
  (assert (from-k? keycode) (str "invalid key code " keycode " can't be used in from"))
  keycode)

(defn parse-ckey [ckey]
  (assert (consumer-k? ckey) (str "invalid consumer key code " ckey))
  ckey)

(defn parse-pkey [pkey]
  (assert (pointing-k? pkey) (str "invalid pointing key code " pkey))
  pkey)

(defn parse-key
  [fname finfo]
  (let [{:keys [key ckey pkey any modi]} finfo
        result {}
        result (if (and (nn? modi) (not (keyword? modi)))
                 (assoc result :modifiers (kmodifier/parse-single-modifier-defination modi fname))
                 result)
        result (if (keyword? modi)
                 (do
                   (cond (modifier-k? modi)
                         (assoc result :modifiers (kmodifier/parse-single-modifier-defination modi fname))
                         (contains?? (:modifiers conf-data) modi)
                         (assoc result :modifiers (modi (:modifiers conf-data)))
                         :else
                         (assert (and (modifier-k? modi) (contains?? (:modifiers conf-data) modi))
                                 (str "invalid modifier keyword " modi ", neither defined in modifiers, nor a modifer key keyword"))))
                 result)

        result (if (nn? key) (assoc result :key_code (name (parse-keycode key))) result)
        result (if (nn? ckey) (assoc result :consumer_key_code (name (parse-ckey ckey))) result)
        result (if (nn? pkey) (assoc result :pointing_button (name (parse-pkey pkey))) result)
        result (if (and (nn? any) (any any-key-keywords)) (assoc result :any (name any-key-keywords)) result)]
    result))

(defn parse-keycode-vec
  [vec]
  (assert (vector? vec) (str "invalid vector " vec))
  (into []
        (for [v vec]
          (do (assert (from-k? v) (str "keycode " v " can't be used as from keycode"))
              (name v)))))

(defn parse-simo
  [sim simo simo-op-value simo-op-keyword result]
  (if (and (nn? sim) (nn? simo-op-value))
    (do (assert (or (contains?? [true false] simo-op-value)
                    (and (keyword? simo-op-value)
                         (contains?? (:values (simo-op-keyword simo-keywords) ) simo-op-value)))
                (str "invalid detect_key_down_uninterruptedly keyword " simo-op-value))
        (if (keyword? simo-op-value) ;; there're true false
          (assoc-in result [:simultaneous_options (:name (simo-op-keyword simo-keywords))] (name simo-op-value))
          (assoc-in result [:simultaneous_options (:name (simo-op-keyword simo-keywords))] simo-op-value)))
    (if (nn? sim)
      (assoc-in result [:simultaneous_options (:name (simo-op-keyword simo-keywords)) ] (first (:json-values (simo-op-keyword simo-keywords))))
      result)))

(defn parse-sim
  [fname finfo prevresult]
  (let [result prevresult
        {:keys [sim simo]} finfo
        result (if (nn? sim) (assoc result :simultaneous (parse-keycode-vec sim)) result)
        {:keys [interrupt dorder uorder upwhen]} simo
        result (parse-simo sim simo interrupt :interrupt result)
        result (parse-simo sim simo dorder :dorder result)
        result (parse-simo sim simo uorder :uorder result)
        result (parse-simo sim simo upwhen :upwhen result)]
    result))

;; TODO parse to_after_key_up

(defn parse-from
  [fname finfo]
  (let [{:keys [sim simo]} finfo
        result (parse-key fname finfo)
        result (if (or sim simo) (parse-sim fname finfo result) result)]
    result))

(defn generate [froms]
  (assoc conf-data :froms
         (into
          {}
          (for [[fname finfo] froms]
            {fname
             (do
               (assert (map? finfo) (str "invalid from defination in " fname ", must be a map"))
               (parse-from fname finfo))}))))

(defn parse-froms [froms]
  (if (nn? froms)
    (update-conf-data (generate froms))))