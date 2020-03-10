(defproject eav-pull "0.2.0"
  :url "https://github.com/souenzzo/eav-pull"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}
  :profiles {:test {:dependencies [[com.datomic/datomic-free "0.9.5697"]
                                   [datascript/datascript "0.18.10"]
                                   [org.clojure/clojure "1.10.1"]
                                   [org.clojure/test.check "0.10.0"]]}})
