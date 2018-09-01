(ns karabiner-configurator.froms-test
  (:require
   [clojure.test :as t]
   [karabiner-configurator.data :refer :all]
   [karabiner-configurator.froms :as sut]))

(init-conf-data)

(update-conf-data (assoc conf-data :modifiers {:1 {:mandatory ["left_command", "right_shift"]
                                                   :optional ["any"]}}))

(def example-froms
  {:1 {:key :d}
   :2 {:key :d :modi :1}
   :3 {:key :d :modi :left_command}
   :4 {:key :d :modi [:left_command :right_shift]}
   :5 {:key :d :modi {:mandatory [:left_command :right_shift]}}
   :6 {:key :d :modi {:mandatory [:left_command :right_shift]
                      :optional [:caps_lock]}}
   :7 {:ckey :display_brightness_decrement}
   :8 {:ckey :display_brightness_decrement :modi :left_command}
   :9 {:pkey :button4 :modi :left_command}
   :10 {:sim [:a :b] :modi :left_command}
   :11 {:sim [:a :b]
        :simo {:interrupt true
               :dorder :insensitive}
        :modi :left_command}})

(def result
 {:applications {},
  :tos {},
  :swaps {},
  :input-source {},
  :modifiers {:1 {:mandatory ["left_command" "right_shift"],
                  :optional ["any"]}},
  :devices {},
  :layers {},
  :froms {:11 {:modifiers {:mandatory ["left_command"]},
               :simultaneous ["a" "b"],
               :simultaneous_options {:detect_key_down_uninterruptedly true,
                                      :key_down_order "insensitive",
                                      :key_up_order "insensitive",
                                      :key_up_when "any"}},
          :10 {:modifiers {:mandatory ["left_command"]},
               :simultaneous ["a" "b"],
               :simultaneous_options {:detect_key_down_uninterruptedly false,
                                      :key_down_order "insensitive",
                                      :key_up_order "insensitive",
                                      :key_up_when "any"}},
          :4 {:modifiers {:mandatory ["left_command" "right_shift"]},
              :key_code "d"},
          :7 {:consumer_key_code "display_brightness_decrement"},
          :1 {:key_code "d"},
          :8 {:modifiers {:mandatory ["left_command"]},
              :consumer_key_code "display_brightness_decrement"},
          :9 {:modifiers {:mandatory ["left_command"]},
              :pointing_button "button4"},
          :2 {:modifiers {:mandatory ["left_command" "right_shift"],
                          :optional ["any"]},
              :key_code "d"},
          :5 {:modifiers {:mandatory ["left_command" "right_shift"]},
              :key_code "d"},
          :3 {:modifiers {:mandatory ["left_command"]},
              :key_code "d"},
          :6 {:modifiers {:mandatory ["left_command" "right_shift"],
                          :optional ["caps_lock"]},
              :key_code "d"}},
  :raws {},
  :simlayers {}})

;; (sut/generate example-froms)
(t/deftest convert-froms
  (t/testing
      (t/is (= (sut/generate example-froms) result))))