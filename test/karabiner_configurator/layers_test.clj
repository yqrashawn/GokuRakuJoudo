(ns karabiner-configurator.layers-test
  (:require [karabiner-configurator.layers :as sut]
            [karabiner-configurator.data :refer :all]
            [clojure.test :as t]))


(def simlayers-example {:vi-mode {:key :d}
                        :chrome-mode {:key :d
                                      :condi [:chromes]}
                        :non-chrome-mode {:key :a
                                          :condi [:!chromes]}
                        :simple-condi-defination-test {:key :a
                                                       :condi ["foo" 1]}})

(def result
  {:applications {:chrome ["^com\\.google\\.Chrome$"],
                  :chrom-canary ["^com\\.google\\.Chrome\\.canary$"],
                  :chromes ["^com\\.google\\.Chrome$"
                            "^com\\.google\\.Chrome\\.canary$"]},
   :tos {},
   :input-source {},
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
                                :simo {:dorder :strict,
                                       :uorder :strict_inverse,
                                       :afterup {:set ["vi-mode" 0]}}}},
               :chrome-mode {:type "basic",
                             :parameters {:basic.simultaneous_threshold_milliseconds 250},
                             :to [{:set ["chrome-mode" 1]}],
                             :from {:sim [:d],
                                    :simo {:dorder :strict,
                                           :uorder :strict_inverse,
                                           :afterup {:set ["chrome-mode" 0]}}},
                             :conditions [{:bundle_identifiers ["^com\\.google\\.Chrome$"
                                                                "^com\\.google\\.Chrome\\.canary$"],
                                           :type "frontmost_application_if"}]},
               :non-chrome-mode {:type "basic",
                                 :parameters {:basic.simultaneous_threshold_milliseconds 250},
                                 :to [{:set ["non-chrome-mode" 1]}],
                                 :from {:sim [:a],
                                        :simo {:dorder :strict,
                                               :uorder :strict_inverse,
                                               :afterup {:set ["non-chrome-mode" 0]}}},
                                 :conditions [{:bundle_identifiers ["^com\\.google\\.Chrome$"
                                                                    "^com\\.google\\.Chrome\\.canary$"],
                                               :type "frontmost_application_unless"}]},
               :simple-condi-defination-test {:type "basic",
                                              :parameters {:basic.simultaneous_threshold_milliseconds 250},
                                              :to [{:set ["simple-condi-defination-test"
                                                          1]}],
                                              :from {:sim [:a],
                                                     :simo {:dorder :strict,
                                                            :uorder :strict_inverse,
                                                            :afterup {:set ["simple-condi-defination-test"
                                                                            0]}}},
                                              :conditions [{:name "foo",
                                                            :value 1,
                                                            :type "variable_if"}]}}})



(t/deftest convert-simlayers
  (init-conf-data)
  (update-conf-data (assoc conf-data :devices {:hhkb-bt [{:vendor_id 1278 :product_id 51966}]
                                               :hhkb [{:vendor_id 2131 :product_id 256}]}))
  (update-conf-data (assoc conf-data :applications {:chrome ["^com\\.google\\.Chrome$"]
                                                    :chrom-canary [ "^com\\.google\\.Chrome\\.canary$"]
                                                    :chromes ["^com\\.google\\.Chrome$" "^com\\.google\\.Chrome\\.canary$"]}))
  (t/testing
      (t/is (= (sut/generate-simlayers simlayers-example) result))))


