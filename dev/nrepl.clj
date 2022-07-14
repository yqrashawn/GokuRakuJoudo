(ns nrepl
  (:require [babashka.fs :as fs]
            [babashka.process :as p :refer [process]]
            [babashka.wait :as wait]))

(defn run []
  (let [port (with-open [sock (java.net.ServerSocket. 0)] (.getLocalPort sock))
        proc (process (str "bb nrepl-server " port) {:inherit true})]
    (wait/wait-for-port "localhost" port)
    (spit ".nrepl-port" port)
    (fs/delete-on-exit ".nrepl-port")
    (deref proc)))
