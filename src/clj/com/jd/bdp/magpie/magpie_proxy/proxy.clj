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

(defn get-magpie-zk-address
  "get magpie zookeeper address
  returncode 0 : success
             1 : no zk address from conf
             2 : NO MAGPIE on the zookeeper
            -1 : unknown error"
  [cluster-id]
  (let [zk-str (get (get @CONF "clusters") cluster-id)]
    (if (nil? zk-str)
      {:success false
       :returncode 1
       :info (str cluster-id " : no zk address from conf")}
      (let [re (utils/check-magpie-zookeeper zk-str)]
        (if (:success re)
          (assoc re :address zk-str)
          (if (= (:returncode re) 1)
            {:success false
             :returncode 2
             :info (str "no magpie path on " zk-str)}
            {:success false
             :returncode -1
             :info (:info re)}))))))

(defn get-task-info
  "get task info
   returncode 0 : success
             -1 : unknown error
              1 : task id NOT EXISTS"
  [cluster-id task-id]
  (log/info "func get-task-info:" cluster-id task-id)
  (let [zk-str (get (get @CONF "clusters") cluster-id)]
    (m-utils/map->string (utils/get-task-info zk-str task-id))))

(defn submit-task
  "submit task
  returncode 0 : success
             1 : task id exists
             2 : no active nimbus
            -1 : unknown error"
  [cluster-id task-id jar klass group type]
  (log/info "func get-task-status:" cluster-id task-id jar klass group type)
  (let [zk-str (get (get @CONF "clusters") cluster-id)
        re (utils/get-task-info zk-str task-id)]
    (if (:success re)
      (do (log/error "task id:" task-id "exists!")
          (m-utils/map->string {:success false
                                :info (str "task id: " task-id " exists!")
                                :returncode 1}))
      (let [re (utils/get-active-nimbus zk-str)]
        (if-not (:success re)
          (if (= (:returncode re) 1)
            (m-utils/map->string {:success false
                                  :info (:info re)
                                  :returncode 2})
            (m-utils/map->string {:success false
                                  :info (:info re)
                                  :returncode -1}))
          (let [re (utils/submit-task (get (:active-nimbus re) "ip")
                                      (get (:active-nimbus re) "port")
                                      task-id
                                      jar
                                      klass
                                      group
                                      type)]
            (if-not (:success re)
              (m-utils/map->string {:success false
                                    :info (:info re)
                                    :returncode -1})
              (if (= (:returncode re) 1)
                (m-utils/map->string {:success false
                                      :returncode 1
                                      :info (:info re)})
                (m-utils/map->string {:success true
                                      :returncode 0
                                      :info (:info re)})))))))))

(defn operate-task
    "magpie operate a task: kill pause active reload
     returncode 0 : task id exists and command submit success
                1 : task id not exists
                2 : no active nimbus
                3 : command is unsupported
               -1 : unknown error"
  [cluster-id task-id command]
  (log/info "func operate-task:" cluster-id task-id command)
  (let [zk-str (get (get @CONF "clusters") cluster-id)
        re (utils/get-task-info zk-str task-id)]
    (if-not (:success re)
      (m-utils/map->string re)
      (let [re (utils/get-active-nimbus zk-str)]
        (if-not (:success re)
          (if (= (:returncode re) 1)
            (m-utils/map->string {:success false
                                  :info (:info re)
                                  :returncode 2})
            (m-utils/map->string {:success false
                                  :info (:info re)
                                  :returncode -1}))
          (let [re (utils/operate-task (get (:active-nimbus re) "ip")
                                       (get (:active-nimbus re) "port")
                                       task-id
                                       command)]
            (m-utils/map->string (case (:returncode re)
                                   0 re
                                   1 {:success false
                                      :returncode 3
                                      :info (:info re)}
                                   2 {:success false
                                      :returncode 1
                                      :info (:info re)}
                                   -1 re))))))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (log/info "Hello, magpie proxy!")
  (let [conf-file "magpie-proxy.yaml"
        conf (m-utils/find-yaml conf-file true)
        check-clusters (fn []
                         (loop [clusters (get @CONF "clusters")]
                           (if-not (empty? clusters)
                             (let [cluster (first clusters)
                                   name (key cluster)
                                   zk-str (val cluster)]
                               (if (utils/check-magpie-zookeeper zk-str)
                                 (log/info "cluster" name "is OK!")
                                 (log/error "cluster" name "is NOT OK!"))
                               (recur (rest clusters))))))]
    (reset! CONF conf)
    (log/info CONF)
    (jsf-utils/start-jsf-server)
    (while true
      (check-clusters)
      (Thread/sleep 20000))))
