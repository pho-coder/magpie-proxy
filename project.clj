(defproject magpie-proxy "0.3.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories [["jd-libs-releases" {:url "http://artifactory.360buy-develop.com/libs-releases"
                                      :update :nerver}]
                 ["jd-libs-snapshots" {:url "http://artifactory.360buy-develop.com/libs-snapshots-local"
                                       :update :nerver}]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.21"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.jd/jsf "1.6.0"]
                 [com.jd.magpie/magpie-client "1.1.3-SNAPSHOT"]
                 [com.jd.bdp.magpie/magpie-utils "0.1.3-SNAPSHOT"]
                 [clj-zookeeper "0.2.0-SNAPSHOT"]]
  :main ^:skip-aot com.jd.bdp.magpie.magpie-proxy.proxy
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :target-path "target/%s"
  :plugins [[cider/cider-nrepl "0.12.0"]]
  :profiles {:uberjar {:aot :all}})
