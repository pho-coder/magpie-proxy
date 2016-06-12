(ns com.jd.bdp.magpie.magpie-proxy.utils
  (:require [clojure.tools.logging :as log]

            [clj-zookeeper.zookeeper :as zk]
            [com.jd.bdp.magpie.util.utils :as m-utils])

  (:import [com.jd.magpie.generated Nimbus]))

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
          {:success false}
          (let [nodes (to-array nimbuses)]
            (java.util.Arrays/sort nodes)
            (let [active-nimbus (m-utils/bytes->map (zk/get-data (str "/magpie/nimbus/" (first nodes))))]
              (log/info "active nimbus:" active-nimbus)
              {:success true :active-nimbus active-nimbus}))))
      (catch Exception e
        (log/error e)
        {:success false}))))

(defn get-one-magpie-client
  "get "
  [])
