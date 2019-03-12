(ns karabiner-configurator.core-test
  (:require [clojure.test :as t]
            [karabiner-configurator.core :as sut]
            [karabiner-configurator.data :refer :all]
            [karabiner-configurator.misc :refer :all]))

(def vi-mode-example (load-edn "resources/configurations/edn/vi_mode.edn"))
(def launch-mode-example (load-edn "resources/configurations/edn/launch_mode.edn"))

(def vi-mode-result (load-result-edn "resources/configurations/generated_edn/vi_mode.edn"))
(def launch-mode-result (load-result-edn "resources/configurations/generated_edn/launch_mode.edn"))

(t/deftest generate-conf
  (init-conf-data)
  (t/testing "generate vi-mode config"
    (t/is (= (sut/parse-edn vi-mode-example) vi-mode-result)))
  (init-conf-data)
  (t/testing "generate launch-mode config"
    (t/is (= (sut/parse-edn launch-mode-example) launch-mode-result))))