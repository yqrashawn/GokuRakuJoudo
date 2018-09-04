(ns karabiner-configurator.tos
  (:require [karabiner-configurator.misc :refer :all]
            [karabiner-configurator.data :refer :all]
            [karabiner-configurator.keys :refer :all]
            [karabiner-configurator.modifiers :as kmodifier]
            [karabiner-configurator.tos-test]))

;; parse tos defination
;; spec https://pqrs.org/osx/karabiner/json.html#to-event-definition

;;                {
;; :key               "key_code": "The name of key_code",
;; :ckey              "consumer_key_code": "The name of consumer_key_code",
;; :pkey              "pointing_button": "The name of pointing_button",
;;
;; :shell             "shell_command": "shell command",
;;
;; :input             "select_input_source": {
;;                        "language": "language regex",
;;                        "input_source_id": "input source id regex",
;;                        "input_mode_id": "input mode id regex"
;;                    },
;;
;; :set               "set_variable": {
;; ["name","value"]       "name": "variable name",
;;                        "value": "variable value"
;;                    },
;;
;; :mkey              "mouse_key": {
;;   :x                   "x": -1546,                  move left
;;                        "x": 1546,                   move right
;;   :y                   "y": -1536,                  move up
;;                        "y": 1536,                   move down
;;   :hwheel              "horizontal_wheel": 32,      scroll left
;;                        "horizontal_wheel": -32,     scroll right
;;   :vwheel              "vertial_wheel": -32,        scroll up
;;                        "vertial_wheel": 32,         scroll down
;;                        "x": -3072,                  fast move left
;;                        "x": 3072,                   fast move right
;;                        "y": -3072,                  fast move up
;;                        "y": 3072,                   fast move down
;;                        "horizontal_wheel": 64,      fast scroll left
;;                        "horizontal_wheel": -64,     fast scroll right
;;                        "vertical_wheel": -64,       fast scroll up
;;                        "vertical_wheel": 64,        fast scroll down
;;   :speed               "speed_multiplier": 2.0,     speed multiplier x2
;;                        "speed_multiplier": 0.5,     speed multiplier /2
;;                    },
;;
;; :modi              "modifiers": [
;;                        modifier,
;;                        modifier,
;;                        ...
;;                    ],
;;
;; :lazy              "lazy": false,
;; :repeat            "repeat": true,
;; :halt              "halt": false,
;; :hold_down_ms      "hold_down_milliseconds": 0
;;                }

(defn parse-to
  [tname tinfos]
  (for [tinfo tinfos]
   (let [{:keys [set input shell lazy repeat halt hold_down_ms]} tinfo
         result (parse-key tname tinfo true)
         validate-shell (assert (or (string? shell) (nil? shell))
                                (str "invalid `shell` in to defination " tname " " shell ", should be a string"))
         validate-input (assert (or (nil? input) (and (keyword? input) (contains? (:input-source conf-data) input)))
                                (str "invalid `input` in to defination " tname " " input ", should be a keyword"))
         validate-set (assert (or (vector? set) (nil? set))
                              (str "invalid `set` in to defination " tname " " set ", should be a vector"))
         result (if (keyword? input)
                  (assoc result :select_input_source (input (:input-source conf-data)))
                  result)
         result (if (string? shell)
                  (assoc result :shell_command shell)
                  result)
         result (if (vector? set)
                  (assoc result :set_variable {:name (first set) :value (second set)})
                  result)
         result (if (false? repeat)
                  (assoc result :repeat false)
                  result)
         result (if (true? halt)
                  (assoc result :halt true)
                  result)
         result (if (and (number? hold_down_ms) (not (= 0 hold_down_ms)))
                  (assoc result :hold_down_milliseconds hold_down_ms)
                  result)
         result (if (true? lazy)
                  (assoc result :lazy true)
                  result)]
     result)))

(defn generate [tos]
  (assoc conf-data :tos
         (into
          {}
          (for [[tname tinfo] tos]
            {tname
             (do
               (assert (or (vector? tinfo) (map? tinfo))
                       (str "invalid to defination in " tname ", must be map or vector"))
               (if (not (vector? tinfo))
                 (into [] (parse-to tname [tinfo]))
                 (into [] (parse-to tname tinfo))))}))))

(defn parse-tos [tos]
  (if (nn? tos)
    (update-conf-data (generate tos))))