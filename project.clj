(defproject magpie-proxy "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :repositories [["jd-libs-releases" "http://artifactory.360buy-develop.com/libs-releases"]]
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.slf4j/slf4j-log4j12 "1.7.21"]
                 [org.clojure/tools.logging "0.3.1"]
                 [com.jd/jsf "1.6.0"]]
  :main ^:skip-aot com.jd.bdp.magpie.magpie-proxy.core
  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})