; Add various key symbols to be used instead of capitalized letters
(ns karabiner-configurator.keys-symbols
  (:require
   [clojure.string :as string]
   [clojure.core.match :refer [match]]))

; S T O C → left_shift  left_control  left_option  left_command
; R W E Q → right_shift right_control right_option right_command
; SS      → shift ...
; labels must = same position
(def modi-sym	["⇧" 	 "⎈" 	"⌃" 	 "⎇" 	"⌥" 	 "⌘" 	"◆" ])
(def modi-‹l 	["S" 	 "T" 	"T" 	 "O" 	"O" 	 "C" 	"C" ])
(def modi-l› 	["R" 	 "W" 	"W" 	 "E" 	"E" 	 "Q" 	"Q" ])
(def modi-l∀ 	["SS"	 "TT"	"TT"	 "OO"	"OO"	 "CC"	"CC" ])
(def ‹key    	["‹" "'"])
(def key›    	["›" "'"])
(def key﹖    	["﹖" "?"])
(def keys-symbols-other {
  "🌐" 	"!F","ƒ""!F","ⓕ""!F","Ⓕ""!F","🄵""!F","🅕""!F","🅵""!F"
  "⇪" 	"P"          	; capslock
  "∀" 	"!A"         	; any regardless of side
  "✱" 	"!!"         	; hyper
  "﹖﹖"	"##","??""##"	; optional any
  "︔" 	"semicolon"
  "⸴" 	"comma"
  "⎋" 	"escape"
  "⭾" 	"tab"
  "␠" 	"spacebar"
  "␣" 	"spacebar"
  "␈" 	"delete_or_backspace"
  "⌫" 	"delete_or_backspace"
  "⏎" 	"return_or_enter"
  "▲" 	"up_arrow"
  "▼" 	"down_arrow"
  "◀" 	"left_arrow"
  "▶" 	"right_arrow"
  " " 	"" ; no-break space removed, used only for rudimentary alignment
})
(def keys-symbols-generated (into {} (
  mapcat      (fn [mod]
    (mapcat   (fn [‹]
      (mapcat (fn [›]
        (map  (fn [﹖]
        (def mI	(.indexOf modi-sym mod)) ; modifier index to pick labels (which have the same position)
        (def ‹l	(nth modi-‹l mI))
        (def l›	(nth modi-l› mI))
        (def l∀	(nth modi-l∀ mI))
        (match [ ; !mandatory ; #optional
          (some? ‹) (some? ›) (some? ﹖)]
          [true      false     false]	{(str ‹ mod    ) 	(str "!" ‹l)}
          [false     true      false]	{(str   mod ›  ) 	(str "!" l›)}
          [false     false     false]	{(str   mod    ) 	(str "!" l∀)}
          [true      false     true] 	{(str ‹ mod   ﹖ )	(str "#" ‹l)}
          [false     true      true] 	{(str   mod › ﹖ )	(str "#" l›)}
          [false     false     true] 	{(str   mod   ﹖ )	(str "#" l∀)}
          :else                      	nil
         )) (concat key﹖ '(nil))
         )) (concat key› '(nil))
         )) (concat ‹key '(nil))
  ))                modi-sym))
)
(def keys-symbols-unordered (merge keys-symbols-generated keys-symbols-other))
; Sort by key length (BB > A) to match ⇧› before ⇧
(defn sort-map-key-len
  ([m    ] (sort-map-key-len m "asc"))
  ([m ord] (into
  (sorted-map-by (fn [key1 key2] (
    compare
      (if (or (= ord "asc") (= ord "↑")) [(count (str key1)) key1] [(count (str key2)) key2])
      (if (or (= ord "asc") (= ord "↑")) [(count (str key2)) key2] [(count (str key1)) key1])
  ))) m)))
(def keys-symbols (sort-map-key-len keys-symbols-unordered "↓"))

(defn replace-map-h "input string + hash-map ⇒ string with all map-keys → map-values in input"
  [s_in m_in]
  (def keys_in     	(keys m_in))                                          	;{"⎇""A","⎈""C"} → "⎇""⎈"
  (def keys_in_q   	(map #(java.util.regex.Pattern/quote %) keys_in))     	;"\\Q⎇\\E"   "\\Q⎈\\E"
  (def keys_in_q_or	(interpose "|"                          keys_in_q))   	;"\\Q⎇\\E""|""\\Q⎈\\E"
  (def keys_in_q_s 	(apply str                              keys_in_q_or))	;"\\Q⎇\\E|\\Q⎈\\E"
  (def keys_in_re  	(re-pattern                             keys_in_q_s)) 	;#"\Q⎇\E|\Q⎈\E"
  (string/replace s_in keys_in_re m_in))

(defn key-name-sub-or-self [k]
  (if (keyword? k)
    (keyword (string/replace (replace-map-h k keys-symbols) #"^:" ""))
    k
  ))
