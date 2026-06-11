(ns karabiner-configurator.tos
  (:require [clojure.set :as cset]
            [karabiner-configurator.data :refer [conf-data update-conf-data]]
            [karabiner-configurator.keys :refer [parse-key]]
            [karabiner-configurator.misc :refer [massert]]))

;; parse tos definition
;; spec https://karabiner-elements.pqrs.org/docs/json/complex-modifications-manipulator-definition/to/

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
;; :noti              "set_notification_message" : {
;;                        "id": "identifier of the message",
;;                        "text": "message text"
;;                    }
;; :sticky            "sticky_modifier": {
;;                        "{modifier_name}": "on | off | toggle"
;;                    }

(defn parse-to
  [tname tinfos]
  (mapv
   (fn [{:keys [set input shell lazy repeat halt hold_down_ms select_input_source noti softf sticky] :as tinfo}]
     ;; validate-shell
     (massert (or (and (vector? shell)
                       (contains? (:templates @conf-data) (first shell)))
                  (string? shell) (nil? shell))
              (format
               "invalid `shell` in to definition %s %s, should be string or keyword"
               tname shell))
     ;; validate-input
     (massert (or (nil? input)
                  (and
                   (keyword? input)
                   (contains? (:input-sources @conf-data) input)))
              (format
               "invalid `input` in to definition %s %s, should be a keyword"
               tname input))
     ;; validate-set
     (massert (or (vector? set) (nil? set))
              (format "invalid `set` in to definition %s %s, should be a vector"
                      tname set))
     ;; validate-noti
     (massert (or (nil? noti)
                  (and (map? noti)
                       (or (keyword? (get noti :id))
                           (string? (get noti :id)))
                       (or (nil? (get noti :text))
                           (keyword? (get noti :text))
                           (string? (get noti :text)))))
              (str "invalid `noti`, must be a map with at least :id, :id must be string or keyword"))
     ;; validate-softf
     (massert (or (nil? softf) (map? softf))
              (str "invalid `softf`, must be a map with valid keys"))
     ;; validate-sticky
     (massert (or (nil? sticky) (map? sticky))
              (str "invalid `sticky`, must be a map with modifier keys and values"))
     (when sticky
       (let [sticky-keys (keys sticky)
             sticky-values (vals sticky)]
         (massert (= 1 (count sticky-keys))
                  (str "sticky modifier must specify exactly one modifier in " tname))
         (massert (every? #(contains? #{:left_control :left_shift :left_option :left_command
                                        :right_control :right_shift :right_option :right_command :fn} %)
                          sticky-keys)
                  (str "invalid sticky modifier key in " tname ". Must be one of: left_control, left_shift, left_option, left_command, right_control, right_shift, right_option, right_command, fn"))
         (massert (every? #(contains? #{:on :off :toggle} %)
                          sticky-values)
                  (str "invalid sticky modifier value in " tname ". Must be one of: on, off, toggle"))))
     (let [softf
           (when softf
             (cset/rename-keys softf {:dbc     :cg_event_double_click
                                      :sleep   :iokit_power_management_sleep_system
                                      :open    :open_application
                                      :setmpos :set_mouse_cursor_position}))
           result
           (cond-> (parse-key tname tinfo true true)
             (keyword? input)
             (assoc :select_input_source (input (:input-sources @conf-data)))

             (string? shell)
             (assoc :shell_command shell)

             (vector? shell)
             (assoc :shell_command
                    (apply
                     format
                     (flatten
                      [((first shell)
                        (:templates @conf-data))
                       (rest shell)
                       ;; optional arguments
                       "" "" "" "" "" ""])))

             (vector? set)
             (assoc :set_variable {:name (first set) :value (second set)})

             (false? repeat)
             (assoc :repeat false)

             (true? halt)
             (assoc :halt true)

             (and (number? hold_down_ms) (not (= 0 hold_down_ms)))
             (assoc :hold_down_milliseconds hold_down_ms)

             (boolean? lazy)
             (assoc :lazy lazy)

             noti
             (assoc :set_notification_message {:id   (:id noti)
                                               :text (or (:text noti) "")})

             (map? softf)
             (assoc :software_function softf)

             (map? sticky)
             (assoc :sticky_modifier
                    (into {} (map (fn [[k v]] [(name k) (name v)]) sticky))))]
       (if select_input_source tinfo result)))
   tinfos))

(defn generate [tos]
  (assoc @conf-data :tos
         (into
          {}
          (for [[tname tinfo] tos]
            {tname
             (do
               (massert (or (vector? tinfo) (map? tinfo))
                        (str "invalid to definition in " tname ", must be map or vector"))
               (if (not (vector? tinfo))
                 (parse-to tname [tinfo])
                 (parse-to tname tinfo)))}))))

(defn parse-tos [tos]
  (when (some? tos)
    (update-conf-data (generate tos))))
