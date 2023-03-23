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
(def keys-symbols (merge keys-symbols-generated keys-symbols-other))
