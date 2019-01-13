(ns pottery.scan
  (:require [pottery.utils :refer [vectorize]]
            [clojure.core.match :refer [match]]
            [clojure.java.io :as io]
            [clojure.string :as str]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Files

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

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Extraction

(defmacro make-extractor [& match-patterns]
  `(fn extract-fn# [expr#]
     (match (vec expr#)
       ~@match-patterns
       :else nil)))

(defn extraction-warning [msg]
  {::warning msg})

(def default-extractor
  (make-extractor
   ['tr (s :guard string?) & _] s
   ['trn [(s1 :guard string?) (s2 :guard string?) & _] _] [s1 s2]
   [(:or 'tr 'trn) & _] (extraction-warning
                         "Could not extrapolate translation string for the form:")))

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
  [extract-fn expr]
  (when-let [val (and (seq? expr) (extract-fn expr))]
    (if-let [warning (::warning val)]
      (println warning expr)
      (with-comment expr val))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Scanning

(defn- find-tr-strings* [extract-fn expressions]
  (->> (map (partial extract extract-fn) (tree-seq coll? identity expressions))
       (remove nil?)
       distinct))

(def find-tr-strings #(update %2 ::expressions (partial find-tr-strings* %1)))

(defn scan-files
  "Walk the given directory and for every clj, cljc or cljs file
  extract the strings for which the extractor returns a value. "
  [{:keys [dir extract-fn]}]
  (println "Scanning files...")
  (->>
   (get-files (java.io.File. dir))
   (map read-file)
   (map (partial find-tr-strings extract-fn))
   (filter (comp seq ::expressions))
   (sort-by ::filename)))
