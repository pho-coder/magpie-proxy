(ns com.jd.bdp.magpie.magpie-proxy.utils
  (:require [clojure.tools.logging :as log]

            [clj-zookeeper.zookeeper :as zk]
            [com.jd.bdp.magpie.util.utils :as m-utils])

  (:import [com.jd.bdp.magpie.client MagpieClient]))

(defn mock-echo-str
  "test fn"
  [a-str]
  (str "Hi " a-str))

(defn check-magpie-zookeeper
  "check magpie zookeeper adress
   returncode 0 : success
              1 : no zk node
             -1 : unknown error"
  [zk-str]
  (with-open [client (zk/new-client zk-str)]
    (try
      (if (zk/check-exists? client "/magpie/nimbus")
        {:success true
         :returncode 0}
        {:success false
         :returncode 1
         :info "zk node not exists!"})
      (catch Exception e
        (log/error e)
        {:success false
         :returncode -1
         :info (.toString e)}))))

(defn get-active-nimbus
  "get magpie active nimbus from zookeeper
  returncode 0 : success
             1 : no nimbus
            -1 : unknown error"
  [zk-str]
  (with-open [client (zk/new-client zk-str)]
    (try
      (let [nimbuses (zk/get-children client "/magpie/nimbus")]
        (if (< (.size nimbuses) 1)
          {:success false
           :info "NO ACTIVE NIMBUSES!"
           :returncode 1}
          (let [nodes (to-array nimbuses)]
            (java.util.Arrays/sort nodes)
            (let [active-nimbus (m-utils/bytes->map (zk/get-data client (str "/magpie/nimbus/" (first nodes))))]
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

(defn operate-task
  "magpie operate a task: kill pause active reload
   returncode 0 : task id exists and command submit success
              1 : command is unsupported
              2 : task id not exists
             -1 : unknown error"
  [nimbus-ip nimbus-port task-id command]
  (try
    (let [client (get-one-magpie-client nimbus-ip nimbus-port)
          re (m-utils/string->map (.operateTask client task-id command))]
      {:success (get re "success")
       :info (get re "info")
       :returncode (get re "returncode")})
    (catch Exception e
      (log/error e)
      {:success false
       :info (.toString e)
       :returncode -1})))

(defn get-task-info
  "get one task info from zookeeper
   returncode 0 : success
              1 : task id not exists
             -1 : unknown error"
  [zk-str task-id]
  (with-open [client (zk/new-client zk-str)]
    (try
      (let [task-info-bytes (zk/get-data client (str "/magpie/assignments/" task-id))]
        (if (nil? task-info-bytes)
          {:success false
           :info (str "task id: " task-id " NOT EXISTS!")
           :returncode 1}
          (let [task-info (m-utils/bytes->map task-info-bytes)
                task-status (m-utils/bytes->string (zk/get-data client (str "/magpie/status/" task-id)))]
            {:success true
             :task-info (assoc task-info "status" task-status)
             :returncode 0})))
      (catch Exception e
        (log/error e)
        {:success false
         :info (.toString e)
         :returncode -1}))))

(defn get-tasks-info
  "get tasks list from assignments
  returncode 0 : success
            -1 : unknown error
             1 : get assignments tasks list error"
  [zk-str]
  (try
    (with-open [client (zk/new-client zk-str)]
      (let [tasks-list (zk/get-children client "/magpie/assignments")]
        (if (nil? tasks-list)
          {:success false
           :info (str "get assignments error!")
           :returncode 1}
          {:success true
           :tasks-info (doall (filter #(not (nil? %))
                                      (map #(let [task-id %
                                                  task-info-bytes (try
                                                                    (zk/get-data client (str "/magpie/assignments/" task-id))
                                                                    (catch org.apache.zookeeper.KeeperException$NoNodeException e
                                                                      (log/warn e)
                                                                      nil))]
                                              (if (nil? task-info-bytes)
                                                nil
                                                (assoc (m-utils/bytes->map task-info-bytes)
                                                       "status"
                                                       (m-utils/bytes->string (zk/get-data client (str "/magpie/status/" task-id))))))
                                           tasks-list)))
           :returncode 0})))
    (catch Exception e
      (log/error e)
      {:success false
       :info (.toString e)
       :returncode -1})))

(defn get-supervisors-info
  "get all supervisors info in one cluster
   returncode 0 : success
             -1 : unknown error
              1 : get supervisors list error"
  [zk-str]
  (try
    (with-open [client (zk/new-client zk-str)]
      (let [supervisors-list (zk/get-children client "/magpie/supervisors")]
        (if (nil? supervisors-list)
          {:success false
           :info "get supervisors error!"
           :returncode 1}
          {:success true
           :returncode 0
           :supervisors-info (doall (filter #(not (nil? %))
                                            (map #(let [supervisor-id %
                                                        supervisor-info-bytes (try
                                                                                (zk/get-data client (str "/magpie/supervisors/" supervisor-id))
                                                                                (catch org.apache.zookeeper.KeeperException$NoNodeException e
                                                                                  (log/warn e)
                                                                                  nil))]
                                                    (if (nil? supervisor-info-bytes)
                                                      nil
                                                      (assoc (m-utils/bytes->map supervisor-info-bytes)
                                                             "tasks"
                                                             (let [tasks-list (zk/get-children client (str "/magpie/yourtasks/" supervisor-id))]
                                                               (if (nil? tasks-list)
                                                                 nil
                                                                 (map (fn [task-id]
                                                                        (let [task-bytes (try
                                                                                           (let [cclient (zk/new-client zk-str)]
                                                                                             (zk/get-data cclient (str "/magpie/yourtasks/" supervisor-id "/" task-id)))
                                                                                           (catch org.apache.zookeeper.KeeperException$NoNodeException e
                                                                                             (log/warn e)
                                                                                             nil))]
                                                                          {"id" task-id
                                                                           "assign-time" (if (nil? task-bytes)
                                                                                           nil
                                                                                           (get (m-utils/bytes->map task-bytes)
                                                                                                "assign-time"))}))
                                                                      tasks-list)
                                                                 )))))
                                                 supervisors-list)))})))
    (catch Exception e
      (log/error e)
      {:success false
       :info (.toString e)
       :returncode -1})))
