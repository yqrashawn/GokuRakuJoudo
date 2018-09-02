(ns karabiner-configurator.rules-test
  (:require [karabiner-configurator.rules :as sut]
            [karabiner-configurator.data :refer :all]
            [clojure.test :as t]))

(def example-mains [{:des "a to 1"                                            :rules [[:a :1]]} ;; a to 1
                    {:des "command a to control 1"                            :rules [[:!Ca :!T1]]} ;; command a to control 1
                    {:des "my spacebar to control 1"                          :rules [[:my-spacebar :!T1]]} ;; my-spacebar to control 1
                    {:des "press b to insert 12"                              :rules [[:b [:1 :2]]]}  ;; key to key
                    {:des "c to example osascript"                            :rules [[:c "osascript -e 'display dialog \"example apple script\"'"]]} ;; key to shell script
                    {:des "d to 1 then example osascript"                     :rules [[:d [:1 "osascript -e 'display dialog \"example apple script\"'"]]]} ;; key to key then shell script
                    {:des "simultaneous e f to 3"                             :rules [[[:e :f] :3]]} ;; simultaneous key to key
                    {:des "g to 4 when variable vi-mode is 1"                 :rules [[:g :4 :vi-mode]]} ;; vi-mode is 1
                    {:des "h to 5 when variable vi-mode is not 1"             :rules [[:h :5 :!vi-mode]]} ;; vi-mode is not 1
                    {:des "i to 6 only for device hhkb-bt"                    :rules [[:i :6 :hhkb-bt]]} ;; key to key in layer b (in layer a) specific to hhkb-bt device
                    {:des "j to 7 on hhkb-bt when variable vi-mode is 1"      :rules [[:j :7 [:vi-mode :hhkb-bt]]]} ;; multiple condition
                    {:des "press h insert 8 then set variable some-mode to 0" :rules [[:h [:8 {:set ["some-mode" 0]}]]]}])

(def result [{:description "a to 1",
              :manipulators [{:from {:key_code "a"},
                              :to [{:key_code "1"}],
                              :type "basic"}]}
             {:description "command a to control 1",
              :manipulators [{:from {:key_code "a",
                                     :modifiers {:mandatory ["left_command"]}},
                              :to [{:key_code "1",
                                    :modifiers ["left_control"]}],
                              :type "basic"}]}
             {:description "my spacebar to control 1",
              :manipulators [{:from {:key :spacebar},
                              :to [{:key_code "1",
                                    :modifiers ["left_control"]}],
                              :type "basic"}]}
             {:description "press b to insert 12",
              :manipulators [{:from {:key_code "b"},
                              :to [{:key_code "1"}
                                   {:key_code "2"}],
                              :type "basic"}]}
             {:description "c to example osascript",
              :manipulators [{:from {:key_code "c"},
                              :to [{:shell_command "osascript -e 'display dialog \"example apple script\"'"}],
                              :type "basic"}]}
             {:description "d to 1 then example osascript",
              :manipulators [{:from {:key_code "d"},
                              :to [{:key_code "1"}
                                   {:shell_command "osascript -e 'display dialog \"example apple script\"'"}],
                              :type "basic"}]}
             {:description "simultaneous e f to 3",
              :manipulators [{:from {:simultaneous [{:key_code "e"}
                                                    {:key_code "f"}],
                                     :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                            :key_down_order "insensitive",
                                                            :key_up_order "insensitive",
                                                            :key_up_when "any"}},
                              :to [{:key_code "3"}],
                              :type "basic"}]}
             {:description "g to 4 when variable vi-mode is 1",
              :manipulators [{:from {:key_code "g"},
                              :to [{:key_code "4"}],
                              :conditions [{:name "vi-mode",
                                            :value 1,
                                            :type "variable_if"}],
                              :type "basic"}
                             {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                              :to [{:set_variable {:name "vi-mode", :value 1}}
                                   {:key_code "4"}],
                              :from {:simultaneous [{:key_code "d"}
                                                    {:key_code "g"}],
                                     :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                            :key_down_order "strict",
                                                            :key_up_order "strict_inverse",
                                                            :key_up_when "any"}}}]}
             {:description "h to 5 when variable vi-mode is not 1",
              :manipulators [{:from {:key_code "h"},
                              :to [{:key_code "5"}],
                              :conditions [{:name "vi-mode",
                                            :value 1,
                                            :type "variable_unless"}],
                              :type "basic"}]}
             {:description "i to 6 only for device hhkb-bt",
              :manipulators [{:from {:key_code "i"},
                              :to [{:key_code "6"}],
                              :conditions [{:identifiers [{:vendor_id 1278,
                                                           :product_id 51966}],
                                            :type "device_if"}],
                              :type "basic"}]}
             {:description "j to 7 on hhkb-bt when variable vi-mode is 1",
              :manipulators [{:from {:key_code "j"},
                              :to [{:key_code "7"}],
                              :conditions [{:name "vi-mode",
                                            :value 1,
                                            :type "variable_if"}
                                           {:identifiers [{:vendor_id 1278,
                                                           :product_id 51966}],
                                            :type "device_if"}],
                              :type "basic"}
                             {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                              :to [{:set_variable {:name "vi-mode", :value 1}}
                                   {:key_code "7"}],
                              :from {:simultaneous [{:key_code "d"}
                                                    {:key_code "j"}],
                                     :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                            :key_down_order "strict",
                                                            :key_up_order "strict_inverse",
                                                            :key_up_when "any"}}}]}
             {:description "press h insert 8 then set variable some-mode to 0",
              :manipulators [{:from {:key_code "h"},
                              :to [{:key_code "8"}
                                   {:set_variable {:name "some-mode", :value 0}}],
                              :type "basic"}]}])

(t/deftest generate-mains
  (init-conf-data)
  (update-conf-data {:applications {:chrome ["^com\\.google\\.Chrome$"]
                                    :chrome-canary ["^com\\.google\\.Chrome\\.canary$"]
                                    :chromes ["^com\\.google\\.Chrome$" "^com\\.google\\.Chrome\\.canary$"]}
                     :devices {:hhkb-bt [{:vendor_id 1278 :product_id 51966}]
                               :hhkb [{:vendor_id 2131 :product_id 256}]}
                     :input-source {}
                     :modifiers {}
                     :froms {:my-spacebar {:key :spacebar}}
                     :tos {}
                     :layers {}
                     :simlayers {:vi-mode {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                                           :to [{:set ["vi-mode" 1]}],
                                           :from {:sim [:d],
                                                  :simo {:dorder :strict,
                                                         :uorder :strict_inverse,
                                                         :afterup {:set ["vi-mode" 0]}}}}}
                     :simlayer-threshold 250
                     :swaps {}
                     :raws {}})

  (t/testing
      (t/is (= (sut/parse-mains example-mains) result))))