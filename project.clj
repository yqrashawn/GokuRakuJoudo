(defproject karabiner-configurator "0.1.0"
  :description "karabiner configuration manager"
  :url "http://https://github.com/yqrashawn/GokuRakuJoudo"
  :license {:name "GNU General Public License v3.0"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :main karabiner-configurator.core
  :aot :all
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "1.0.206"]
                 [com.taoensso/timbre "5.2.1"]
                 [djblue/portal "0.28.0"]
                 [babashka/fs "0.1.6"]
                 [cheshire "5.10.2"]]
  :plugins [[lein-cloverage "1.2.3"]])


