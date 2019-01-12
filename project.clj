(defproject brightin/pottery "0.0.1"
  :description "A clojure library to interact with gettext and PO/POT files"
  :url "https://www.github.com/brightin/pottery"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0" :scope "provided"]
                 [org.clojure/core.match "0.3.0-alpha5"]]
  :repl-options {:init-ns pottery.core})
