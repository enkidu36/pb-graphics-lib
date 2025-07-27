(ns pbranes.core
  (:require [pbranes.webgl.constants :as pwc]))

(defn init! []
  (js/console.log pwc/BUFFER-SIZE)
  (js/console.log "Hello pbranes-graphics-lib"))
