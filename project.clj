(defproject blog-checker "0.1.0-SNAPSHOT"
  :description "social sharing/sentiment tool for hacker news and reddit"
  :url "http://stats.timgallant.us"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-2322"]
                 [prismatic/dommy "1.0.0"]
                 [compojure "1.1.8"]
                 [selmer "0.6.9"]
                 [clj-http "1.0.0"]
                 [clj-time "0.8.0"]
                 [cheshire "5.3.1"]]
  
  :uberjar {:aot [blog-checker.handler]}

  :plugins [[lein-ring "0.8.11"]
            [lein-cljsbuild "1.0.4-SNAPSHOT"]]

  :source-paths ["src"]

  :cljsbuild {
              :builds [{:id "blog-checker"
                        :source-paths ["src"]
                        :compiler {
                                   :output-to "resources/public/js/main.js"
                                   :optimizations :advanced
                                   :pretty-print true}}]}

  :ring {:handler blog-checker.handler/app :port 3001}
  :profiles
  {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                        [ring-mock "0.1.5"]]}})
