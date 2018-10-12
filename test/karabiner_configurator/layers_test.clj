(ns karabiner-configurator.layers-test
  (:require [karabiner-configurator.layers :as sut]
            [karabiner-configurator.data :refer :all]
            [clojure.test :as t]))

(def layers-example {:tab-mode {:key :tab :afterup [{:set ["chunkwm-move-mode" 0]}
                                                    {:set ["chunkwm-scale-mode" 0]}]}
                     :chunkwm-move-mode {:key :f :condi :tab-mode}
                     :chunkwm-scale-mode {:key :c :condi :tab-mode}
                     :hyper-mode {:key :caps_lock :alone :escape}})

(def layer-result
  {:applications {:chrome ["^com\\.google\\.Chrome$"],
                  :chrom-canary ["^com\\.google\\.Chrome\\.canary$"],
                  :chromes ["^com\\.google\\.Chrome$"
                            "^com\\.google\\.Chrome\\.canary$"]},
   :tos {},
   :modifiers {},
   :simlayer-threshold 250,
   :input-sources {},
   :devices {:hhkb-bt [{:vendor_id 1278,
                        :product_id 51966}],
             :hhkb [{:vendor_id 2131,
                     :product_id 256}]},
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
                                       :type "variable_if"}]}
    :hyper-mode {:type "basic",
                 :to [{:set_variable {:name "hyper-mode", :value 1}}],
                 :from {:key_code "caps_lock"},
                 :to_after_key_up [{:set_variable {:name "hyper-mode",
                                                   :value 0}}],
                 :to_if_alone [{:key_code "escape"}]}}
   :froms {},
   :simlayers {}})

(def simlayers-example {:vi-mode {:key :d}
                        :test-to-after-key-up-mode {:key :l :afterup {:set ["foo" 0]}}
                        :chrome-mode {:key :d
                                      :condi [:chromes]}
                        :non-chrome-mode {:key :a
                                          :condi [:!chromes]}
                        :simple-condi-definition-test {:key :a
                                                       :condi ["foo" 1]}})

(def simlayer-result
  {:applications {:chrome ["^com\\.google\\.Chrome$"],
                  :chrom-canary ["^com\\.google\\.Chrome\\.canary$"],
                  :chromes ["^com\\.google\\.Chrome$"
                            "^com\\.google\\.Chrome\\.canary$"]},
   :tos {},
   :input-sources {},
   :modifiers {},
   :simlayer-threshold 250,
   :devices {:hhkb-bt [{:vendor_id 1278,
                        :product_id 51966}],
             :hhkb [{:vendor_id 2131,
                     :product_id 256}]},
   :layers {},
   :froms {},
   :simlayers {:vi-mode {:type "basic",
                         :parameters {:basic.simultaneous_threshold_milliseconds 250},
                         :to [{:set ["vi-mode" 1]}],
                         :from {:sim [:d],
                                :simo {:interrupt true,
                                       :dorder :strict,
                                       :uorder :strict_inverse,
                                       :afterup {:set ["vi-mode" 0]}}}},
               :test-to-after-key-up-mode {:type "basic",
                                           :parameters {:basic.simultaneous_threshold_milliseconds 250},
                                           :to [{:set ["test-to-after-key-up-mode" 1]}],
                                           :from {:sim [:l],
                                                  :simo {:interrupt true,
                                                         :dorder :strict,
                                                         :uorder :strict_inverse,
                                                         :afterup [{:set ["test-to-after-key-up-mode" 0]}
                                                                   {:set ["foo" 0]}]}}}
               :chrome-mode {:type "basic",
                             :parameters {:basic.simultaneous_threshold_milliseconds 250},
                             :to [{:set ["chrome-mode" 1]}],
                             :from {:sim [:d],
                                    :simo {:interrupt true,
                                           :dorder :strict,
                                           :uorder :strict_inverse,
                                           :afterup {:set ["chrome-mode" 0]}}},
                             :conditions [{:bundle_identifiers ["^com\\.google\\.Chrome$"
                                                                "^com\\.google\\.Chrome\\.canary$"],
                                           :type "frontmost_application_if"}]},
               :non-chrome-mode {:type "basic",
                                 :parameters {:basic.simultaneous_threshold_milliseconds 250},
                                 :to [{:set ["non-chrome-mode" 1]}],
                                 :from {:sim [:a],
                                        :simo {:interrupt true,
                                               :dorder :strict,
                                               :uorder :strict_inverse,
                                               :afterup {:set ["non-chrome-mode" 0]}}},
                                 :conditions [{:bundle_identifiers ["^com\\.google\\.Chrome$"
                                                                    "^com\\.google\\.Chrome\\.canary$"],
                                               :type "frontmost_application_unless"}]},
               :simple-condi-definition-test {:type "basic",
                                              :parameters {:basic.simultaneous_threshold_milliseconds 250},
                                              :to [{:set ["simple-condi-definition-test"
                                                          1]}],
                                              :from {:sim [:a],
                                                     :simo {:interrupt true,
                                                            :dorder :strict,
                                                            :uorder :strict_inverse,
                                                            :afterup {:set ["simple-condi-definition-test"
                                                                            0]}}},
                                              :conditions [{:name "foo",
                                                            :value 1,
                                                            :type "variable_if"}]}}})

(t/deftest convert-simlayers
  (init-conf-data)
  (update-conf-data (assoc conf-data :devices {:hhkb-bt [{:vendor_id 1278 :product_id 51966}]
                                               :hhkb [{:vendor_id 2131 :product_id 256}]}))
  (update-conf-data (assoc conf-data :applications {:chrome ["^com\\.google\\.Chrome$"]
                                                    :chrom-canary ["^com\\.google\\.Chrome\\.canary$"]
                                                    :chromes ["^com\\.google\\.Chrome$" "^com\\.google\\.Chrome\\.canary$"]}))
  ;; (t/testing
  ;;   (t/is (= (sut/generate-layers layers-example) layer-result))
  ;;   (t/is (= (sut/generate-simlayers simlayers-example) simlayer-result)))
  (t/testing
   "testing simlayers"
    (sut/generate-simlayers simlayers-example)
    (t/is (= conf-data simlayer-result))))

(t/deftest convert-layers
  (init-conf-data)
  (update-conf-data (assoc conf-data :devices {:hhkb-bt [{:vendor_id 1278 :product_id 51966}]
                                               :hhkb [{:vendor_id 2131 :product_id 256}]}))
  (update-conf-data (assoc conf-data :applications {:chrome ["^com\\.google\\.Chrome$"]
                                                    :chrom-canary ["^com\\.google\\.Chrome\\.canary$"]
                                                    :chromes ["^com\\.google\\.Chrome$" "^com\\.google\\.Chrome\\.canary$"]}))
  (t/testing
   "testing layers"
    (sut/generate-layers layers-example)
    (t/is (= conf-data layer-result))))
