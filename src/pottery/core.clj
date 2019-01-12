(ns pottery.core
  (:require [pottery.po :as po]
            [pottery.scan :as scan]))

(defn scan-codebase!
  "Recursively reads the code in dir, scans all strings and outputs a
  .pot file according to the gettext format with all the translatable
  strings."
  [{:keys [dir template-file] :as opts}]
  (->> (scan/scan-files dir)
       (po/gen-template)
       (spit template-file)))

(def read-po-file #'po/read-po-file)
