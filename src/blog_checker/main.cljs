(ns blog-checker.main)

(defn ^:export redirect [url]
  (let [field (.getElementById js/document "inputForm")
        button (.getElementById js/document "submit")]
    (set! (.-value field) url)
    (.click button)))
