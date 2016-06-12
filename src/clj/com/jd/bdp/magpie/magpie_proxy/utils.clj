(ns com.jd.bdp.magpie.magpie-proxy.utils
  (:require [clojure.tools.logging :as log]
            [clj-zookeeper.zookeeper :as zk])

  (:import [com.jd.magpie.client MagpieClient]))

(defn mock-echo-str
  "test fn"
  [a-str]
  (str "Hi " a-str))

(defn check-magpie-zookeeper
  "check magpie zookeeper adress"
  [zk-str]
  (try
    (zk/new-client zk-str)
    (zk/check-exists? "/magpie/nimbus")
    (zk/close)
    (catch Exception e
      (log/error e)
      (zk/close)
      false)))

(defn get-active-nimbus
  "get magpie active nimbus from zookeeper"
  [zk-str])

(defn get-one-magpie-client
  "get "
  [])
