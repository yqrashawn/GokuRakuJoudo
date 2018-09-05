(defproject karabiner-configurator "0.1.0-SNAPSHOT"
  :description "karabiner configuration manager"
  :url "http://example.com/FIXME"
  :license {:name "GNU General Public License v3.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main karabiner-configurator.core
  :aot :all
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [prismatic/schema "1.1.9"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.clojure/tools.cli "0.3.7"]
                 [me.raynes/fs "1.4.6"]
                 [cheshire "5.8.0"]])

