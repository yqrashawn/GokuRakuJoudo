(ns karabiner-configurator.froms-test
  (:require
   [clojure.test :as t]
   [karabiner-configurator.data :refer :all]
   [karabiner-configurator.froms :as sut]))

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
               :afterup [{:set ["haha" 1]}]
               :dorder :insensitive}
        :modi :left_command}
   :12 {:key :!CSd}})

(def result
     {:applications {},
      :tos {},
      :input-source {},
      :modifiers {:1 {:mandatory ["left_command" "right_shift"],
                      :optional ["any"]}},
      :simlayer-threshold 250,
      :devices {},
      :layers {},
      :froms {:12 {:key_code "d",
                   :modifiers {:mandatory ["left_command" "left_shift"]}},
              :11 {:modifiers {:mandatory ["left_command"]},
                   :simultaneous [{:key_code "a"}
                                  {:key_code "b"}],
                   :simultaneous_options {:detect_key_down_uninterruptedly true,
                                          :key_down_order "insensitive",
                                          :key_up_order "insensitive",
                                          :key_up_when "any",
                                          :to_after_key_up [{:set_variable {:name "haha", :value 1}}]}},
              :10 {:modifiers {:mandatory ["left_command"]},
                   :simultaneous [{:key_code "a"}
                                  {:key_code "b"}],
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
      :simlayers {}})


;; (sut/generate example-froms)
(t/deftest convert-froms
  (init-conf-data)
  (update-conf-data (assoc conf-data :modifiers {:1 {:mandatory ["left_command", "right_shift"]
                                                     :optional ["any"]}}))
  (t/testing
   (t/is (= (sut/generate example-froms) result))))
