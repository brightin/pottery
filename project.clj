(defproject brightin/pottery "0.0.3"
  :description "A clojure library to interact with gettext and PO/POT files"
  :url "https://www.github.com/brightin/pottery"
  :license {:name "Hippocratic License"
            :url "https://firstdonoharm.dev/"}
  :dependencies [[org.clojure/clojure "1.10.0" :scope "provided"]
                 [org.clojure/core.match "1.0.0"]]
  :repl-options {:init-ns pottery.core})
