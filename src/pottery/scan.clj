(ns pottery.scan
  (:require [pottery.utils :refer [vectorize]]
            [clojure.core.match :refer [match]]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(defn- source-file? [file]
  (some #(re-find (re-pattern (str "." % "$")) (.getName file))
        ["clj" "cljc" "cljs"]))

(defn- get-files [dir]
  (filter source-file? (file-seq (io/file dir))))

(defn- read-file [file]
  {::filename (io/as-relative-path file)
   ::expressions
   (read-string {:read-cond :preserve}
                (format "(%s)" (str/replace (slurp file) #"::" ":")))})

(defn- warn-extract! [expression]
  (println "Could not extrapolate translation string for the form: " expression))

(defn- with-comment [expression text]
  (if-let [notes (:notes (meta expression))]
    {::value text ::notes (vectorize notes)}
    {::value text}))

(defn extract
  "Extracts strings from either
    `(tr i18n [\"Hello!\" arg])` or
    `(tr [\"Hello!\" arg])` or

  and the plural forms;

    `(trn i18n [\"One item\" \"%1 items\"] n)` or
    `(trn [\"One item\" \"%1 items\"] n)`"
  [expr]
  (when (seq? expr)
    (when-let [val (match (vec expr)
                     ['tr _ [s & _]] s
                     ['tr [s & _]] s
                     ['trn _ [s1 s2 & _] _] [s1 s2]
                     ['trn [s1 s2 & _] _] [s1 s2]
                     [(:or 'tr 'trn) & _] (warn-extract! expr)
                     :else nil)]
      (with-comment expr val))))

(defn- find-tr-strings* [expressions]
  (distinct (remove nil? (map extract (tree-seq coll? identity expressions)))))

(def find-tr-strings #(update % ::expressions find-tr-strings*))

(defn scan-files
  "Walk the given directory and for every clj, cljc or cljs file
  extract the strings for which the extractor returns a value. "
  [dir]
  (println "Scanning files...")
  (->>
   (get-files (java.io.File. dir))
   (map read-file)
   (map find-tr-strings)
   (filter (comp seq ::expressions))
   (sort-by ::filename)))
