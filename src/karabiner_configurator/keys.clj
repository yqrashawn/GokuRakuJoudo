(ns karabiner-configurator.keys
  (:require
   [karabiner-configurator.misc :refer :all]
   [karabiner-configurator.data :refer :all]
   [karabiner-configurator.modifiers :as kmodifier]))

(def any-key-keywords {:consumer_key_code {}
                       :pointing_button {}
                       :key_code {}})

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
  "this function is used in froms and tos for parsing key ckey pkey modi any"
  [kname kinfo & [mandatory-only]]
  (let [{:keys [key ckey pkey any modi mkey]} kinfo
        result {}
        valid-modifier-defination? (assert (or (nil? modi) (map? modi)
                                               (vector? modi) (modifier-k? modi)
                                               predefined-modi?)
                                           (str "invalid modifier defination " modi))
        predefined-modi? (and (keyword? modi) (contains?? (:modifiers conf-data) modi))
        mandatory-only? (true? mandatory-only)
        result (if (nn? modi)
                 (if (not predefined-modi?)
                   (if mandatory-only?
                     (assoc result :modifiers (:mandatory (kmodifier/parse-single-modifier-defination modi kname)))
                     (assoc result :modifiers (kmodifier/parse-single-modifier-defination modi kname)))
                   (if mandatory-only?
                     (assoc result :modifiers (:mandatory (modi (:modifiers conf-data))))
                     (assoc result :modifiers (modi (:modifiers conf-data)))))
                 result)
        result (if (nn? key) (assoc result :key_code (name (parse-keycode key))) result)
        result (if (nn? ckey) (assoc result :consumer_key_code (name (parse-ckey ckey))) result)
        result (if (nn? pkey) (assoc result :pointing_button (name (parse-pkey pkey))) result)
        result (if (and (nn? any) (any any-key-keywords)) (assoc result :any (name any-key-keywords)) result)]
    result))