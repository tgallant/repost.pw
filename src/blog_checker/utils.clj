(ns blog-checker.utils
  (:require [clojure.string :refer [join]]
            [selmer.parser :refer [render-file]]
            [selmer.filters :refer [add-filter!]]
            [clj-http.client :as client]
            [cheshire.core :refer :all]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.coerce :as c]))

(defn get-link
  [link]
  (client/get
    (join "" ["http://hn.algolia.com/api/v1/search?query=" link "&tags=story"])))

(defn get-reddit-link
  [link]
  (client/get (join "" ["http://www.reddit.com/submit.json?url=" link]) {:throw-exceptions false}))

(defn get-body
  [link]
  (get (parse-string (get (get-link link) :body) true) :hits))

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

(defn parse-date [dt]
  (let [obj (f/parse (f/formatters :date-time) dt)]
    (f/unparse (f/formatters :rfc822) obj)))

(defn parse-reddit-date [t]
  (let [dt (.replaceAll (.replaceAll (str t) "E9" "000") "[.]" "")]
    (f/unparse (f/formatters :rfc822) (c/from-long (Long. dt)))))

(defn posted-first
  [hn reddit]
  (let [hn-date (for [x hn]
                  (f/parse (get x :created_at)))
        reddit-date (for [x reddit]
                      (Long. (.replaceAll (.replaceAll (str (get-in x [:data :created_utc])) "E9" "000") "[.]" "")))
        reddit-min-date (c/from-long (apply min reddit-date))
        hn-min-date (apply min hn-date)]
    (if (t/after? reddit-min-date hn-min-date)
      "Hacker News had this story first!"
      "reddit had this story first!")))

(defn get-stories []
  (let [stories (client/get "http://hn.algolia.com/api/v1/search_by_date?tags=story&numericFilters=points>20&hitsPerPage=3")]
    (render-file "templates/index.html" {:stories (get (parse-string (get stories :body) true) :hits)})))

(add-filter! :reddit-date #(parse-reddit-date %))
(add-filter! :hn-url #(get-url %))

(defn posted-handler [hn-vec reddit-vec]
  (if (and (= 0 (count hn-vec)) (= 0 (count reddit-vec)))
    "This article has not yet been posted"
    (if (= 0 (count hn-vec))
      "reddit had this story first!"
      (if (= 0 (count reddit-vec))
        "Hacker News had this story first!"
        (posted-first hn-vec reddit-vec)))))

(defn get-info [link]
  (let [hn-vec (get-body link)
        reddit-vec (get-reddit-body link)]
    (render-file "templates/report.html"
      {:url link
       :posted_first (posted-handler hn-vec reddit-vec)
       :hn hn-vec
       :reddit reddit-vec
       :hn-count (count hn-vec)
       :reddit-count (count reddit-vec)})))
