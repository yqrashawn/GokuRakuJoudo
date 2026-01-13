(defproject karabiner-configurator "0.1.0"
  :description "karabiner configuration manager"
  :url "http://https://github.com/yqrashawn/GokuRakuJoudo"
  :license {:name "GNU General Public License v3.0"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :main karabiner-configurator.core
  :aot :all
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.cli "1.0.206"]
                 [me.raynes/fs "1.4.6"]
                 [org.clojure/core.match "1.0.1"]
                 [babashka/process "0.1.7"]
                 [cheshire "5.10.2"]
                 [com.github.clj-easy/graal-build-time "0.1.4"]]
  :plugins [[lein-cloverage "1.2.3"]]
  :profiles {:socket {:jvm-opts ["-Dclojure.server.repl={:port 5555 :accept clojure.core.server/repl :server-daemon false}"]}})


