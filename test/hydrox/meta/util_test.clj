(ns hydrox.meta.util-test
  (:use midje.sweet)
  (:require [hydrox.meta.util :refer :all]
            [clojure.java.io :as io]
            [rewrite-clj.zip :as z]
            [rewrite-clj.node :as node]))

^{:refer hydrox.meta.util/append-node :added "0.1"}
(fact "Adds node as well as whitespace and newline on right"

  (-> (z/of-string "(+)")
      (z/down)
      (append-node 2)
      (append-node 1)
      (z/->root-string))
  => "(+\n  1\n  2)")

^{:refer hydrox.meta.util/has-quotes? :added "0.1"}
(fact "checks if a string has quotes"

  (has-quotes? "\"hello\"")
  => true)

^{:refer hydrox.meta.util/strip-quotes :added "0.1"}
(fact "gets rid of quotes in a string"

  (strip-quotes "\"hello\"")
  => "hello")

^{:refer hydrox.meta.util/escape-newlines :added "0.1"}
(fact "makes sure that newlines are printable"

  (escape-newlines "\\n")
  => "\\n")

^{:refer hydrox.meta.util/escape-escapes :added "0.1"}
(fact "makes sure that newlines are printable"

  (escape-escapes "\\n")
  => "\\\\n")

^{:refer hydrox.meta.util/escape-quotes :added "0.1"}
(fact "makes sure that quotes are printable in string form"

  (escape-quotes "\"hello\"")
  => "\\\"hello\\\"")

^{:refer hydrox.meta.util/strip-quotes-array :added "0.1"}
(fact "utility that strips quotes when not the result of a fact"
  (strip-quotes-array ["\"hello\""])
  => ["hello"]
  
  (strip-quotes-array ["(str \"hello\")" " " "=>" " " "\"hello\""])
  => ["(str \"hello\")" " " "=>" " " "\"hello\""])


^{:refer hydrox.meta.util/nodes->docstring :added "0.1"}
(fact "converts nodes to a docstring compatible"
  (->> (z/of-string "\"hello\"\n  (+ 1 2)\n => 3 ")
       (iterate z/right*)
       (take-while identity)
       (map z/node)
       (nodes->docstring)
       (node/string))
  => "\"hello\n   (+ 1 2)\n  => 3 \""

  (->> (z/of-string (str [\e \d]))
       (iterate z/right*)
       (take-while identity)
       (map z/node)
       (nodes->docstring)
       (str)
       (read-string))
  => "[\\e \\d]")

^{:refer hydrox.meta.util/import-location :added "0.1"}
(fact "imports the meta information and docstring")

^{:refer hydrox.meta.util/write-to-file :added "0.1"}
(fact "exports the zipper contents to file")

^{:refer hydrox.meta.util/all-files :added "0.1"}
(fact "finds all files in the project given a context"

  (->> (all-files {:root (.getCanonicalPath (io/file "example"))} :root "md")
       (map #(.getName %)))
  => ["README.md"])
