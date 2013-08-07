(ns flock.flock
  (:require [mondrian.anim :as anim]
            [mondrian.canvas :as canvas]
            [mondrian.color :as color]
            [mondrian.math :as math]
            [mondrian.plot :as plot]
            [mondrian.ui :as ui]
            [monet.canvas :as m]
            [clojure.set :as set]
            [flock.math :as fm]
            )
  (:use-macros [mondrian.macros :only [defmondrian]]))

(defn merge-control-values
  "Merge the current values of the controls into state."
  [{:keys [drawing] :as state}]
  (merge state (ui/update-controls drawing)))

(defn move-bird [bird h w delta-pixels]
  (let [
        dir (:direction bird)
        rad (math/radians dir)
        x (+ (:x bird) (math/circle-x delta-pixels rad))
        y (+ (:y bird) (math/circle-y delta-pixels rad))
        ]
    (assoc bird :x x :y y :direction dir)
    ))

(defn mean-alignment [flock]
  (fm/mean-of-angles (map :direction flock))
  )

(def *rotation-speed* 
  "Speed of alignment from 0 to 1, where 0 is ingnoring the flock, 1 is align"
  {:alignment 0.1 
   :seperation 0.2
   :cohesion 0.1
   :off-the-wall 0.3
   }
  )

(defn update-direction-toward-mean-alignment [bird birds alignment-r rate]
  (let [flock (fm/local-flock bird birds alignment-r)]
    (if (empty? flock)
      bird
      (assoc bird :direction (fm/relative-mean-of-angles (mean-alignment flock) (:direction bird) rate ))
      ))
  )


(defn update-direction-toward-mean-position [bird birds r rate]
  (let [flock (fm/local-flock bird birds r)]
    (if (empty? flock)
      bird
      (assoc bird :direction (fm/relative-mean-of-angles (fm/direction-to bird (fm/mean-poistion flock)) (:direction bird) rate))
      ))
  )

(defn update-direction-away-mean-position [bird birds r rate]
  (let [flock (fm/local-flock bird birds r)]
    (if (empty? flock)
      bird
      (assoc bird :direction (fm/relative-mean-of-angles (+ 180 (fm/direction-to bird (fm/mean-poistion flock))) (:direction bird) rate))
      ))
  )

(defn update-direction-off-the-walls [bird w h r rate]
  (let [to (cond 
            (< (:x bird) r) 0
            (> (:x bird) (- w r)) 180
            (< (:y bird) r) 90
            (> (:y bird) (- w r)) 270
            :else (:direction bird)
            )]
    (assoc bird :direction (fm/relative-mean-of-angles to (:direction bird) rate))
    ))

(defn move-flock
  [{:keys [delta-t-ms speed-pps w h birds seperation-r alignment-r cohesion-r] :as state}]
  ;;  (.log js/console (str  "flock:" birds))
  (let [pixels-per-millisecond (/ speed-pps 1000)
        delta-pixels (* delta-t-ms pixels-per-millisecond)
        new-birds (set (for [b birds]
                         (-> b
                             (update-direction-toward-mean-alignment birds alignment-r (:alignment *rotation-speed*))
                             (update-direction-toward-mean-position  birds cohesion-r (:cohesion *rotation-speed*))
                             (update-direction-away-mean-position birds seperation-r (:seperation *rotation-speed*))
                             (update-direction-off-the-walls w h seperation-r (:off-the-wall *rotation-speed*))
                             (move-bird h w delta-pixels)
                             )))]
    (assoc state :birds new-birds)))

(def inital-state {
                   :init true
                   :birds 
                   (set (for [i (range 0 15)] 
                          {:x ( + 50 (rand-int 900))
                           :y ( + 50 (rand-int 900)) 
                           :direction (rand-int 360) }))
                   }
  )


(defn update-pipeline
  [state]
  (-> 
   (merge inital-state state)
   merge-control-values
   move-flock
   ))


;; ---------------------------------------------------------------------
;; Render stack
;;
(defn clear-background
  [{:keys [ctx w h persist-image]}]
  (when-not persist-image
    (-> ctx
        (m/fill-style "rgba(25,29,33,0.75)") ;; Alpha adds motion blur
        (m/fill-rect {:x 0 :y 0 :w w :h h}))))

(defn x-y [x y raduis direction]
  (let  [d (math/radians direction) 
         dx (math/circle-x raduis d)
         dy (math/circle-y raduis d)]
    [(+ x dx) (+ y dy)]))

(defn draw-bird
  [ctx x y direction]
  ;;  (.log js/console (str  "draw-bird: " x "," y))
  (let [w 5 
        h 17
        [x1 y1] (x-y x y w (+ direction 90))
        [x2 y2] (x-y x y h direction)
        [x3 y3] (x-y x y w (+ direction 270))]
    (-> ctx 
        (m/fill-style "yellow")
        (m/stroke-style "yellow")
        (m/begin-path)
        (m/move-to x y)
        (m/line-to x1 y1)
        (m/line-to x2 y2)
        (m/line-to x3 y3)
        (m/line-to x y)
        (m/fill)
        (m/stroke)
        (m/close-path))))

(defn empty-circle [ctx {:keys [x y r color]}]
  (-> ctx
      (m/stroke-width 2)
      (m/stroke-style color)
      (m/begin-path)
      (. (arc x y r 0 (* (.-PI js/Math) 2) true)))
  (m/stroke ctx))


(defn draw-circles [ctx x y circles]
  (doseq [c circles]
    (when (:show c) 
      (do 
        (empty-circle ctx { :x x :y y :r (:r c) :color (:color c) })))
    ))


(defn draw-flock [{:keys [ctx birds show-seperation seperation-r show-alignment alignment-r show-cohesion cohesion-r] } ]
  (doseq [b birds]
    (draw-bird ctx (:x b) (:y b) (:direction b))
    (draw-circles ctx (:x b) (:y b) 
                  [ {:show show-seperation :r seperation-r :color "blue"}  
                    {:show show-alignment :r alignment-r :color "green"} 
                    {:show show-cohesion :r cohesion-r :color "gray"}
                    ]
                  ))
  )

(defn render-stack
  [state]
  (clear-background state)
  (draw-flock state)
  )


;; ---------------------------------------------------------------------
;; Main entry point
;;

(defmondrian flock-anim
  {}
  update-pipeline
  render-stack)
