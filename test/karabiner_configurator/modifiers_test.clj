(ns karabiner-configurator.modifiers-test
  (:require [clojure.test :refer [deftest is testing]]
            [karabiner-configurator.data :refer [init-conf-data]]
            [karabiner-configurator.modifiers :as sut]))

(def example-modifers {:modifiers {:111 [:left_command :left_control]
                                   :222 {:mandatory [:left_command :left_shift]}
                                   :3 {:mandatory :left_command}
                                   :444 {:optional :any}}})

(def result {:applications {},
             :tos {},
             :input-sources {},
             :modifiers {:111 {:mandatory ["left_command" "left_control"]},
                         :222 {:mandatory ["left_command" "left_shift"]},
                         :3 {:mandatory ["left_command"]},
                         :444 {:optional ["any"]}},
             :simlayer-threshold 250,
             :devices {},
             :layers {},
             :froms {},
             :simlayers {}
             :profiles {:Default
                        {:sim 50,:delay 500,:alone 1000,:held 500,:default true}}})

(deftest convert-modifiers
  (init-conf-data)
  (testing "FIXME, convert-modifiers fail."
    (is (= (sut/generate (:modifiers example-modifers)) result))))
