(ns karabiner-configurator.rules
  (:require
   [karabiner-configurator.misc :refer :all]))


(defn from-key
  "generate normal from key config"
  [from])

(defn to-key
  "generate to config"
  [to])

(defn parse-optional-arg [arg])

(defn parse-rule
  "generate one configuration"
  ([from to]
   (from-key from) (to-key to))
  ([from to conditions]
   (from-key from) (to-key to) (parse-optional-arg conditions))
  ([from to conditions other-options]
   (from-key from) (to-key to) (parse-optional-arg conditions) (parse-optional-arg other-options)))

(def example-rule [[:a :1] ;; a to 1
                   [:Ca :$1] ;; command a to control 1
                   [:my-spacebar :$1] ;; my-spacebar to control 1
                   [:b [:1 :2]]  ;; key to key
                   [:c "osascript -e 'display dialog \"example apple script\"'"] ;; key to shell script
                   [:d [:1 "osascript -e 'display dialog \"example apple script\"'"]] ;; key to key then shell script
                   [[:e :f] :3] ;; simultaneous key to key
                   [:g :4 :vi-mode] ;; vi-mode is 1
                   [:h :5 :!vi-mode] ;; vi-mode is 0
                   [:i :6 :hhkb-bt] ;; key to key in layer b (in layer a) specific to hhkb-bt device
                   [:j :7 [:vi-mode :hhkb-bt]] ;; multiple condition
                   [:h [:8 {:set ["some-mode" 0]}]]])

(defn parse-rules
  [rules]
  (doseq [rule rules]
    (let [[from to condition other-options] rule]
      (assert (and (nn? from) (nn? to)) (str "invalid rule:" rule ", <from> or <to> is nil"))
      (cond (nil? condition) (parse-rule from to)
            (and (nil? other-options) (nn? condition)) (parse-rule from to condition)
            (and (nn? condition) (nn? other-options)) (parse-rule from to condition other-options)))))