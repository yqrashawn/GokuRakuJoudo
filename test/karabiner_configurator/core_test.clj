(ns karabiner-configurator.core-test
  (:require [clojure.test :as t]
            [karabiner-configurator.core :as sut]
            [karabiner-configurator.data :refer :all]))

(def vi-mode-example {:applications {:vim-mode-disabled-application ["^com\\.apple\\.Terminal$",
                                                                     "^com\\.googlecode\\.iterm2$",
                                                                     "^co\\.zeit\\.hyperterm$",
                                                                     "^co\\.zeit\\.hyper$",
                                                                     "^io\\.alacritty$",
                                                                     "^net\\.kovidgoyal\\.kitty$",
                                                                     "^org\\.vim\\.",
                                                                     "^com\\.qvacua\\.VimR$"]}
                      :simlayers {:vi-mode {:key :d}
                                  :vi-visual-mode {:key :v :condi [["vi-mode" 1] :!vim-mode-disabled-application]}}
                      :main [{:des "Vi Mode [D as Trigger Key]"
                              :rules [[:##j :down_arrow :vi-mode]
                                      [:##k :up_arrow :vi-mode]
                                      [:##h :left_arrow :vi-mode]
                                      [:##l :right_arrow :vi-mode]
                                      [:##b :!Oleft_arrow :vi-mode]
                                      [:##w :!Oright_arrow :vi-mode]
                                      [:##0 :!Ta :vi-mode]
                                      [:##4 :!Te :vi-mode]]}
                             {:des "Vi Visual Mode"
                              :rules [[:##j :!Sdown_arrow :vi-visual-mode]
                                      [:##k :!Sup_arrow :vi-visual-mode]
                                      [:##h :!Sleft_arrow :vi-visual-mode]
                                      [:##l :!Sright_arrow :vi-visual-mode]
                                      [:##b :!SOleft_arrow :vi-visual-mode]
                                      [:##w :!SOright_arrow :vi-visual-mode]
                                      [:##0 :!SCleft_arrow :vi-visual-mode]
                                      [:##4 :!SCright_arrow :vi-visual-mode]
                                      [:##open_bracket :!SOup_arrow :vi-visual-mode]
                                      [:##close_bracket :!SOdown_arrow :vi-visual-mode]]}]})


(def result
  [{:description "Vi Mode [D as Trigger Key]",
    :manipulators [{:from {:key_code "j",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "down_arrow"}],
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-mode", :value 1}}
                         {:key_code "down_arrow"}],
                    :from {:simultaneous [{:key_code "d"}
                                          {:key_code "j"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}}}
                   {:from {:key_code "k",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "up_arrow"}],
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-mode", :value 1}}
                         {:key_code "up_arrow"}],
                    :from {:simultaneous [{:key_code "d"}
                                          {:key_code "k"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}}}
                   {:from {:key_code "h",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "left_arrow"}],
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-mode", :value 1}}
                         {:key_code "left_arrow"}],
                    :from {:simultaneous [{:key_code "d"}
                                          {:key_code "h"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}}}
                   {:from {:key_code "l",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "right_arrow"}],
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-mode", :value 1}}
                         {:key_code "right_arrow"}],
                    :from {:simultaneous [{:key_code "d"}
                                          {:key_code "l"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}}}
                   {:from {:key_code "b",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "left_arrow",
                          :modifiers ["left_option"]}],
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-mode", :value 1}}
                         {:key_code "left_arrow",
                          :modifiers ["left_option"]}],
                    :from {:simultaneous [{:key_code "d"}
                                          {:key_code "b"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}}}
                   {:from {:key_code "w",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "right_arrow",
                          :modifiers ["left_option"]}],
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-mode", :value 1}}
                         {:key_code "right_arrow",
                          :modifiers ["left_option"]}],
                    :from {:simultaneous [{:key_code "d"}
                                          {:key_code "w"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}}}
                   {:from {:key_code "0",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "a",
                          :modifiers ["left_control"]}],
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-mode", :value 1}}
                         {:key_code "a",
                          :modifiers ["left_control"]}],
                    :from {:simultaneous [{:key_code "d"}
                                          {:key_code "0"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}}}
                   {:from {:key_code "4",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "e",
                          :modifiers ["left_control"]}],
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-mode", :value 1}}
                         {:key_code "e",
                          :modifiers ["left_control"]}],
                    :from {:simultaneous [{:key_code "d"}
                                          {:key_code "4"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}}}]}
   {:description "Vi Visual Mode",
    :manipulators [{:from {:key_code "j",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "down_arrow",
                          :modifiers ["left_shift"]}],
                    :conditions [{:name "vi-visual-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-visual-mode",
                                         :value 1}}
                         {:key_code "down_arrow",
                          :modifiers ["left_shift"]}],
                    :from {:simultaneous [{:key_code "v"}
                                          {:key_code "j"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}},
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}
                                 {:bundle_identifiers ["^com\\.apple\\.Terminal$"
                                                       "^com\\.googlecode\\.iterm2$"
                                                       "^co\\.zeit\\.hyperterm$"
                                                       "^co\\.zeit\\.hyper$"
                                                       "^io\\.alacritty$"
                                                       "^net\\.kovidgoyal\\.kitty$"
                                                       "^org\\.vim\\."
                                                       "^com\\.qvacua\\.VimR$"],
                                  :type "frontmost_application_unless"}]}
                   {:from {:key_code "k",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "up_arrow",
                          :modifiers ["left_shift"]}],
                    :conditions [{:name "vi-visual-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-visual-mode",
                                         :value 1}}
                         {:key_code "up_arrow",
                          :modifiers ["left_shift"]}],
                    :from {:simultaneous [{:key_code "v"}
                                          {:key_code "k"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}},
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}
                                 {:bundle_identifiers ["^com\\.apple\\.Terminal$"
                                                       "^com\\.googlecode\\.iterm2$"
                                                       "^co\\.zeit\\.hyperterm$"
                                                       "^co\\.zeit\\.hyper$"
                                                       "^io\\.alacritty$"
                                                       "^net\\.kovidgoyal\\.kitty$"
                                                       "^org\\.vim\\."
                                                       "^com\\.qvacua\\.VimR$"],
                                  :type "frontmost_application_unless"}]}
                   {:from {:key_code "h",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "left_arrow",
                          :modifiers ["left_shift"]}],
                    :conditions [{:name "vi-visual-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-visual-mode",
                                         :value 1}}
                         {:key_code "left_arrow",
                          :modifiers ["left_shift"]}],
                    :from {:simultaneous [{:key_code "v"}
                                          {:key_code "h"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}},
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}
                                 {:bundle_identifiers ["^com\\.apple\\.Terminal$"
                                                       "^com\\.googlecode\\.iterm2$"
                                                       "^co\\.zeit\\.hyperterm$"
                                                       "^co\\.zeit\\.hyper$"
                                                       "^io\\.alacritty$"
                                                       "^net\\.kovidgoyal\\.kitty$"
                                                       "^org\\.vim\\."
                                                       "^com\\.qvacua\\.VimR$"],
                                  :type "frontmost_application_unless"}]}
                   {:from {:key_code "l",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "right_arrow",
                          :modifiers ["left_shift"]}],
                    :conditions [{:name "vi-visual-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-visual-mode",
                                         :value 1}}
                         {:key_code "right_arrow",
                          :modifiers ["left_shift"]}],
                    :from {:simultaneous [{:key_code "v"}
                                          {:key_code "l"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}},
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}
                                 {:bundle_identifiers ["^com\\.apple\\.Terminal$"
                                                       "^com\\.googlecode\\.iterm2$"
                                                       "^co\\.zeit\\.hyperterm$"
                                                       "^co\\.zeit\\.hyper$"
                                                       "^io\\.alacritty$"
                                                       "^net\\.kovidgoyal\\.kitty$"
                                                       "^org\\.vim\\."
                                                       "^com\\.qvacua\\.VimR$"],
                                  :type "frontmost_application_unless"}]}
                   {:from {:key_code "b",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "left_arrow",
                          :modifiers ["left_option" "left_shift"]}],
                    :conditions [{:name "vi-visual-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-visual-mode",
                                         :value 1}}
                         {:key_code "left_arrow",
                          :modifiers ["left_option" "left_shift"]}],
                    :from {:simultaneous [{:key_code "v"}
                                          {:key_code "b"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}},
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}
                                 {:bundle_identifiers ["^com\\.apple\\.Terminal$"
                                                       "^com\\.googlecode\\.iterm2$"
                                                       "^co\\.zeit\\.hyperterm$"
                                                       "^co\\.zeit\\.hyper$"
                                                       "^io\\.alacritty$"
                                                       "^net\\.kovidgoyal\\.kitty$"
                                                       "^org\\.vim\\."
                                                       "^com\\.qvacua\\.VimR$"],
                                  :type "frontmost_application_unless"}]}
                   {:from {:key_code "w",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "right_arrow",
                          :modifiers ["left_option" "left_shift"]}],
                    :conditions [{:name "vi-visual-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-visual-mode",
                                         :value 1}}
                         {:key_code "right_arrow",
                          :modifiers ["left_option" "left_shift"]}],
                    :from {:simultaneous [{:key_code "v"}
                                          {:key_code "w"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}},
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}
                                 {:bundle_identifiers ["^com\\.apple\\.Terminal$"
                                                       "^com\\.googlecode\\.iterm2$"
                                                       "^co\\.zeit\\.hyperterm$"
                                                       "^co\\.zeit\\.hyper$"
                                                       "^io\\.alacritty$"
                                                       "^net\\.kovidgoyal\\.kitty$"
                                                       "^org\\.vim\\."
                                                       "^com\\.qvacua\\.VimR$"],
                                  :type "frontmost_application_unless"}]}
                   {:from {:key_code "0",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "left_arrow",
                          :modifiers ["left_command" "left_shift"]}],
                    :conditions [{:name "vi-visual-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-visual-mode",
                                         :value 1}}
                         {:key_code "left_arrow",
                          :modifiers ["left_command" "left_shift"]}],
                    :from {:simultaneous [{:key_code "v"}
                                          {:key_code "0"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}},
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}
                                 {:bundle_identifiers ["^com\\.apple\\.Terminal$"
                                                       "^com\\.googlecode\\.iterm2$"
                                                       "^co\\.zeit\\.hyperterm$"
                                                       "^co\\.zeit\\.hyper$"
                                                       "^io\\.alacritty$"
                                                       "^net\\.kovidgoyal\\.kitty$"
                                                       "^org\\.vim\\."
                                                       "^com\\.qvacua\\.VimR$"],
                                  :type "frontmost_application_unless"}]}
                   {:from {:key_code "4",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "right_arrow",
                          :modifiers ["left_command" "left_shift"]}],
                    :conditions [{:name "vi-visual-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-visual-mode",
                                         :value 1}}
                         {:key_code "right_arrow",
                          :modifiers ["left_command" "left_shift"]}],
                    :from {:simultaneous [{:key_code "v"}
                                          {:key_code "4"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}},
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}
                                 {:bundle_identifiers ["^com\\.apple\\.Terminal$"
                                                       "^com\\.googlecode\\.iterm2$"
                                                       "^co\\.zeit\\.hyperterm$"
                                                       "^co\\.zeit\\.hyper$"
                                                       "^io\\.alacritty$"
                                                       "^net\\.kovidgoyal\\.kitty$"
                                                       "^org\\.vim\\."
                                                       "^com\\.qvacua\\.VimR$"],
                                  :type "frontmost_application_unless"}]}
                   {:from {:key_code "open_bracket",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "up_arrow",
                          :modifiers ["left_option" "left_shift"]}],
                    :conditions [{:name "vi-visual-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-visual-mode",
                                         :value 1}}
                         {:key_code "up_arrow",
                          :modifiers ["left_option" "left_shift"]}],
                    :from {:simultaneous [{:key_code "v"}
                                          {:key_code "open_bracket"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}},
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}
                                 {:bundle_identifiers ["^com\\.apple\\.Terminal$"
                                                       "^com\\.googlecode\\.iterm2$"
                                                       "^co\\.zeit\\.hyperterm$"
                                                       "^co\\.zeit\\.hyper$"
                                                       "^io\\.alacritty$"
                                                       "^net\\.kovidgoyal\\.kitty$"
                                                       "^org\\.vim\\."
                                                       "^com\\.qvacua\\.VimR$"],
                                  :type "frontmost_application_unless"}]}
                   {:from {:key_code "close_bracket",
                           :modifiers {:optional ["any"]}},
                    :to [{:key_code "down_arrow",
                          :modifiers ["left_option" "left_shift"]}],
                    :conditions [{:name "vi-visual-mode",
                                  :value 1,
                                  :type "variable_if"}],
                    :type "basic"}
                   {:parameters {:basic.simultaneous_threshold_milliseconds 250},
                    :to [{:set_variable {:name "vi-visual-mode",
                                         :value 1}}
                         {:key_code "down_arrow",
                          :modifiers ["left_option" "left_shift"]}],
                    :from {:simultaneous [{:key_code "v"}
                                          {:key_code "close_bracket"}],
                           :simultaneous_options {:detect_key_down_uninterruptedly false,
                                                  :key_down_order "strict",
                                                  :key_up_order "strict_inverse",
                                                  :key_up_when "any"}},
                    :conditions [{:name "vi-mode",
                                  :value 1,
                                  :type "variable_if"}
                                 {:bundle_identifiers ["^com\\.apple\\.Terminal$"
                                                       "^com\\.googlecode\\.iterm2$"
                                                       "^co\\.zeit\\.hyperterm$"
                                                       "^co\\.zeit\\.hyper$"
                                                       "^io\\.alacritty$"
                                                       "^net\\.kovidgoyal\\.kitty$"
                                                       "^org\\.vim\\."
                                                       "^com\\.qvacua\\.VimR$"],
                                  :type "frontmost_application_unless"}]}]}])

(t/deftest generate-conf
  (init-conf-data)
  (t/testing "generate vi-mode config"
    (t/is (= (sut/generate vi-mode-example) result))))