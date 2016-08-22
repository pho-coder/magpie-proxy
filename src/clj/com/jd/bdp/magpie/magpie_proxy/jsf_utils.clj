(ns com.jd.bdp.magpie.magpie-proxy.jsf-utils
  (:require [clojure.tools.logging :as log])
  (:import [com.jd.jsf.gd.config ProviderConfig ServerConfig RegistryConfig]

           [com.jd.bdp.magpie.proxy ProxyService ProxyServiceImpl]))

(defn start-jsf-server
  "start jsf server"
  [jsf-alias]
  (let [server-config (ServerConfig.)
        registry-config (RegistryConfig.)
        jsf-protocol "jsf"
        provider-config (ProviderConfig.)
        interface-id (.getName ProxyService)
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
