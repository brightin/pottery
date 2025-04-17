(ns pottery.po
  (:require [clojure.string :as str]
            [pottery.scan :as scan]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Generating PO Template file

(def join (partial str/join "\n"))

(defn- ->blocks
  "Converts a scan result block (filename + expressions) and assoc's
  all expressions (msg-ids) into the result map, mergine the filenames
  and note's of the scan result as values."
  [result {::scan/keys [filename expressions]}]
  (reduce (fn [acc {::scan/keys [value notes]}]
            (update acc value #(merge-with concat % {:filenames [filename]
                                                     :notes notes})))
          result expressions))

(defn- format-notes [notes]
  (when (seq notes)
    (str (str/join "\n" (mapcat (fn [note] (map #(str "#. " %) (str/split-lines note))) notes)) "\n")))

(defn- create-sort-index
  "Creates a map from every scanned value to an occurence
  position. Used to retain scan order when generating the PO
  template."
  [scan-results]
  (zipmap (map ::scan/value (mapcat ::scan/expressions scan-results))
          (range)))

(defn- fmt-msg-id [s]
  (let [lines (str/split-lines s)
        q #(str \" (str/escape % {\" "\\\""}) \")]
    (if (next lines)
      (->> (concat [(q "")]
                   (map #(q (str % "\\n")) (butlast lines))
                   [(q (last lines))])
           (str/join "\n"))
      (q s))))

(defn gen-template
  "Takes in a list of scan results (filename + msg-ids), groups
  multiple appearances of the same msgid together and returns a ready
  to spit PO template file."
  [scan-results]
  (println "Generating POT file...")
  (join
   (for [[msg-id {:keys [filenames notes]}] (sort-by (comp (create-sort-index scan-results) key)
                                                     (reduce ->blocks {} scan-results))]
     (str
      (format-notes notes)
      (if (vector? msg-id)
        (format "#: %s\nmsgid %s\nmsgid_plural %s\nmsgstr[0] \"\"\nmsgstr[1] \"\"\n"
                (str/join " " filenames) (fmt-msg-id (first msg-id)) (fmt-msg-id (second msg-id)))
        (format "#: %s\nmsgid %s\nmsgstr \"\"\n" (str/join " " filenames) (fmt-msg-id msg-id)))))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Reading PO files

(defn- quoted-string? [line]
  (and (str/starts-with? line "\"") (str/ends-with? line "\"")))

(defn- read-quoted-string [line]
  (str/replace
   (subs line 1 (dec (count line)))
   #"\\n" "\n"))

(defn- tag-parser
  "Creates a fn that takes remaining lines and returns a tuple with
  the key, value and and the remaining unparsed lines."
  [key]
  (fn [[line & rest]]
    (let [[multiline-values rest] (split-with quoted-string? rest)
          values (map read-quoted-string (concat [line] multiline-values))]
      [key (str/join values) rest])))

(defn default-parser [lines]
  [nil nil (drop 1 lines)])

(def PO_TAGS
  {"msgid"        (tag-parser ::msgid)
   "msgid_plural" (tag-parser ::msgid-plural)
   "msgstr"       (tag-parser ::msgstr)
   "msgstr[0]"    (tag-parser ::msgstr)
   "msgstr[1]"    (tag-parser ::msgstr-plural)})

(defn parse-block
  "Parses a block in a PO file, and returns an object with the msgid,
  msgstr and possible plural versions of the strings."
  [block-str]
  (loop [lines (map str/trim (str/split-lines block-str))
         result {}]
    (if (empty? lines)
      result
      (let [[tag remainder] (str/split (first lines) #"\s" 2)
            lines (concat [remainder] (rest lines))
            parser (get PO_TAGS tag default-parser)
            [k v rest] (parser lines)]
        (recur rest (if v (assoc result k v) result))))))

(defn- ->kv [block]
  (if (contains? block ::msgid-plural)
    [[(::msgid block) (::msgid-plural block)]
     [(::msgstr block) (::msgstr-plural block)]]
    [(::msgid block) (::msgstr block)]))

(defn read-po-str [s]
  (->> (str/split s #"\n\n")
       (drop 1) ;; Header meta data
       (map parse-block)
       (map ->kv)
       (into {})))

(defn read-po-file [file]
  (read-po-str (slurp file)))
