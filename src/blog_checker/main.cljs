(ns blog-checker.main
  (:require 
   [dommy.core :refer-macros [sel sel1]]))

(defn ^:export submit-handler []
  (let [url (.-value (sel1 :#inputForm))]
    (set! (.-href js/window.location) (str "/?url=" url))))

(defn set-url-value! [url]
  (->
    (sel1 :#inputForm)
    (dommy.core/set-value! url)))

(defn ^:export redirect [url]
  (let [button (.getElementById js/document "submit")]
    (set-url-value! url)
    (.click button)))
