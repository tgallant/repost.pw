(defproject blog-checker "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.8"]
                 [selmer "0.6.9"]
                 [clj-http "1.0.0"]
                 [clj-time "0.8.0"]
                 [cheshire "5.3.1"]]

  :plugins [[lein-ring "0.8.11"]]

  :ring {:handler blog-checker.handler/app}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
