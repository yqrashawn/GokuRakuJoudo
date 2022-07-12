(ns karabiner-configurator.keys
  (:require
   [clojure.string :refer [includes? join]]
   [karabiner-configurator.data :as d]
   [karabiner-configurator.misc :refer [contains?? massert]]
   [karabiner-configurator.modifiers :as kmodifier]))

(def any-key-keywords {:consumer_key_code {}
                       :pointing_button {}
                       :key_code {}})

(defn parse-keycode [keycode & [from-to]]
  (if from-to
    (massert (d/to-k? keycode) (str "invalid key code " keycode " can't be used in from"))
    (massert (d/from-k? keycode) (str "invalid key code " keycode " can't be used in from")))
  keycode)

(defn parse-ckey [ckey]
  (massert (d/consumer-k? ckey) (str "invalid consumer key code " ckey))
  ckey)

(defn parse-pkey [pkey]
  (massert (d/pointing-k? pkey) (str "invalid pointing key code " pkey))
  pkey)

(defn update-mouse-map
  [[k v]]
  [(d/mouse-key-name k) v])

(defn parse-mkey
  [mkey]
  (into {} (map update-mouse-map mkey)))

(def special-modi-re #"(^![!ACSTOQWERFP]+#[#CSTOQWERFP]+)")
(def special-modi-mandatory-re #"(^![!ACSTOQWERFP]+)")
(def special-modi-optional-re #"(^#[#CSTOQWERFP]+)")
(def special-modi-optional-both-re #"(#[#CSTOQWERFP]+)")

;; (defn special-modi-realkey [smodi]
;;   (let [keystr (name smodi)
;;         both (re-find special-modi-re keystr)
;;         mandatory (if both
;;                     (re-find special-modi-mandatory-re (first both))
;;                     (re-find special-modi-mandatory-re keystr))
;;         optional (if both
;;                    (re-find special-modi-optional-both-re (first both))
;;                    (re-find special-modi-optional-re keystr))
;;         validate (massert (or both mandatory optional)
;;                           (str "invalid special modifier keyword " smodi))
;;         mandatory (if mandatory (first mandatory) nil)
;;         optional (if optional (first optional) nil)
;;         realkey (cond both
;;                       (keyword (subs keystr (count (first both))))
;;                       (and mandatory (not both))
;;                       (keyword (subs keystr (count mandatory)))
;;                       (and optional (not both))
;;                       (keyword (subs keystr (count optional))))]
;;     realkey))

(defn special-modi-vector-to-modifiers
  [vec]
  (if (vector? vec)
    (let [result []
          s      (join "" vec)
          result (cond
                   (includes? s "CC")
                   (conj result (name :command))
                   (contains?? vec \C)
                   (conj result (name :left_command))
                   :else
                   result)
          result (cond
                   (includes? s "TT")
                   (conj result (name :control))
                   (contains?? vec \T)
                   (conj result (name :left_control))
                   :else
                   result)
          result (cond
                   (includes? s "OO")
                   (conj result (name :option))
                   (contains?? vec \O) (conj result (name :left_option))
                   :else
                   result)
          result (cond
                   (includes? s "SS")
                   (conj result (name :shift))
                   (contains?? vec \S)
                   (conj result (name :left_shift))
                   :else
                   result)
          result (if (contains?? vec \Q) (conj result (name :right_command)) result)
          result (if (contains?? vec \W) (conj result (name :right_control)) result)
          result (if (contains?? vec \E) (conj result (name :right_option)) result)
          result (if (contains?? vec \R) (conj result (name :right_shift)) result)
          result (if (contains?? vec \F) (conj result (name :fn)) result)
          result (if (contains?? vec \P) (conj result (name :caps_lock)) result)
          result (if (contains?? vec \#) [(name :any)] result)
          result (cond
                   (includes? s "!A")
                   [(name :command)
                    (name :control)
                    (name :option)
                    (name :shift)]
                   (contains?? vec \!)
                   [(name :left_command)
                    (name :left_control)
                    (name :left_option)
                    (name :left_shift)]
                   :else
                   result)]
      result)
    nil))

(defn parse-special-modi
  [smodi prevresult & [mandatory-only]]
  (let [result prevresult
        keystr (name smodi)
        both (re-find special-modi-re keystr)
        mandatory (if both
                    (re-find special-modi-mandatory-re (first both))
                    (re-find special-modi-mandatory-re keystr))
        optional (if both
                   (re-find special-modi-optional-both-re (first both))
                   (re-find special-modi-optional-re keystr))
        _validate (massert (or both mandatory optional)
                           (str "invalid special modifier keyword " smodi))
        mandatory (if mandatory (first mandatory) nil)
        optional (if optional (first optional) nil)
        realkey (cond both
                      (keyword (subs keystr (count (first both))))
                      (and mandatory (not both))
                      (keyword (subs keystr (count mandatory)))
                      (and optional (not both))
                      (keyword (subs keystr (count optional))))
        _validate-realkey (massert (d/k? realkey)
                                   (str "invalid special key keyword " smodi ", no key " realkey))
        result (assoc result :key_code (name realkey))
        mandatory (if mandatory (into [] (subs mandatory 1)) nil)
        optional (if optional (into [] (subs optional 1)) nil)
        result (if (and (not (true? mandatory-only)) mandatory) (assoc-in result [:modifiers :mandatory] (special-modi-vector-to-modifiers mandatory)) result)
        result (if (and (true? mandatory-only) mandatory) (assoc result :modifiers (special-modi-vector-to-modifiers mandatory)) result)
        result (if (and (not (true? mandatory-only)) optional) (assoc-in result [:modifiers :optional] (special-modi-vector-to-modifiers optional)) result)]
    result))

(defn parse-key
  "this function is used in froms and tos for parsing key ckey pkey modi any"
  [kname kinfo & [mandatory-only from-to]]
  (let [{:keys [key ckey pkey any modi mkey]} kinfo
        result {}
        special-modi? (d/special-modi-k? key)
        _both-special-modi-and-modi? (massert (not (and special-modi? (some? modi))) (str "can't use special modi and modi togeher, check " kname))
        predefined-modi? (and (keyword? modi) (contains?? (:modifiers @d/conf-data) modi))
        _valid-modifier-definition? (massert (or (nil? modi) (map? modi)
                                                 (vector? modi) (d/modifier-k? modi)
                                                 predefined-modi?)
                                             (str "invalid modifier definition " modi " in " kname))
        _valid-any-keyword? (when any (massert (any any-key-keywords) (str "invalid :any keyword " (name any) " in " kname)))
        mandatory-only? (true? mandatory-only)
        result (if special-modi? (parse-special-modi key result mandatory-only) result)
        result (if (and (not special-modi?) (some? modi))
                 (if (not predefined-modi?)
                   (if mandatory-only?
                     (assoc result :modifiers (:mandatory (kmodifier/parse-single-modifier-definition modi kname)))
                     (assoc result :modifiers (kmodifier/parse-single-modifier-definition modi kname)))
                   (if mandatory-only?
                     (assoc result :modifiers (:mandatory (modi (:modifiers @d/conf-data))))
                     (assoc result :modifiers (modi (:modifiers @d/conf-data)))))
                 result)
        result (if (and (not (:key_code result)) (some? key)) (assoc result :key_code (name (parse-keycode key from-to))) result)
        result (if (some? ckey) (assoc result :consumer_key_code (name (parse-ckey ckey))) result)
        result (if (some? pkey) (assoc result :pointing_button (name (parse-pkey pkey))) result)
        result (if (and (some? any) (any any-key-keywords)) (assoc result :any (name any)) result)
        result (if (some? mkey) (assoc result :mouse_key (parse-mkey mkey)) result)]
    result))
