(ns karabiner-configurator.profiles
  (:require
   [karabiner-configurator.misc :refer :all]
   [karabiner-configurator.data :refer :all]))

(defn generate-profiles [profiles]
  (massert (= (count (filter #(= (:default %) true) profiles)) 1)
           "There should be one and only one default profile")
  (for [[{:keys [name held sim delay alone default]} profile] profiles]
    {:name name
     :complex_modifications
     {:parameters
      {:basic.simultaneous_threshold_milliseconds sim
       :basic.to_delayed_action_delay_milliseconds delay
       :basic.to_if_alone_timeout_milliseconds alone
       :basic.to_if_held_down_threshold_milliseconds held}
      :rules []}}))

(defn parse-profiles [profiles]
  (massert (vector? profiles) "Invalid profiles. Must be a vector")
  (generate-profiles profiles))
