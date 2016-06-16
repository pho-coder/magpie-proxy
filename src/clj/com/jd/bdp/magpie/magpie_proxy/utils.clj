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
  "get magpie active nimbus from zookeeper
  returncode 0 : success
             1 : no nimbus
            -1 : unknown error"
  [zk-str]
  (with-open [client (zk/new-client zk-str)]
    (try
      (let [nimbuses (zk/get-children "/magpie/nimbus")]
        (if (< (.size nimbuses) 1)
          {:success false
           :info "NO ACTIVE NIMBUSES!"
           :returncode 1}
          (let [nodes (to-array nimbuses)]
            (java.util.Arrays/sort nodes)
            (let [active-nimbus (m-utils/bytes->map (zk/get-data (str "/magpie/nimbus/" (first nodes))))]
              (log/info "active nimbus:" active-nimbus)
              {:success true
               :active-nimbus active-nimbus
               :returncode 0}))))
      (catch Exception e
        (log/error e)
        {:success false
         :info (.toString e)
         :returncode -1}))))

(defn get-one-magpie-client
  "get one magpie client
  success return true"
  [nimbus-ip nimbus-port]
  (.getClient (MagpieClient. (hash-map) nimbus-ip nimbus-port)))

(defn submit-task
  "magpie submit a task
  returncode 0 : submit success and is a new task id
             1 : submit success and task id exists
            -1 : submit error"
  [nimbus-ip nimbus-port task-id jar klass group type]
  (try
    (let [client (get-one-magpie-client nimbus-ip nimbus-port)
          re (m-utils/string->map (.submitTask client task-id jar klass group type))]
      (if (get re "success")
        (if (= (get re "returncode") 0)
          {:success true :returncode 0 :info (get re "info")}
          {:success true :returncode 1 :info (get re "info")})
        {:success false :returncode -1 :info (get re "info")}))
    (catch Exception e
      (log/error e)
      {:success false :info (.toString e) :returncode -1})))

(defn get-task-info
  "get one task info from zookeeper
   returncode 0 : success
              1 : task id not exists
             -1 : unknown error"
  [zk-str task-id]
  (with-open [client (zk/new-client zk-str)]
    (try
      (let [task-info-bytes (zk/get-data (str "/magpie/assignments/" task-id))]
        (if (nil? task-info-bytes)
          (do (log/warn "task id not exists:" task-id)
              {:success false
               :info (str "task id: " task-id " NOT EXISTS!")
               :returncode 1})
          (let [task-info (m-utils/bytes->map task-info-bytes)
                task-status (m-utils/bytes->string (zk/get-data (str "/magpie/status/" task-id)))]
            {:success true
             :info (assoc task-info "status" task-status)
             :returncode 0})))
      (catch Exception e
        (log/error e)
        {:success false
         :info (.toString e)
         :returncode -1}))))
