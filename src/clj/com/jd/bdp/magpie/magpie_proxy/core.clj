(ns com.jd.bdp.magpie.magpie-proxy.core
  (:gen-class)

  (:require [clojure.tools.logging :as log]

            [com.jd.bdp.magpie.magpie-proxy.utils :as utils]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (log/info "Hello, World!")
  (utils/start-jsf-server)
  (while true
    (Thread/sleep 10000)))
