(ns pottery.utils)

(defn vectorize
  "Will return any data in vector form. Vectors return themselves,
  sequences are transformed to vectors and anything else will be
  wrapped in a vector."
  [x]
  (cond
    (nil? x) []
    (vector? x) x
    (set? x) (vec x)
    (seq? x) (vec x)
    :else (vector x)))
