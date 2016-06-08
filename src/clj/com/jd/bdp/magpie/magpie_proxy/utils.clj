(ns com.jd.bdp.magpie.magpie-proxy.utils
  (:require [clojure.tools.logging :as log]
            [yaml.core :as yaml])
  (:import [com.jd.jsf.gd.config ProviderConfig ServerConfig RegistryConfig]

           [com.jd.bdp.magpie.proxy ProxyService ProxyServiceImpl]))

(defn start-jsf-server
  "start jsf server"
  []
  (let [server-config (ServerConfig.)
        registry-config (RegistryConfig.)
        jsf-protocol "jsf"
        provider-config (ProviderConfig.)
        interface-id (.getName ProxyService)
        jsf-alias "magpie-proxy"
        ref (ProxyServiceImpl.)]
    (.setProtocol server-config jsf-protocol)
    (.setIndex registry-config "i.jsf.jd.com")
    
    (.setInterfaceId provider-config interface-id)
    (.setAlias provider-config jsf-alias)
    (.setRef provider-config ref)
    (.setServer provider-config server-config)
    (.setRegistry provider-config registry-config)
    (.setRegister provider-config true)
    (.export provider-config)
    (log/info "jsf server started!")))

(defn mock-echo-str
  "test fn"
  [a-str]
  (str "Hi " a-str))

(defn read-conf
  "read yaml conf"
  [conf-file]
  (let [re (yaml/from-file conf-file)]
    (if (nil? re)
      (do (log/error conf-file "conf is nil!")
          (System/exit -1))
      re)))
