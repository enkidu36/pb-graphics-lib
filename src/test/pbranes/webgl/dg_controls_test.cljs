(ns  pbranes.webgl.dg-controls-test
  (:require [cljs.test :refer [deftest is]]
            [pbranes.webgl.dg-controls :as dg]))

(deftest normalize-color
  (is (= (dg/normalize-color [25 100 255])
         [0.09803921568627451 0.39215686274509803 1]))) 

(deftest de-normalize-color
  (is (= (dg/de-normalize-color [0.09803921568627451
                                 0.39215686274509803
                                 1])
         [25 100 255]) ))

(deftest folder?
  (is (dg/folder? {})))

(deftest action?
  (is (dg/action? (fn [] nil)))
  (is (not (dg/action? 1))))

(deftest color?
  (is (dg/color? "#00ff00"))
  (is (dg/color? [1 2 3])))

(dg/de-normalize-color [0.09803921568627451 0.39215686274509803 1])

