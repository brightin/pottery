(ns simple.example
  (:require [pottery.core :as pottery]
            [clojure.java.io :as io]))

(def DICT
  {:nl (pottery/read-po-file (io/file "examples/simple/gettext/nl.po"))
   :fr (pottery/read-po-file (io/file "examples/simple/gettext/fr.po"))})

(defn tr [lang s & args]
  (let [string (or (get-in DICT [lang s]) s)]
    (apply format string args)))

(defn trn [lang strings count & args]
  (let [[singular plural] (or (get-in DICT [lang strings]) strings)]
    (if (= 1 count)
      (apply format singular (conj args count))
      (apply format plural (conj args count)))))

;; Useful to have this in a dev user namespace
(defn gettext-do-scan! []
  (pottery/scan-codebase!
   {:dir "examples/simple"
    :template-file (io/file "examples/simple/gettext/template.pot")
    :extract-fn (pottery/make-extractor ;; We use the lang as first argument to tr and trn.
                 ['tr _ (s :guard string?) & _] s
                 ['trn _ [(s1 :guard string?) (s2 :guard string?)] & _] [s1 s2]
                 [(:or 'tr 'trn) & _] (pottery.scan/extraction-warning
                                       "Could not extract strings for the form:"))}))

;; After calling the scan functions and translating the PO files,
;; re-eval DICT and this should be the results:
(tr :en "Greetings")                  ;; => "Greetings"
(tr :nl "Greetings")                  ;; => "Groeten"
(tr :fr "Please confirm your email")  ;; => "Veillez confirmer votre email"
(tr :nl "Welcome, %s!" "John")        ;; => "Welkom, John!

(trn :nl ["product" "%s products"] 3) ;; => "3 producten"
(trn :fr ["product" "%s products"] 1) ;; => "produit"
