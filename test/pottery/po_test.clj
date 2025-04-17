(ns pottery.po-test
  (:require [clojure.java.io :as io]
            [clojure.test :refer [deftest is testing]]
            [pottery.po :as sut]
            [pottery.scan :as scan]))

(deftest gen-template-test
  (testing "Single and plural expressions"
    (is (= (str "#: a.cljs\n"
                "msgid \"Hello %s!\"\n"
                "msgstr \"\"\n"
                "\n"
                "#: b.cljs\n"
                "msgid \"Item\"\n"
                "msgid_plural \"Items\"\n"
                "msgstr[0] \"\"\n"
                "msgstr[1] \"\"\n")
           (sut/gen-template [{::scan/filename "a.cljs"
                               ::scan/expressions [{::scan/value "Hello %s!"}]}
                              {::scan/filename "b.cljs"
                               ::scan/expressions [{::scan/value ["Item" "Items"]}]}]))))

  (testing "Grouping multiple appearances of same string"
    (is (= (str "#: foo.cljs bar.cljs\n"
                "msgid \"Hello %s!\"\n"
                "msgstr \"\"\n")
           (sut/gen-template [{::scan/filename "foo.cljs"
                               ::scan/expressions [{::scan/value "Hello %s!"}]}
                              {::scan/filename "bar.cljs"
                               ::scan/expressions [{::scan/value "Hello %s!"}]}]))))

  (testing "Escape double-quotes in msgids"
    (is (= (str "#: foo.cljs\n"
                "msgid \"Hello \\\"%s\\\"!\"\n"
                "msgstr \"\"\n")
           (sut/gen-template [{::scan/filename "foo.cljs"
                               ::scan/expressions [{::scan/value "Hello \"%s\"!"}]}]))))

  (testing "Outputting translator notes"
    (is (= (str "#. note 1\n"
                "#. note 2\n"
                "#: file.cljs\n"
                "msgid \"id\"\n"
                "msgstr \"\"\n")
           (sut/gen-template [{::scan/filename "file.cljs"
                               ::scan/expressions [{::scan/value "id"
                                                    ::scan/notes ["note 1" "note 2"]}]}]))))

  (testing "Outputting translator of multiple appearances"
    (is (= (str "#. note 1\n"
                "#. note 2\n"
                "#. note 3\n"
                "#. note 4\n"
                "#: file.cljs file2.cljs\n"
                "msgid \"id\"\n"
                "msgstr \"\"\n")
           (sut/gen-template [{::scan/filename "file.cljs"
                               ::scan/expressions [{::scan/value "id"
                                                    ::scan/notes ["note 1" "note 2"]}]}
                              {::scan/filename "file2.cljs"
                               ::scan/expressions [{::scan/value "id"
                                                    ::scan/notes ["note 3" "note 4"]}]}])))))

(defn read-fixture [name]
  (slurp (io/file "test/pottery/_fixtures/" name)))

(def single-block (read-fixture "single_block.po"))
(def plural-block (read-fixture "plural_block.po"))
(def multi-line-single-block (read-fixture "multiline_single_block.po"))
(def multi-line-plural-block (read-fixture "multiline_plural_block.po"))

(deftest parse-block-test
  (is (= {::sut/msgid "Hello!"
          ::sut/msgstr "Hoi!"}
         (sut/parse-block single-block)))

  (is (= {::sut/msgid        "one mouse"
          ::sut/msgid-plural "%1 mice"
          ::sut/msgstr "een muis"
          ::sut/msgstr-plural "muizen"}
         (sut/parse-block plural-block)))

  (is (= {::sut/msgid "First-line\nSecond-line"
          ::sut/msgstr "Eerste regel\nTweede regel"}
         (sut/parse-block multi-line-single-block)))

  (is (= {::sut/msgid "Some\nLong Message id",
          ::sut/msgid-plural "Some\n plural id",
          ::sut/msgstr "First\nSecond line [s]",
          ::sut/msgstr-plural "First\nSecond Line [p]"}
         (sut/parse-block multi-line-plural-block))))
