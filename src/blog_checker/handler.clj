(ns blog-checker.handler
  (:use [org.httpkit.server :only [run-server]])
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [blog-checker.utils :refer [get-info get-stories]]))

(defroutes app-routes
  (GET "/" {params :params}
    (if (nil? (:url params))
      (get-stories)
      (get-info (:url params))))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))

(defn -main []
  (run-server app {:port 3001}))
