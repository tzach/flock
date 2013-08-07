(ns flock.math
  (:require [mondrian.math :as math]
            [clojure.set :as set]
            [monet.geometry :as mg]
            ))

(defn mean [col]
  "return mean is col have more than one element, else 0"
  (let [l (count col)]
    (if (pos? l)
      (/ (reduce + col) l)
      0)))

(defn mean-of-angles [col]
  (int
   (math/degrees
    (let [size (count col)
          sins  (/ (reduce + (map #(Math/sin (math/radians %)) col)) size)
          coss  (/ (reduce + (map #(Math/cos (math/radians %)) col)) size)
          ] 
      (Math/atan2 sins coss)
      )
    )))

(defn relative-mean [ x y ratio]
  (+ (* x ratio) (* y (- 1 ratio)))
  )

(defn relative-mean-of-angles [a b ratio]
  "ration between 0 and 1"
  (let [ap (int  (* 10 ratio))
        bp (- 10 ap)]
    (mean-of-angles (concat  (repeat ap a) (repeat bp b)))
    ))


(defn mean-poistion [flock]
  { :x (mean (map :x flock))
    :y (mean (map :y flock))}
  )

(defn direction-to [from to]
  (math/degrees
   (Math/atan2 
    (- (:y to) (:y from))
    (- (:x to) (:x from))
    )))

(defn local-flock [bird birds r]
  "return list of birds in the local range, not including self"
  (disj (set (filter #(mg/in-radius? bird % r) birds)) bird)
  )
