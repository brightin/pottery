(ns pottery.core
  (:require [pottery.po :as po]
            [pottery.scan :as scan]
            [clojure.java.io :as io]))

(defn- default-scan-options []
  {:dir "src"
   :extract-fn #'scan/default-extractor
   :template-file (io/file "resources/gettext/template.pot")})

(defmacro make-extractor
  "Returns an extraction function using the core.match pattern syntax.

  Example:

    (make-extractor
      ['tr s] s
      ['trn [s1 s2] _] [s1 s2]) "
  [& args]
  `(scan/make-extractor ~@args))

(defn scan-codebase!
  "Recursively reads the code in dir, scans all strings and outputs a
  .pot file according to the gettext format with all the translatable
  strings.

  Opts is a map which accepts:
  :dir - The source dir to be scanned.
  :extract-fn - The extraction function that gets called with every
                expression in the codebase
  :template-file - The POT file where the results are to be written.

  All of these options have defaults."
  ([] (scan-codebase! {}))
  ([opts]
   (let [{:keys [template-file] :as opts} (merge (default-scan-options) opts)]
     (io/make-parents template-file)
     (->> (scan/scan-files opts)
          (po/gen-template)
          (spit template-file)))))

(def read-po-file #'po/read-po-file)
