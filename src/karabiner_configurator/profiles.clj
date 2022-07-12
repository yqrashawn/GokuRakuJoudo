(ns karabiner-configurator.profiles
  (:require
   [karabiner-configurator.data :as d]
   [karabiner-configurator.misc :refer [massert]]))

(defn parse-profiles
  "Parse profiles and save it in conf data for later use."
  [profiles]
  (massert (map? profiles) "Invalid profiles. Must be a map")
  (massert (= (count (filter #(:default (second %)) profiles)) 1)
           "There should be one and only one default profile")
  (let [_user-default-profile (d/update-user-default-profile-name
                               (first (first (filter #(:default (second %)) profiles))))]
    (doseq [profile profiles
            :let [[name {:keys [held sim delay alone]}] profile]]
      (d/assoc-in-conf-data
       [:profiles name]
       {:complex_modifications
        {:parameters
         {:basic.simultaneous_threshold_milliseconds sim
          :basic.to_delayed_action_delay_milliseconds delay
          :basic.to_if_alone_timeout_milliseconds alone
          :basic.to_if_held_down_threshold_milliseconds held}}}))))
(defn parse-rules
  "Parse generated rules into profiles"
  [rules]
  (into
   {}
   (for [[profile-name profile-complex-modification] rules]
     {profile-name
      (assoc-in (profile-name
                 (:profiles @d/conf-data))
                [:complex_modifications :rules]
                profile-complex-modification)})))
