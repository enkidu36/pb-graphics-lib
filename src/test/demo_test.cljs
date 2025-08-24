(ns demo-test
  (:require [cljs.test :refer [deftest is]]
            [pbranes.webgl.dg-controls :as dg]))


(deftest folder?-test
  (is (dg/folder? {})))

(deftest a-failing-test
  (is (= 1 2)))
