(ns foo)

(defn square [x] x)
(defn tr [& _args])

(defn render-thing [{:keys [some-arg] ::keys [some-other-arg] ::foo/keys [yet-another-arg]}]
  [:div #?(:clj (tr "Clojure text %1 %2 %3" some-arg some-other-arg yet-another-arg)
           :cljs (tr "ClojureScript text %1 %2 %3" some-arg some-other-arg yet-another-arg))])
