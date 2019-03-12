(ns karabiner-configurator.rules-test
  (:require [karabiner-configurator.rules :as sut]
            [karabiner-configurator.data :refer :all]
            [clojure.test :as t]))

(def example-mains [{:des "a to 1"                                            :rules [[:condi :chunkwm-move-mode]
                                                                                      [:profiles :Default :test-profile-2]
                                                                                      [:a :1]]} ;; a to 1
                    {:des "command a to control 1"                            :rules [[:!C#Pa :!T1]]} ;; command a to control 1
                    {:des "my spacebar to control 1"                          :rules [[:my-spacebar :!T1]]} ;; my-spacebar to control 1
                    {:des "press b to insert 12"                              :rules [[:b [:1 :2]]]}  ;; key to key
                    {:des "c to example osascript"                            :rules [[:c "osascript -e 'display dialog \"example apple script\"'"]]} ;; key to shell script
                    {:des "d to 1 then example osascript"                     :rules [[:d [:1 [:example-template "example apple script"]]]]} ;; key to key then shell script
                    {:des "simultaneous e f to 3"                             :rules [[[:e :f] :3]]} ;; simultaneous key to key
                    {:des "g to 4 when variable vi-mode is 1"                 :rules [[:g :4 :vi-mode]]} ;; vi-mode is 1
                    {:des "h to 5 when variable vi-mode is not 1"             :rules [[:h :5 :!vi-mode]]} ;; vi-mode is not 1
                    {:des "i to 6 only for device hhkb-bt"                    :rules [[:i :6 [:hhkb-bt :hhkb]]]} ;; key to key in layer b (in layer a) specific to hhkb-bt device
                    {:des "j to 7 on hhkb-bt when variable vi-mode is 1"      :rules [[:j :7 [:vi-mode :hhkb-bt]]]} ;; multiple condition
                    {:des "press h insert 8 then set variable some-mode to 0" :rules [[:h [:8 {:set ["some-mode" 0]}]]]}
                    {:des "capslock to control as modifier to escape when press alone" :rules [[:##caps_lock :left_control nil {:alone :escape}]]}
                    {:des "Quit application by pressing command-q twice" :rules [[:!C#Pq :!Cq ["command-q" 1]]
                                                                                 [:!C#Pq ["command-q" 1] nil {:delayed {:invoked ["command-q" 0] :canceled ["commandq" 0]}}]]}
                    {:des "Quit application by holding command-q" :rules [[:!C#Pq nil nil {:held {:key :q :modi :left_command :repeat false}}]]}
                    {:des "Quit Safari by pressing command-q twice" :rules [[:!C#Pq :!Cq [:safari ["command-q" 1]]]
                                                                            [:!C#Pq ["command-q" 1] :safari {:delayed {:invoked ["command-q" 0] :canceled ["command-q" 0]}}]]}
                    {:des "Mouse button"
                     :rules [[{:pkey :button5} :mission_control]
                             [{:pkey :button4} [{:pkey :button1} {:pkey :button1} :!!grave_accent_and_tilde]]]}
                    {:des "Change input source"
                     :rules [[:i :us :q-mode]
                             [:o :squirrel :q-mode]]}
                    {:des "tab-mode"
                     :rules [:test-profile
                             :chunkwm-move-mode
                             [:h "/usr/local/bin/chunkc tiling::window --warp west"]
                             :Default
                             :chunkwm-scale-mode
                             [:h "/usr/local/bin/chunkc tiling::window --use-temporary-ratio 0.03 --adjust-window-edge west"]
                             [:profiles :test-profile :test-profile-2]
                             :tab-mode
                             [:h "/usr/local/bin/chunkc tiling::window --focus west"]
                             [:condi :chunkwm-move-mode :chunkwm-scale-mode]
                             [:l "/usr/local/bin/chunkc tiling::window --focus east"]]}
                    {:des "input source as condition"
                     :rules [[:a :a :us]]}
                    {:des "any keycode"
                     :rules [[{:any :key_code} :a]
                             [{:any :consumer_key_code} :a]
                             [{:any :pointing_button} :a]]}
                    {:des "double press and held key in simlayer (to_delayed_action, to_if_held_down)"
                     :rules [[:j "say 'j double press'" [["q-mode" 1] ["q-mode-j-dbpress-mode" 1]]]
                             :q-mode
                             [:j ["say 'j press down'" ["q-mode-j-dbpress-mode" 1]] nil {:delayed {:canceled ["q-mode-j-dbpress-mode" 0]
                                                                                                   :invoked ["q-mode-j-dbpress-mode" 0]}
                                                                                         :held "say 'j held down'"}]]}
                    {:des "QWER in to right modifier keys" :rules [[:!QWERa :a]]}])

(def result {:test-profile-2
             [{:description "Auto generated layer conditions",
               :manipulators
               [{:type "basic",
                 :to [{:set_variable {:name "chunkwm-move-mode", :value 1}}],
                 :from {:key_code "f"},
                 :to_after_key_up
                 [{:set_variable {:name "chunkwm-move-mode", :value 0}}],
                 :to_if_alone [{:key_code "f"}],
                 :conditions [{:name "tab-mode", :value 1, :type "variable_if"}]}
                {:type "basic",
                 :to [{:set_variable {:name "tab-mode", :value 1}}],
                 :from {:key_code "tab"},
                 :to_after_key_up
                 [{:set_variable {:name "tab-mode", :value 0}}
                  {:set_variable {:name "chunkwm-move-mode", :value 0}}
                  {:set_variable {:name "chunkwm-scale-mode", :value 0}}],
                 :to_if_alone [{:key_code "tab"}]}
                {:type "basic",
                 :to [{:set_variable {:name "chunkwm-scale-mode", :value 1}}],
                 :from {:key_code "c"},
                 :to_after_key_up
                 [{:set_variable {:name "chunkwm-scale-mode", :value 0}}],
                 :to_if_alone [{:key_code "c"}],
                 :conditions [{:name "tab-mode", :value 1, :type "variable_if"}]}]}
              {:description "a to 1",
               :manipulators
               [{:from {:key_code "a"},
                 :to [{:key_code "1"}],
                 :conditions
                 [{:name "chunkwm-move-mode", :value 1, :type "variable_if"}],
                 :type "basic"}]}
              {:description "tab-mode",
               :manipulators
               [{:from {:key_code "h"},
                 :to
                 [{:shell_command
                   "/usr/local/bin/chunkc tiling::window --focus west"}],
                 :conditions [{:name "tab-mode", :value 1, :type "variable_if"}],
                 :type "basic"}
                {:from {:key_code "l"},
                 :to
                 [{:shell_command
                   "/usr/local/bin/chunkc tiling::window --focus east"}],
                 :conditions
                 [{:name "chunkwm-scale-mode", :value 1, :type "variable_if"}
                  {:name "chunkwm-move-mode", :value 1, :type "variable_if"}],
                 :type "basic"}]}],
             :Default
             [{:description "Auto generated layer conditions",
               :manipulators
               [{:type "basic",
                 :to [{:set_variable {:name "chunkwm-move-mode", :value 1}}],
                 :from {:key_code "f"},
                 :to_after_key_up
                 [{:set_variable {:name "chunkwm-move-mode", :value 0}}],
                 :to_if_alone [{:key_code "f"}],
                 :conditions [{:name "tab-mode", :value 1, :type "variable_if"}]}
                {:type "basic",
                 :to [{:set_variable {:name "chunkwm-scale-mode", :value 1}}],
                 :from {:key_code "c"},
                 :to_after_key_up
                 [{:set_variable {:name "chunkwm-scale-mode", :value 0}}],
                 :to_if_alone [{:key_code "c"}],
                 :conditions [{:name "tab-mode", :value 1, :type "variable_if"}]}]}
              {:description "a to 1",
               :manipulators
               [{:from {:key_code "a"},
                 :to [{:key_code "1"}],
                 :conditions
                 [{:name "chunkwm-move-mode", :value 1, :type "variable_if"}],
                 :type "basic"}]}
              {:description "command a to control 1",
               :manipulators
               [{:from
                 {:key_code "a",
                  :modifiers
                  {:mandatory ["left_command"], :optional ["caps_lock"]}},
                 :to [{:key_code "1", :modifiers ["left_control"]}],
                 :type "basic"}]}
              {:description "my spacebar to control 1",
               :manipulators
               [{:from {:key :spacebar},
                 :to [{:key_code "1", :modifiers ["left_control"]}],
                 :type "basic"}]}
              {:description "press b to insert 12",
               :manipulators
               [{:from {:key_code "b"},
                 :to [{:key_code "1"} {:key_code "2"}],
                 :type "basic"}]}
              {:description "c to example osascript",
               :manipulators
               [{:from {:key_code "c"},
                 :to
                 [{:shell_command
                   "osascript -e 'display dialog \"example apple script\"'"}],
                 :type "basic"}]}
              {:description "d to 1 then example osascript",
               :manipulators
               [{:from {:key_code "d"},
                 :to
                 [{:key_code "1"}
                  {:shell_command
                   "osascript -e 'display dialog \"example apple script\"'"}],
                 :type "basic"}]}
              {:description "simultaneous e f to 3",
               :manipulators
               [{:from
                 {:simultaneous [{:key_code "e"} {:key_code "f"}],
                  :simultaneous_options
                  {:detect_key_down_uninterruptedly false,
                   :key_down_order "insensitive",
                   :key_up_order "insensitive",
                   :key_up_when "any"}},
                 :to [{:key_code "3"}],
                 :type "basic"}]}
              {:description "g to 4 when variable vi-mode is 1",
               :manipulators
               [{:from {:key_code "g"},
                 :to [{:key_code "4"}],
                 :conditions [{:name "vi-mode", :value 1, :type "variable_if"}],
                 :type "basic"}
                {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                 :to [{:set_variable {:name "vi-mode", :value 1}} {:key_code "4"}],
                 :from
                 {:simultaneous [{:key_code "d"} {:key_code "g"}],
                  :simultaneous_options
                  {:detect_key_down_uninterruptedly true,
                   :key_down_order "strict",
                   :key_up_order "strict_inverse",
                   :key_up_when "any",
                   :to_after_key_up
                   [{:set_variable {:name "vi-mode", :value 0}}]}}}]}
              {:description "h to 5 when variable vi-mode is not 1",
               :manipulators
               [{:from {:key_code "h"},
                 :to [{:key_code "5"}],
                 :conditions
                 [{:name "vi-mode", :value 1, :type "variable_unless"}],
                 :type "basic"}]}
              {:description "i to 6 only for device hhkb-bt",
               :manipulators
               [{:from {:key_code "i"},
                 :to [{:key_code "6"}],
                 :conditions
                 [{:identifiers
                   [{:vendor_id 1278, :product_id 51966}
                    {:vendor_id 2131, :product_id 256}],
                   :type "device_if"}],
                 :type "basic"}]}
              {:description "j to 7 on hhkb-bt when variable vi-mode is 1",
               :manipulators
               [{:from {:key_code "j"},
                 :to [{:key_code "7"}],
                 :conditions
                 [{:name "vi-mode", :value 1, :type "variable_if"}
                  {:identifiers [{:vendor_id 1278, :product_id 51966}],
                   :type "device_if"}],
                 :type "basic"}
                {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                 :to [{:set_variable {:name "vi-mode", :value 1}} {:key_code "7"}],
                 :from
                 {:simultaneous [{:key_code "d"} {:key_code "j"}],
                  :simultaneous_options
                  {:detect_key_down_uninterruptedly true,
                   :key_down_order "strict",
                   :key_up_order "strict_inverse",
                   :key_up_when "any",
                   :to_after_key_up
                   [{:set_variable {:name "vi-mode", :value 0}}]}}}]}
              {:description "press h insert 8 then set variable some-mode to 0",
               :manipulators
               [{:from {:key_code "h"},
                 :to
                 [{:key_code "8"} {:set_variable {:name "some-mode", :value 0}}],
                 :type "basic"}]}
              {:description
               "capslock to control as modifier to escape when press alone",
               :manipulators
               [{:to_if_alone [{:key_code "escape"}],
                 :from {:key_code "caps_lock", :modifiers {:optional ["any"]}},
                 :to [{:key_code "left_control"}],
                 :type "basic"}]}
              {:description "Quit application by pressing command-q twice",
               :manipulators
               [{:from
                 {:key_code "q",
                  :modifiers
                  {:mandatory ["left_command"], :optional ["caps_lock"]}},
                 :to [{:key_code "q", :modifiers ["left_command"]}],
                 :conditions [{:name "command-q", :value 1, :type "variable_if"}],
                 :type "basic"}
                {:to_delayed_action
                 {:to_if_invoked [{:set_variable {:name "command-q", :value 0}}],
                  :to_if_canceled [{:set_variable {:name "commandq", :value 0}}]},
                 :from
                 {:key_code "q",
                  :modifiers
                  {:mandatory ["left_command"], :optional ["caps_lock"]}},
                 :to [{:set_variable {:name "command-q", :value 1}}],
                 :type "basic"}]}
              {:description "Quit application by holding command-q",
               :manipulators
               [{:to_if_held_down
                 [{:modifiers ["left_command"], :key_code "q", :repeat false}],
                 :from
                 {:key_code "q",
                  :modifiers
                  {:mandatory ["left_command"], :optional ["caps_lock"]}},
                 :type "basic"}]}
              {:description "Quit Safari by pressing command-q twice",
               :manipulators
               [{:from
                 {:key_code "q",
                  :modifiers
                  {:mandatory ["left_command"], :optional ["caps_lock"]}},
                 :to [{:key_code "q", :modifiers ["left_command"]}],
                 :conditions
                 [{:bundle_identifiers ["^com\\.apple\\.Safari$"],
                   :type "frontmost_application_if"}
                  {:name "command-q", :value 1, :type "variable_if"}],
                 :type "basic"}
                {:to_delayed_action
                 {:to_if_invoked [{:set_variable {:name "command-q", :value 0}}],
                  :to_if_canceled [{:set_variable {:name "command-q", :value 0}}]},
                 :from
                 {:key_code "q",
                  :modifiers
                  {:mandatory ["left_command"], :optional ["caps_lock"]}},
                 :to [{:set_variable {:name "command-q", :value 1}}],
                 :conditions
                 [{:bundle_identifiers ["^com\\.apple\\.Safari$"],
                   :type "frontmost_application_if"}],
                 :type "basic"}]}
              {:description "Mouse button",
               :manipulators
               [{:from {:pointing_button "button5"},
                 :to [{:key_code "mission_control"}],
                 :type "basic"}
                {:from {:pointing_button "button4"},
                 :to
                 [{:pointing_button "button1"}
                  {:pointing_button "button1"}
                  {:key_code "grave_accent_and_tilde",
                   :modifiers
                   ["left_command" "left_control" "left_option" "left_shift"]}],
                 :type "basic"}]}
              {:description "Change input source",
               :manipulators
               [{:from {:key_code "i"},
                 :to
                 [{:select_input_source
                   {:input_mode_id "",
                    :input_source_id "com.apple.keylayout.US",
                    :language "en"}}],
                 :conditions [{:name "q-mode", :value 1, :type "variable_if"}],
                 :type "basic"}
                {:key :q,
                 :from
                 {:simultaneous [{:key_code "i"}],
                  :simultaneous_options
                  {:detect_key_down_uninterruptedly false,
                   :key_down_order "insensitive",
                   :key_up_order "insensitive",
                   :key_up_when "any"}},
                 :to
                 [{:select_input_source
                   {:input_mode_id "",
                    :input_source_id "com.apple.keylayout.US",
                    :language "en"}}]}
                {:from {:key_code "o"},
                 :to
                 [{:select_input_source
                   {:input_mode_id "com.googlecode.rimeime.inputmethod.Squirrel",
                    :input_source_id
                    "com.googlecode.rimeime.inputmethod.Squirrel.Rime",
                    :language "zh-Hans"}}],
                 :conditions [{:name "q-mode", :value 1, :type "variable_if"}],
                 :type "basic"}
                {:key :q,
                 :from
                 {:simultaneous [{:key_code "o"}],
                  :simultaneous_options
                  {:detect_key_down_uninterruptedly false,
                   :key_down_order "insensitive",
                   :key_up_order "insensitive",
                   :key_up_when "any"}},
                 :to
                 [{:select_input_source
                   {:input_mode_id "com.googlecode.rimeime.inputmethod.Squirrel",
                    :input_source_id
                    "com.googlecode.rimeime.inputmethod.Squirrel.Rime",
                    :language "zh-Hans"}}]}]}
              {:description "tab-mode",
               :manipulators
               [{:from {:key_code "h"},
                 :to
                 [{:shell_command
                   "/usr/local/bin/chunkc tiling::window --use-temporary-ratio 0.03 --adjust-window-edge west"}],
                 :conditions
                 [{:name "chunkwm-scale-mode", :value 1, :type "variable_if"}],
                 :type "basic"}]}
              {:description "input source as condition",
               :manipulators
               [{:from {:key_code "a"},
                 :to [{:key_code "a"}],
                 :conditions
                 [{:input_sources
                   [{:input_mode_id "",
                     :input_source_id "com.apple.keylayout.US",
                     :language "en"}],
                   :type "input_source_if"}],
                 :type "basic"}]}
              {:description "any keycode",
               :manipulators
               [{:from {:any "key_code"}, :to [{:key_code "a"}], :type "basic"}
                {:from {:any "consumer_key_code"},
                 :to [{:key_code "a"}],
                 :type "basic"}
                {:from {:any "pointing_button"},
                 :to [{:key_code "a"}],
                 :type "basic"}]}
              {:description
               "double press and held key in simlayer (to_delayed_action, to_if_held_down)",
               :manipulators
               [{:from {:key_code "j"},
                 :to [{:shell_command "say 'j double press'"}],
                 :conditions
                 [{:name "q-mode", :value 1, :type "variable_if"}
                  {:name "q-mode-j-dbpress-mode", :value 1, :type "variable_if"}],
                 :type "basic"}
                {:to_if_held_down [{:shell_command "say 'j held down'"}],
                 :to_delayed_action
                 {:to_if_invoked
                  [{:set_variable {:name "q-mode-j-dbpress-mode", :value 0}}],
                  :to_if_canceled
                  [{:set_variable {:name "q-mode-j-dbpress-mode", :value 0}}]},
                 :from {:key_code "j"},
                 :to
                 [{:shell_command "say 'j press down'"}
                  {:set_variable {:name "q-mode-j-dbpress-mode", :value 1}}],
                 :conditions [{:name "q-mode", :value 1, :type "variable_if"}],
                 :type "basic"}
                {:key :q,
                 :from
                 {:simultaneous [{:key_code "j"}],
                  :simultaneous_options
                  {:detect_key_down_uninterruptedly false,
                   :key_down_order "insensitive",
                   :key_up_order "insensitive",
                   :key_up_when "any"}},
                 :to
                 [{:shell_command "say 'j press down'"}
                  {:set_variable {:name "q-mode-j-dbpress-mode", :value 1}}],
                 :to_if_held_down [{:shell_command "say 'j held down'"}],
                 :to_delayed_action
                 {:to_if_invoked
                  [{:set_variable {:name "q-mode-j-dbpress-mode", :value 0}}],
                  :to_if_canceled
                  [{:set_variable {:name "q-mode-j-dbpress-mode", :value 0}}]}}]}
              {:description "QWER in to right modifier keys",
               :manipulators
               [{:from
                 {:key_code "a",
                  :modifiers
                  {:mandatory
                   ["right_command"
                    "right_control"
                    "right_option"
                    "right_shift"]}},
                 :to [{:key_code "a"}],
                 :type "basic"}]}],
             :test-profile
             [{:description "Auto generated layer conditions",
               :manipulators
               [{:type "basic",
                 :to [{:set_variable {:name "chunkwm-move-mode", :value 1}}],
                 :from {:key_code "f"},
                 :to_after_key_up
                 [{:set_variable {:name "chunkwm-move-mode", :value 0}}],
                 :to_if_alone [{:key_code "f"}],
                 :conditions [{:name "tab-mode", :value 1, :type "variable_if"}]}
                {:type "basic",
                 :to [{:set_variable {:name "tab-mode", :value 1}}],
                 :from {:key_code "tab"},
                 :to_after_key_up
                 [{:set_variable {:name "tab-mode", :value 0}}
                  {:set_variable {:name "chunkwm-move-mode", :value 0}}
                  {:set_variable {:name "chunkwm-scale-mode", :value 0}}],
                 :to_if_alone [{:key_code "tab"}]}
                {:type "basic",
                 :to [{:set_variable {:name "chunkwm-scale-mode", :value 1}}],
                 :from {:key_code "c"},
                 :to_after_key_up
                 [{:set_variable {:name "chunkwm-scale-mode", :value 0}}],
                 :to_if_alone [{:key_code "c"}],
                 :conditions [{:name "tab-mode", :value 1, :type "variable_if"}]}]}
              {:description "tab-mode",
               :manipulators
               [{:from {:key_code "h"},
                 :to
                 [{:shell_command
                   "/usr/local/bin/chunkc tiling::window --warp west"}],
                 :conditions
                 [{:name "chunkwm-move-mode", :value 1, :type "variable_if"}],
                 :type "basic"}
                {:from {:key_code "h"},
                 :to
                 [{:shell_command
                   "/usr/local/bin/chunkc tiling::window --focus west"}],
                 :conditions [{:name "tab-mode", :value 1, :type "variable_if"}],
                 :type "basic"}
                {:from {:key_code "l"},
                 :to
                 [{:shell_command
                   "/usr/local/bin/chunkc tiling::window --focus east"}],
                 :conditions
                 [{:name "chunkwm-scale-mode", :value 1, :type "variable_if"}
                  {:name "chunkwm-move-mode", :value 1, :type "variable_if"}],
                 :type "basic"}]}]})

(t/deftest generate-mains
  (init-conf-data)
  (update-conf-data
   {:profiles {:Default
               {:complex_modifications {:parameters {:basic.simultaneous_threshold_milliseconds 50,
                                                     :basic.to_delayed_action_delay_milliseconds 500,
                                                     :basic.to_if_alone_timeout_milliseconds 1000,
                                                     :basic.to_if_held_down_threshold_milliseconds 500}}}
               :test-profile
               {:complex_modifications {:parameters {:basic.simultaneous_threshold_milliseconds 50,
                                                     :basic.to_delayed_action_delay_milliseconds 500,
                                                     :basic.to_if_alone_timeout_milliseconds 1000,
                                                     :basic.to_if_held_down_threshold_milliseconds 500}}}
               :test-profile-2
               {:complex_modifications {:parameters {:basic.simultaneous_threshold_milliseconds 50,
                                                     :basic.to_delayed_action_delay_milliseconds 500,
                                                     :basic.to_if_alone_timeout_milliseconds 1000,
                                                     :basic.to_if_held_down_threshold_milliseconds 500}}}}
    :applications {:safari ["^com\\.apple\\.Safari$"]
                   :chrome ["^com\\.google\\.Chrome$"]
                   :chrome-canary ["^com\\.google\\.Chrome\\.canary$"]
                   :chromes ["^com\\.google\\.Chrome$" "^com\\.google\\.Chrome\\.canary$"]}
    :devices {:hhkb-bt [{:vendor_id 1278 :product_id 51966}]
              :hhkb [{:vendor_id 2131 :product_id 256}]}
    :input-sources {:squirrel {:input_mode_id "com.googlecode.rimeime.inputmethod.Squirrel"
                               :input_source_id "com.googlecode.rimeime.inputmethod.Squirrel.Rime"
                               :language "zh-Hans"}
                    :us {:input_mode_id ""
                         :input_source_id "com.apple.keylayout.US"
                         :language "en"}}
    :templates {:example-template "osascript -e 'display dialog \"%s\"'"}
    :modifiers {}
    :froms {:my-spacebar {:key :spacebar}}
    :tos {}
    :layers
    {:tab-mode {:type "basic",
                :to [{:set_variable {:name "tab-mode", :value 1}}],
                :from {:key_code "tab"},
                :to_after_key_up [{:set_variable {:name "tab-mode", :value 0}}
                                  {:set_variable {:name "chunkwm-move-mode",
                                                  :value 0}}
                                  {:set_variable {:name "chunkwm-scale-mode",
                                                  :value 0}}],
                :to_if_alone [{:key_code "tab"}]},
     :chunkwm-move-mode {:type "basic",
                         :to [{:set_variable {:name "chunkwm-move-mode",
                                              :value 1}}],
                         :from {:key_code "f"},
                         :to_after_key_up [{:set_variable {:name "chunkwm-move-mode",
                                                           :value 0}}],
                         :to_if_alone [{:key_code "f"}],
                         :conditions [{:name "tab-mode",
                                       :value 1,
                                       :type "variable_if"}]},
     :chunkwm-scale-mode {:type "basic",
                          :to [{:set_variable {:name "chunkwm-scale-mode",
                                               :value 1}}],
                          :from {:key_code "c"},
                          :to_after_key_up [{:set_variable {:name "chunkwm-scale-mode",
                                                            :value 0}}],
                          :to_if_alone [{:key_code "c"}],
                          :conditions [{:name "tab-mode",
                                        :value 1,
                                        :type "variable_if"}]}}
    :simlayers {:q-mode {:key :q}
                :vi-mode {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                          :to [{:set ["vi-mode" 1]}],
                          :from {:sim [:d],
                                 :simo {:interrupt true,
                                        :dorder :strict,
                                        :uorder :strict_inverse,
                                        :afterup {:set ["vi-mode" 0]}}}}
                :launcher-mode {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                                :to [{:set ["vi-mode" 1]}],
                                :from {:sim [:d],
                                       :simo {:interrupt true,
                                              :dorder :strict,
                                              :uorder :strict_inverse,
                                              :afterup {:set ["vi-mode" 0]}}}}}
    :simlayer-threshold 250})

  (t/testing
      (t/is (= (sut/parse-mains example-mains) result))))

