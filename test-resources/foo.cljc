(ns foo
  (:require [clojure.set :as set]))

(defn square [x] x)
(defn tr [& _args])

(defn render-thing [{:keys [some-arg] ::keys [some-other-arg] ::set/keys [yet-another-arg]}]
  [:div
   (tr "Both Clojure and ClojureScript")
   #?(:clj (tr "Clojure text %1 %2 %3" some-arg some-other-arg yet-another-arg)
      :cljs (tr "ClojureScript text %1 %2 %3" some-arg some-other-arg yet-another-arg))])
