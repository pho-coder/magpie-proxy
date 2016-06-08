(ns com.jd.bdp.magpie.magpie-proxy.core
  (:gen-class)

  (:require [clojure.tools.logging :as log]
            [com.jd.bdp.magpie.util.utils :as m-utils]

            [com.jd.bdp.magpie.magpie-proxy.utils :as utils]))

(defn mock-get-task-status
  "mock get task status"
  [cluster-id task-id]
  (log/info cluster-id task-id)
  (str cluster-id " " task-id))

(defn mock-submit-task
  "mock submit task"
  [cluster-id task-id jar klass group type]
  (log/info task-id group)
  (str cluster-id " " task-id " " jar " " klass " " group " " type))

(defn mock-operate-task
  "mock operate task"
  [cluster-id task-id command]
  (log/info task-id command)
  (str cluster-id " " task-id " " command))

(defn get-task-status
  "get task status"
  [cluster-id task-id]
  )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (log/info "Hello, World!")
  (let [conf-file "magpie-proxy.yaml"
        conf (m-utils/find-yaml conf-file true)
        _ (log/info conf)])
  ;(utils/start-jsf-server)
  ;; (while true
  ;;   (Thread/sleep 10000))
  )
