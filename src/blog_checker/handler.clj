(ns blog-checker.handler
  (:require [compojure.core :refer :all]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [ring.util.response :as resp]
            [blog-checker.utils :refer [get-info]]))

(def home-page
  (-> (resp/resource-response "index.html" {:root "public"})
      (resp/header "Cache-Control" "max-age=604800")
      (resp/header "Vary" "Accept-Encoding")
      (resp/header "Content-Type" "text/html; charset=utf-8")))

(defroutes app-routes
  (GET "/" [] home-page)
  (POST "/" [url] (get-info url))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (handler/site app-routes))
