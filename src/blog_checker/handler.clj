(ns blog-checker.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [blog-checker.utils :refer [get-info]]))

(defroutes app-routes
  (GET "/" [] (resp/resource-response "index.html" {:root "public"}))
  (POST "/" [url] (get-info url))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
