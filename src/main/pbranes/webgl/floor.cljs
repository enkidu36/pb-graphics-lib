(ns pbranes.webgl.floor)

(defn combine-tvecs
  "Combines transient axis vectors into one persistent vector"
  [key axis-1 axis-2]

  (persistent! (reduce conj! (key axis-1) (persistent! (key  axis-2)))))

(defn build-floor [{:keys [dimension lines] :as floor}]
  (let [increment (* 2 (/ dimension lines))]
    (loop [l 0
           axis-1 {:vertices (transient []) :indices (transient [])}
           axis-2 {:vertices (transient []) :indices (transient [])}]
      (if (> l lines)
        ;; return floor with combined axis verticies and indices
        ;; into one persistent vector for each.
        (-> floor
            (assoc :vertices (combine-tvecs :vertices axis-1 axis-2))
            (assoc :indices  (combine-tvecs :indices axis-1 axis-2)))

        ;; loop two builds two axis to be combined
        ;; breaking out of loop
        (let [v1 (reduce conj! (:vertices axis-1)
                         [(- dimension) 0 (+ (- dimension) (* l increment))
                          dimension     0 (+ (- dimension) (* l increment))])

              i-1 (reduce conj! (:indices axis-1) [(* 2 l)
                                                   (+ (* 2 l) 1)])

              v2 (reduce conj! (:vertices axis-2)
                         [(+ (- dimension) (* l increment)) 0  (- dimension)
                          (+ (- dimension) (* l increment)) 0  dimension])

              i-2 (reduce conj! (:indices axis-2)
                          [(+ (* 2 (+ lines 1)) (* 2 l))
                           (+ (* 2 (+ lines 1)) (* 2 l) 1)])]

          (recur (inc l)
                 (-> axis-1 (assoc :vertices v1) (assoc :indices i-1))
                 (-> axis-2 (assoc :vertices v2) (assoc :indices i-2))))))))


(defn create-floor [dimension lines]

  (let [floor {:alias "floor"
               :dimension dimension
               :lines (* 2 (/ dimension lines))
               :wireframe true
               :visible true
               :vertices []
               :indices []}]
    (build-floor floor)))




(defn make-floor [dimension lines]

  (let [cnt (* 2 (/ dimension lines))
        increment (* (/ dimension cnt))
        sz  (+ (* 2 (+ cnt 1)) (* 2 cnt) 1)
        v (into [] (range sz))
        i (into [] (range 52))]

    (reduce  +  v)
    
    ))

(make-floor 50 5)

(comment

  (def floor (create-floor 50 5))

  (def vs (:vertices floor))



  (println floor)

  ;; Helper function to partition axis line vertices
  (def part-line-vec (partial partition 6))

  ;; Print out vertices as sequences
  (map #(println %) (-> floor
                        (:vertices)
                        (part-line-vec)))
  ;; end comments
  )
