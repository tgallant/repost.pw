(ns blog-checker.utils
  (:require [clojure.string :refer [join]]
            [selmer.parser :refer [render-file]]
            [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clj-time.format :as f]
            [blog-checker.sentiment :refer [score-sentence]]))

(defn get-link [link]
  (client/get (join "" ["http://hn.algolia.com/api/v1/search?query=" link "&tags=story"])))

(defn get-reddit-link [link]
  (client/get (join "" ["http://www.reddit.com/submit.json?url=" link])))

(defn get-body [link]
  (get (get (parse-string (get (get-link link) :body) true) :hits) 0))

(defn get-reddit-body [link]
  (get (get (parse-string (get (get-reddit-link link) :body) true) :data) :children))

(defn get-id [obj]
  (get (get obj :_tags) 2))

(defn get-url [obj]
  (let [id (get-id obj)]
    (join "" ["https://news.ycombinator.com/item?id=" (re-find #"\d+" id)])))

(defn get-comments [obj]
  (client/get (join "" ["http://hn.algolia.com/api/v1/search?tags=comment," (get-id obj) "&hitsPerPage=150"])))

(defn get-body-comments [obj]
  (get (parse-string (get (get-comments obj) :body) true) :hits))

(defn get-sentiment [a-vec]
  (for [x (get-body-comments a-vec)]
    (score-sentence (get x :comment_text))))

(defn avg-sent [a-vec]
  (/ (reduce + a-vec) (count a-vec)))

(defn sent-classifier [n]
  (cond
   (< n 0) "Negative"
   (> n 0) "Positive"))

(defn parse-date [dt]
  (let [obj (f/parse (f/formatters :date-time) dt)]
    (f/unparse (f/formatters :rfc822) obj)))

(defn get-info [link]
  (let [res (get-body link)]
    (render-file "public/report.html" {:url link
                                       :created_at (parse-date (get res :created_at))
                                       :title (get res :title)
                                       :author (get res :author)
                                       :points (get res :points)
                                       :num_comments (get res :num_comments)
                                       :sentiment (sent-classifier (avg-sent (get-sentiment res)))
                                       :hn-url (get-url res)
                                       :reddit (get-reddit-body link)
                                       })))
