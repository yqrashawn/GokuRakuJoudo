(ns karabiner-configurator.keys-info)

(def keys-info
  {:button1 {:button true}
   :button2 {:button true}
   :button3 {:button true}
   :button4 {:button true}
   :button5 {:button true}
   :button6 {:button true}
   :button7 {:button true}
   :button8 {:button true}
   :button9 {:button true}
   :button10 {:button true}
   :button11 {:button true}
   :button12 {:button true}
   :button13 {:button true}
   :button14 {:button true}
   :button15 {:button true}
   :button16 {:button true}
   :button17 {:button true}
   :button18 {:button true}
   :button19 {:button true}
   :button21 {:button true}
   :button22 {:button true}
   :button23 {:button true}
   :button24 {:button true}
   :button25 {:button true}
   :button26 {:button true}
   :button27 {:button true}
   :button28 {:button true}
   :button29 {:button true}
   :button30 {:button true}
   :button31 {:button true}
   :button32 {:button true}
   ;; :x {:mouse_key true}
   ;; :y {:mouse_key true}
   :any {:modifier true :both true}
   :command {:modifier true :both true}
   :shift {:modifier true :both true}
   :option {:modifier true :both true}
   :control {:modifier true :both true}
   :caps_lock {:modifier true}
   :left_control {:modifier true}
   :left_shift {:modifier true}
   :left_option {:modifier true}
   :left_command {:modifier true}
   :right_control {:modifier true}
   :right_shift {:modifier true}
   :right_option {:modifier true}
   :right_command {:modifier true}
   :fn {:modifier true}
   :return_or_enter {:label "enter" :display true}
   :escape {:display true}
   :delete_or_backspace {:label "backspace" :display true}
   :delete_forward {:label "del" :display true}
   :tab {:display true}
   :spacebar {:display true}
   :hyphen {:label "hyphen (-)" :display true}
   :equal_sign {:label "equal_sign (=)" :display true}
   :open_bracket {:label "open_bracket [" :display true}
   :close_bracket {:label "close_bracket ]" :display true}
   :backslash {:label "backslash (\\)" :display true}
   :non_us_pound {}
   :semicolon {:label "semicolon (;)" :display true}
   :quote {:label "quote (')" :display true}
   :grave_accent_and_tilde {:label "grave_accent_and_tilde ()" :display true}
   :comma {:label "comma (,)" :display true}
   :period {:label "period (.)" :display true}
   :slash {:label "slash (/)" :display true}
   :non_us_backslash {}
   :up_arrow {:display true}
   :down_arrow {:display true}
   :left_arrow {:display true}
   :right_arrow {:display true}
   :page_up {:display true}
   :page_down {:display true}
   :home {:display true}
   :end {:display true}
   :a {:display true}
   :b {:display true}
   :c {:display true}
   :d {:display true}
   :e {:display true}
   :f {:display true}
   :g {:display true}
   :h {:display true}
   :i {:display true}
   :j {:display true}
   :k {:display true}
   :l {:display true}
   :m {:display true}
   :n {:display true}
   :o {:display true}
   :p {:display true}
   :q {:display true}
   :r {:display true}
   :s {:display true}
   :t {:display true}
   :u {:display true}
   :v {:display true}
   :w {:display true}
   :x {:display true}
   :y {:display true}
   :z {:display true}
   :1 {:display true}
   :2 {:display true}
   :3 {:display true}
   :4 {:display true}
   :5 {:display true}
   :6 {:display true}
   :7 {:display true}
   :8 {:display true}
   :9 {:display true}
   :0 {:display true}
   :f1 {:display true}
   :f2 {:display true}
   :f3 {:display true}
   :f4 {:display true}
   :f5 {:display true}
   :f6 {:display true}
   :f7 {:display true}
   :f8 {:display true}
   :f9 {:display true}
   :f10 {:display true}
   :f11 {:display true}
   :f12 {:display true}
   :f13 {}
   :f14 {}
   :f15 {}
   :f16 {}
   :f17 {}
   :f18 {}
   :f19 {}
   :f20 {}
   :f21 {:not-to true}
   :f22 {:not-to true}
   :f23 {:not-to true}
   :f24 {:not-to true}
   :display_brightness_decrement {:not-from true :consumer-key true}
   :display_brightness_increment {:not-from true :consumer-key true}
   :mission_control {:not-from true}
   :launchpad {:not-from true}
   :dashboard {:not-from true}
   :illumination_decrement {:not-from true}
   :illumination_increment {:not-from true}
   :rewind {:not-from true :consumer-key true}
   :play_or_pause {:not-from true :consumer-key true}
   :fastforward {:not-from true :consumer-key true}
   :mute {:consumer-key true}
   :volume_decrement {:consumer-key true}
   :volume_increment {:consumer-key true}
   :eject {:not-from true :consumer-key true}
   :apple_display_brightness_decrement {:not-from true}
   :apple_display_brightness_increment {:not-from true}
   :apple_top_case_display_brightness_decrement {:not-from true}
   :apple_top_case_display_brightness_increment {:not-from true}
   :keypad_num_lock {:display true}
   :keypad_slash {:display true}
   :keypad_asterisk {:display true}
   :keypad_hyphen {:display true}
   :keypad_plus {:display true}
   :keypad_enter {:display true}
   :keypad_1 {:display true}
   :keypad_2 {:display true}
   :keypad_3 {:display true}
   :keypad_4 {:display true}
   :keypad_5 {:display true}
   :keypad_6 {:display true}
   :keypad_7 {:display true}
   :keypad_8 {:display true}
   :keypad_9 {:display true}
   :keypad_0 {:display true}
   :keypad_period {:display true}
   :keypad_equal_sign {:display true}
   :keypad_comma {:display true}
   :vk_none {:label "vk_none (disable this key)" :not-from true}
   :print_screen {:display true}
   :scroll_lock {:display true}
   :pause {:display true}
   :insert {:display true}
   :application {}
   :help {}
   :power {}
   :execute {:not-to true}
   :menu {:not-to true}
   :select {:not-to true}
   :stop {:not-to true}
   :again {:not-to true}
   :undo {:not-to true}
   :cut {:not-to true}
   :copy {:not-to true}
   :paste {:not-to true}
   :find {:not-to true}
   :international1 {}
   :international2 {:not-to true}
   :international3 {}
   :international4 {:not-to true}
   :international5 {:not-to true}
   :international6 {:not-to true}
   :international7 {:not-to true}
   :international8 {:not-to true}
   :international9 {:not-to true}
   :lang1 {}
   :lang2 {}
   :lang3 {:not-to true}
   :lang4 {:not-to true}
   :lang5 {:not-to true}
   :lang6 {:not-to true}
   :lang7 {:not-to true}
   :lang8 {:not-to true}
   :lang9 {:not-to true}
   :japanese_eisuu {:label "英数キー" :display true}
   :japanese_kana {:label "かなキー" :display true}
   :japanese_pc_nfer {:label "PCキーボードの無変換キー" :not-to true}
   :japanese_pc_xfer {:label "PCキーボードの変換キー" :not-to true}
   :japanese_pc_katakana {:label "PCキーボードのかなキー" :not-to true}
   :keypad_equal_sign_as400 {:not-to true}
   :locking_caps_lock {:not-to true}
   :locking_num_lock {:not-to true}
   :locking_scroll_lock {:not-to true}
   :alternate_erase {:not-to true}
   :sys_req_or_attention {:not-to true}
   :cancel {:not-to true}
   :clear {:not-to true}
   :prior {:not-to true}
   :return {:label "rarely used return (HID usage 0x9e)" :not-to true}
   :separator {:not-to true}
   :out {:not-to true}
   :oper {:not-to true}
   :clear_or_again {:not-to true}
   :cr_sel_or_props {:not-to true}
   :ex_sel {:not-to true}
   :left_alt {:label "left_alt (equal toleft_option)"}
   :left_gui {:label "left_gui (equal toleft_command)"}
   :right_alt {:label "right_alt (equal toright_option)"}
   :right_gui {:label "right_gui (equal toright_command)"}
   :vk_consumer_brightness_down {:label "vk_consumer_brightness_down (equal todisplay_brightness_decrement)" :not-from true}
   :vk_consumer_brightness_up {:label "vk_consumer_brightness_up (equal todisplay_brightness_increment)" :not-from true}
   :vk_mission_control {:label "vk_mission_control (equal tomission_control)" :not-from true}
   :vk_launchpad {:label "vk_launchpad (equal tolaunchpad)" :not-from true}
   :vk_dashboard {:label "vk_dashboard (equal todashboard)" :not-from true}
   :vk_consumer_illumination_down {:label "vk_consumer_illumination_down (equal toillumination_decrement)" :not-from true}
   :vk_consumer_illumination_up {:label "vk_consumer_illumination_up (equal toillumination_increment)" :not-from true}
   :vk_consumer_previous {:label "vk_consumer_previous (equal torewind)" :not-from true}
   :vk_consumer_play {:label "vk_consumer_play (equal toplay)" :not-from true}
   :vk_consumer_next {:label "vk_consumer_next (equal tofastforward)" :not-from true}
   :volume_down {:label "volume_down (equal tovolume_decrement)"}
   :volume_up {:label "volume_up (equal tovolume_increment`)"}})
