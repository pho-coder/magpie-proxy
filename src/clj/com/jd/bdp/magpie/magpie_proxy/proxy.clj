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

(defn get-task-status
  "get task status
   returncode 0 : success
             -1 : unknown error
              1 : no zk address from conf
              2 : NO MAGPIE on the zookeeper
              3 : task id NOT EXISTS"
  [cluster-id task-id]
  (log/info "func get-task-status" cluster-id task-id)
  (let [re2 (let [re0 (get-magpie-zk-address cluster-id)]
              (if (:success re0)
                (let [zk-str (:address re0)
                      re1 (utils/get-task-info zk-str task-id)]
                  (if (= 1 (:returncode re1))
                    (m-utils/map->string {:success false
                                          :returncode 3
                                          :info (:info re1)})
                    (m-utils/map->string re1)))
                (m-utils/map->string re0)))]
    (log/info re2)
    re2))

(defn submit-task
  "submit task
  returncode 0 : success"
  [cluster-id task-id jar klass group type]
  (let [zk-str (get (get @CONF "clusters") cluster-id)]
    (let [re (utils/get-task-info zk-str task-id)]
      (if (:success re)
        (do (log/error "task id:" task-id "exists!")
            {:success false :info (str "task id:" task-id "exists!")})
        (let [active-nimbus (utils/get-active-nimbus zk-str)
              re (utils/submit-task (:ip active-nimbus)
                                    (:port active-nimbus)
                                    task-id
                                    jar
                                    klass
                                    group
                                    type)]
          (if-not (:success re)
            {:success false :info (:info re)}
            (let [re (utils/get-task-info zk-str task-id)]
              )))))))

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
          (recur (rest clusters))))))
  (jsf-utils/start-jsf-server)
  (while true
    (log/info "I am alive!")
    (Thread/sleep 10000)))
