(ns blog-checker.sentiment
  (:require [clojure.java.io :as io]))

(defn tokenize
  "Break a string into words (letters, digits and minus)"
  [a-str]
  (vec (re-seq #"[\w\d-]+" a-str)))

(defn load-scores
  "Read score file and return hash-map of lowercase word: score."
  [a-path]
  (with-open [r (io/reader (io/as-file a-path))]
    (->> (line-seq r)
        (mapv tokenize)
        (filterv (complement empty?))
        (mapv (juxt (comp #(.toLowerCase %) first)
                    (comp #(Float/parseFloat %) second)))
        (into (hash-map)))))

(def scores (load-scores "inputs/scores.txt"))

(defn score-word
  "Lookup word in current scores."
  [word]
  (get @(var scores) (.toLowerCase word) 0))

(defn score-sentence
  "Tokenize string and score based on tokens count."
  [a-str]
  (let [words (tokenize a-str)]
    (if (empty? words) 0
      (/ (reduce + (mapv score-word words)) (count words)))))
