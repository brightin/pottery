(ns pottery.core
  (:require [pottery.po :as po]
            [pottery.scan :as scan]))

(defmacro make-extractor
  "Returns an extraction function using the core.match pattern syntax.

  Example:
  ```
  (make-extractor
    ['tr s] s
    ['trn [s1 s2] _] [s1 s2])
  ```"
  [& args]
  `(scan/make-extractor ~@args))

(defn scan-codebase!
  "Recursively reads the code in dir, scans all strings and outputs a
  .pot file according to the gettext format with all the translatable
  strings."
  [{:keys [template-file] :as opts}]
  (->> (scan/scan-files opts)
       (po/gen-template)
       (spit template-file)))

(def read-po-file #'po/read-po-file)
