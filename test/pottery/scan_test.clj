(ns pottery.scan-test
  (:require [pottery.scan :as sut]
            [clojure.test :refer [deftest testing is are]]))

(deftest extract-test
  (testing "Default extractor"
    (are [expr result] (= result (::sut/value (sut/extract sut/default-extractor expr)))
      '(tr "Hello") "Hello"
      '(tr "Hello %s" arg1) "Hello %s"
      '(trn ["One" "Many"] 1) ["One" "Many"]
      '(trn ["One %s" "Many %s" arg1] 3) ["One %s" "Many %s"]))

  (testing "Advanced extractor"

    (let [extractor (sut/make-extractor
                     ['tr _ [s & _]] s
                     ['tr [s & _]] s
                     ['trn _ [s1 s2 & _] _] [s1 s2]
                     ['trn [s1 s2 & _] _] [s1 s2])
          extract (partial sut/extract extractor)]

      (are [expr result] (= result (::sut/value (extract expr)))
        '(tr ["Hello"])               "Hello"
        '(tr ["Hello %1!" arg1 arg2]) "Hello %1!"
        '(trn ["Item" "Items"] 2)     ["Item" "Items"]
        '(inc 6)                      nil
        "foo"                         nil)

      (is (vector? (::sut/value (extract '(trn i18n ["one" "many"] 2)))))

      (is (= ["Some note"]
             (::sut/notes
              (extract (read-string "^{:notes \"Some note\"} (tr i18n [\"This is text\"])")))))

      (is (= ["note 1" "note 2"]
             (::sut/notes
              (extract (read-string "^{:notes [\"note 1\" \"note 2\"]} (tr i18n [\"This is text\"])")))))

      (is (= "Text" (::sut/value (extract (read-string "(tr (get-i18n) [\"Text\"])"))))))))

(deftest find-tr-strings-test
  (is (= {::sut/filename "foo.cljs"
          ::sut/expressions [{::sut/value "Some text %1"}]}
         (sut/find-tr-strings
          sut/default-extractor
          {::sut/filename "foo.cljs"
           ::sut/expressions '((ns foo (:require [x :as b]))
                               (defn square [x] x)
                               (defn render-thing [{:keys [some-arg]}]
                                 [:div (tr "Some text %1" some-arg)]))}))))
