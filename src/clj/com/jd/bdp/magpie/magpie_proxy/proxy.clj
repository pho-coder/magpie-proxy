(ns com.jd.bdp.magpie.magpie-proxy.proxy
  (:gen-class)

  (:require [clojure.tools.logging :as log]
            [com.jd.bdp.magpie.util.utils :as m-utils]

            [com.jd.bdp.magpie.magpie-proxy.utils :as utils]
            [com.jd.bdp.magpie.magpie-proxy.jsf-utils :as jsf-utils]))

(def CONF (atom nil))

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

(defn submit-task
  "submit task"
  [cluster-id task-id jar klass group type]
  (let [zk-str (get (get @CONF "clusters") cluster-id)
        active-nimbus (utils/get-active-nimbus zk-str)]))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (log/info "Hello, magpie proxy!")
  (let [conf-file "magpie-proxy.yaml"
        conf (m-utils/find-yaml conf-file true)]
    (reset! CONF conf)
    (log/info CONF)
    (loop [clusters (get conf "clusters")]
      (if-not (empty? clusters)
        (let [cluster (first clusters)
              name (key cluster)
              zk-str (val cluster)]
          (if (utils/check-magpie-zookeeper zk-str)
            (log/info "cluster" name "is OK!")
            (log/error "cluster" name "is NOT OK!"))
          (recur (rest clusters)))))
    (submit-task "raven" "test-1" "a.jar" "com.a" "default" "memory"))
  ;(utils/start-jsf-server)
  (while true
    (Thread/sleep 10000)))
