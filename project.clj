(defproject eav-pull "0.1.0"
  :url "https://github.com/souenzzo/eav-pull"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :profiles {:test {:dependencies [[com.datomic/datomic-free "0.9.5697"]
                                   [datascript/datascript "0.16.6"]
                                   [org.clojure/test.check "0.10.0-alpha3"]]}})
