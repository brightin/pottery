(ns foo)

(defn square [x] x)
(defn tr [& _args])

(defn render-thing [{:keys [some-arg] ::keys [some-other-arg] ::foo/keys [yet-another-arg]}]
  [:div (tr "Some text %1 %2 %3" some-arg some-other-arg yet-another-arg)])

