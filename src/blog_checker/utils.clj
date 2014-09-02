(ns blog-checker.utils
  (:require [clojure.string :refer [join]]
            [selmer.parser :refer [render-file]]
            [selmer.filters :refer [add-filter!]]
            [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as c]
            [blog-checker.sentiment :refer [score-sentence]]))

(defn get-link
  [link]
  (client/get (join "" ["http://hn.algolia.com/api/v1/search?query=" link "&tags=story"])))

(defn get-reddit-link
  [link]
  (client/get (join "" ["http://www.reddit.com/submit.json?url=" link])))

(defn get-body
  [link]
  (get (get (parse-string (get (get-link link) :body) true) :hits) 0))

(defn get-reddit-body
  [link]
  (get (get (parse-string (get (get-reddit-link link) :body) true) :data) :children))

(defn get-id
  [obj]
  (get (get obj :_tags) 2))

(defn get-url
  [obj]
  (let [id (get-id obj)]
    (if (empty? id) 0
      (join "" ["https://news.ycombinator.com/item?id=" (re-find #"\d+" id)]))))

(defn get-comments
  [obj]
  (client/get (join "" ["http://hn.algolia.com/api/v1/search?tags=comment," (get-id obj) "&hitsPerPage=150"])))

(defn get-body-comments
  [obj]
  (get (parse-string (get (get-comments obj) :body) true) :hits))

(defn get-sentiment
  [a-vec]
  (if (empty? a-vec) "none"
    (for [x (get-body-comments a-vec)]
      (score-sentence (get x :comment_text)))))

(defn avg-sent
  [a-vec]
  (if (empty? a-vec) 0
    (/ (reduce + a-vec) (count a-vec))))

(defn sent-classifier
  [n]
  (cond
   (< n 0) "Negative"
   (> n 0) "Positive"
   (= n 0) 0))

(defn parse-date [dt]
  (let [obj (f/parse (f/formatters :date-time) dt)]
    (f/unparse (f/formatters :rfc822) obj)))

(defn parse-reddit-date [t]
  (let [dt (.replaceAll (.replaceAll (str t) "E9" "000") "[.]" "")]
    (f/unparse (f/formatters :rfc822) (c/from-long (Long. dt)))))

(defn posted-first
  [hn reddit]
  (let [hn-date (f/parse (f/formatters :date-time) (get hn :created_at))
        reddit-date (for [x reddit]
                      (Long. (.replaceAll (.replaceAll (str (get-in x [:data :created_utc])) "E9" "000") "[.]" "")))
        reddit-min-date (c/from-long (apply min reddit-date))]
    (if (t/after? reddit-min-date hn-date)
      "Hacker News had this story first!"
      "reddit had this story first!")))

(defn get-stories []
  (let [stories (client/get "http://hn.algolia.com/api/v1/search_by_date?tags=story&numericFilters=points>20&hitsPerPage=3")]
    (render-file "public/index.html" {:stories (get (parse-string (get stories :body) true) :hits)})))

(add-filter! :reddit-date #(parse-reddit-date %))

(defn get-info [link]
  (let [res (get-body link)
        reddit-vec (get-reddit-body link)]
    (render-file "public/report.html" {:url link
                                       :posted_first (if (and (empty? (get res :created_at)) (= 0 (count reddit-vec)))
                                                       "This article has not yet been posted"
                                                       (if (empty? (get res :created_at))
                                                         "reddit had this story first!"
                                                         (if (= 0 (count reddit-vec))
                                                           "Hacker News had this story first!"
                                                           (posted-first res reddit-vec))))
                                       :created_at (if (empty? (get res :created_at)) 0
                                                     (parse-date (get res :created_at)))
                                       :hn res
                                       :sentiment (if (= "none" (get-sentiment res)) 0
                                                    (sent-classifier (avg-sent (get-sentiment res))))
                                       :hn-url (get-url res)
                                       :reddit reddit-vec
                                       :reddit-count (count reddit-vec)})))
