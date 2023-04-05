(ns karabiner-configurator.keys-symbols-test
  (:require [clojure.test                        :as t]
            [karabiner-configurator.rules        :as rules]
            [karabiner-configurator.keys-symbols :as sut]))

;; convert symbols
(def symbol-map {
  :⇧k   	:!SSk,
  :‹⇧k  	:!Sk,
  :⇧›k  	:!Rk,
  :⎇k   	:!OOk,
  :‹⌘k  	:!Ck,
  :⎇⇧b  	:!OO!SSb,
  :‹⎈##3	:!T##3
  :🌐    	:!F
})

;; convert modifiers
(def modi-map {
  :!OO!SSb     	:!OOSSb,
  :!CC!TT      	:!CCTT,
  :!CC!AA#BB!TT	:!CCAATT#BB,
  :!!##i       	:!!##i,
  :!CC!TT#CC#TT :!CCTT#CCTT
})


;; convert keys with modifiers
(def key-modi-map {
  :⇧›1              	:!R1
  :⎈semicolon       	:!TTsemicolon
  :⎈quote           	:!TTquote
  :⎈spacebar        	:!TTspacebar
  :◆›z              	:!Qz
  :◆›period         	:!Qperiod
  :Ⓕnon_us_backslash	:!Fnon_us_backslash
  :‹◆d              	:!Cd
  :⇧‹◆f10           	:!SSCf10
  :⎇›⇧              	:!Eshift
  :⎇›⇪              	:!Ecaps_lock
  :⇧⁄               	:!SSslash
  :⇧〔               	:!SSopen_bracket
  :⇧【               	:!SSopen_bracket
  :⇧「               	:!SSopen_bracket
  :⇧‐               	:!SShyphen
  :⇧﹨               	:!SSnon_us_backslash
  :⇧⧵               	:!SSbackslash
  :⇧＼               	:!SSbackslash
  ; more complicated cases in vertically aligned pairs
  [:w {:set ["␠w" 1]  :repeat false} [:!␠w       ]] ; skip conversion of variable values
  [:w {:set ["␠w" 1], :repeat false} [:!spacebarw]]
  [:⁄     [:say_␠h :repeat false]] ; skip conversion of user functions with _escaped symbols
  [:slash [:say_␠h :repeat false]]
})

;; convert keys as strings
(def key-string-map {
  "‘  	 ⇧ 	  	 	k"	:!SSk,
  "‘  	‹⇧ 	  	 	k"	:!Sk,
  "‘  	 ⇧›	  	 	k"	:!Rk,
  "‘  	   	  	⎇	k"	:!OOk,
  "‘  	   	‹⌘	 	k"	:!Ck,
  "‘  	 ⇧ 	  	⎇	k"	:!SSOOk,
  "‘‹⎈	   	  	 	k"	:!Tk
})

;; convert map of keys with modifiers
(def map-key-modi-map {
  {:key :⎈›a, :halt true}   	{:key :!Wa, :halt true}
  {:sim[:₌ :␈]}             	{:sim [:equal_sign :delete_or_backspace]},
  {:sim[:₌ :␈] :modi :any}  	{:sim [:equal_sign :delete_or_backspace] :modi :any}
  {:sim["‘₌" :␈] :modi :any}	{:sim [:equal_sign :delete_or_backspace] :modi :any}
})

(t/deftest convert-symbols
  (t/testing "convert symbols"
    (doseq [[k v] symbol-map]
      (t/is (= (sut/key-name-sub-or-self k) v))))

  (t/testing "move mandatory/optional prefix from individual modifiers to a group"
    (doseq [[k v] modi-map]
      (t/is (= (sut/move-modi-prefix-front k) v))))

  (t/testing "convert key with symbol modifiers to regular keys"
    (doseq [[k v] key-modi-map]
      (t/is (= (sut/key-sym-to-key k) v))))

  (t/testing "convert map with :key with symbol modifiers to regular keys"
    (doseq [[k v] map-key-modi-map]
      (t/is (= (sut/key-sym-to-key k) v))))

  (t/testing "convert map with 'keys as strings'"
    (doseq [[k v] key-string-map]
      (t/is (= (sut/key-sym-to-key k) v))))
  )
