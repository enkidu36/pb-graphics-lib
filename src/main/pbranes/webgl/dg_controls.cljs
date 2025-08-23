(ns pbranes.webgl.dg-controls
  (:require ["dat.gui" :as dg]))

(defn folder?
  "Setting is a folder when it is a map and has no value"
  [setting]
  (and (map? setting) (nil? (:value setting))))

(defn action?
  "Setting is an action when it is a function"
  [setting]
  (fn? setting))

(defn color?
  "Setting is a color when there is a '#' or is a vector size greater than 3"
  [setting]
  (or
   (and (string? setting) (= "#" (re-find #"#" setting)))
   (and (vector? setting) (>= 3 (count setting)))))

(defn get-GUI
  "Returns existing GUI or makes a new one"
  [options]
  (if (:gui options)
    (:gui options)
    (dg/GUI. (clj->js options))))

(defn add-state! [gui k state]
  (.add gui (clj->js @state) (clj->js k)))

(defn add-folder! [gui k]
  (.addFolder gui (clj->js k)))

(defn add-color! [gui state k]
  (.addColor gui (clj->js @state) (clj->js k)))

(defn add-min-max! [gui state k min max step]
  (.add gui (clj->js @state) (clj->js k) min max step))

(defn make-controller! [gui k state setting]
  (let [{:keys [value
                min
                max
                step
                options]} setting
        {:keys [on-change] :or {on-change (fn [] nil)}} setting]
    (swap! state assoc k value)

    (let [controller (cond
                       options (.add gui (clj->js @state) (clj->js options))
                       (color? value) (add-color! gui state k)
                       :else (add-min-max! gui state k min max step))]

      (.onChange controller (clj->js (fn [v] (on-change v  @state)))))))

(defn
  configure-controls
  ([settings] (configure-controls settings {:width 300}))
  ([settings options]
   (let [gui (get-GUI options)
         state (atom {})]

     (for [[k v] settings]
       (cond
         (action? v) (do
                       (swap! state assoc k v)
                       (add-state! gui k state))

         (folder? v) (configure-controls v
                                         {:gui (add-folder! gui k)})

         :else
         (make-controller! gui k state v))))))

(comment
  (configure-controls  {"Parent Color" {:value 0
                                        :min 0
                                        :max 100
                                        :step 2
                                        :on-change (fn [v]
                                                     (js/console.log (str "Hello " v)))}
                        "Color" {"Sphere Color" {:value "#ff0000"}
                                 "Square Color" {:value "#00ff00"}
                                 "Triangle Color" {:value "#0000ff"}}})

;; comments
  )
