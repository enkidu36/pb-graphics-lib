(ns pbranes.app
  (:require [helix.core :refer [$]]
            [pbranes.page :refer [page]]
            ["react-dom/client" :as rdom]))

;; start your app with your favorite React renderer
(defonce root (rdom/createRoot (js/document.getElementById "root")))

(defn ^:dev/after-load init! []
  (.render root ($ page)))
