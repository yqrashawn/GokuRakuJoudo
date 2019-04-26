(defproject karabiner-configurator "0.1.0"
  :description "karabiner configuration manager"
  :url "http://https://github.com/yqrashawn/GokuRakuJoudo"
  :license {:name "GNU General Public License v3.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main karabiner-configurator.core
  :aot :all
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [prismatic/schema "1.1.9"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.clojure/tools.cli "0.4.2"]
                 [me.raynes/fs "1.4.6"]
                 [cheshire "5.8.0"]
                 [environ "1.1.0"]]
  :plugins [[lein-cloverage "1.0.13"]
            [lein-environ "1.1.0"]]
  :profiles {:dev {:env {:is-dev true}}
             :test {:env {:is-dev true}}})


