(ns com.jd.bdp.magpie.magpie-proxy.utils
  (:require [clojure.tools.logging :as log]

            [clj-zookeeper.zookeeper :as zk]
            [com.jd.bdp.magpie.util.utils :as m-utils])

  (:import [com.jd.magpie.client MagpieClient]))

(defn mock-echo-str
  "test fn"
  [a-str]
  (str "Hi " a-str))

(defn check-magpie-zookeeper
  "check magpie zookeeper adress"
  [zk-str]
  (with-open [client (zk/new-client zk-str)]
    (try
      (zk/check-exists? "/magpie/nimbus")
      (catch Exception e
        (log/error e)
        false))))

(defn get-active-nimbus
  "get magpie active nimbus from zookeeper"
  [zk-str]
  (with-open [client (zk/new-client zk-str)]
    (try
      (let [nimbuses (zk/get-children "/magpie/nimbus")]
        (if (< (.size nimbuses) 1)
          {:success false :info "NO ACTIVE NIMBUSES!"}
          (let [nodes (to-array nimbuses)]
            (java.util.Arrays/sort nodes)
            (let [active-nimbus (m-utils/bytes->map (zk/get-data (str "/magpie/nimbus/" (first nodes))))]
              (log/info "active nimbus:" active-nimbus)
              {:success true :active-nimbus active-nimbus}))))
      (catch Exception e
        (log/error e)
        {:success false :info (.toString e) :retry true}))))

(defn get-one-magpie-client
  "get one magpie client
  success return true"
  [nimbus-ip nimbus-port]
  (.getClient (MagpieClient. (hash-map) nimbus-ip nimbus-port)))

(defn submit-task
  "magpie submit a task"
  [nimbus-ip nimbus-port task-id jar klass group type]
  (try
    (let [client (get-one-magpie-client nimbus-ip nimbus-port)
          re (m-utils/string->map (.submitTask client task-id jar klass group type))]
      (log/info re)
      {:success true})
    (catch Exception e
      (log/error e)
      {:success false :info (.toString e)})))

(defn get-task-info
  "get one task info from zookeeper"
  [zk-str task-id]
  (with-open [client (zk/new-client zk-str)]
    (try
      (let [task-info (m-utils/bytes->map (zk/get-data (str "/magpie/assignments/" task-id)))]
        (log/info "get task info:" task-info)
        {:success true :task-info task-info})
      (catch Exception e
        (log/error e)
        {:success false :info (.toString e)}))))
