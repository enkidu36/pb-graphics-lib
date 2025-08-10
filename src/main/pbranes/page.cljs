(ns pbranes.page
  (:require [helix.core :refer [defnc $ <>]]
            [helix.dom :as d]
            [helix.hooks :as hooks]
            [pbranes.utils :as u]
            ["dat.gui" :as dg]))

(set! *warn-on-infer* false)

;; Square vertices in clip space.
;; Clipspace coordinates go from -1 to 1 regardless of size of canvas
(def vertices
  [-50 0 0
   50 0 0
   -1 0.25 0
   1 0.25 0
   ])

(def indices
  [0 1 2 3])

(def dat-gui (atom nil))

(def vs-shader
  "#version 300 es
precision mediump float;

// Supplied vertex position attribute
 in  vec3 aVertexPosition;

void main(void) {
  // Set the position in the clipspace coordinates
  gl_Position = vec4(aVertexPosition, 1.0);
  gl_PointSize = 5.0;
}
")

(def fs-shader
  "#version 300 es
  precision mediump float;

 // Color that is the result of this shader
  out  vec4 fragColor;

  void main(void) {
      fragColor = vec4(1.0, 0.0, 0.0, 1.0);
  }
")

(defn init-program [gl]
  (let [vertex-shader (u/compile-shader gl vs-shader (.-VERTEX_SHADER gl))
        fragment-shader (u/compile-shader gl fs-shader (.-FRAGMENT_SHADER gl))
        program (.createProgram gl)]

    (.attachShader gl program vertex-shader)
    (.attachShader gl program fragment-shader)
    (.linkProgram gl program)

    (when (not (.getProgramParameter gl program (.-LINK_STATUS gl)))
      (js/console.error "Could not initialize shaders"))

    ;; Use this program instance
    (.useProgram gl program)

      ;; Attaching for easy access in the code
    (set! (.-aVertexPosition program) (.getAttribLocation gl program  "aVertexPosition"))

    ;; return program
    program))

(defn init-buffers [gl program vbo ibo]
  (let [vertex-array (u/create-vertex-array gl)
        index-buffer (u/create-index-buffer gl ibo)]

    ;; Create vertex array object
    (.bindVertexArray gl vertex-array)

    (u/create-vertex-buffer gl vbo)

    ;; Provide instructions for VAO to use later in Draw
    (.enableVertexAttribArray gl (.-aVertexPosition program))
    (.vertexAttribPointer gl (.-aVertexPosition program) 3 (.-FLOAT gl) false 0 0)

    (u/clear-all-arrays-buffers gl)

    {:vertex-array vertex-array
     :index-buffer index-buffer}))

(defn draw [gl buffers]
  ;; clear the scene
  (u/clear-scene gl)

  (.bindVertexArray gl (:vertex-array buffers))
  (.bindBuffer gl (.-ELEMENT_ARRAY_BUFFER gl) (:index-buffer buffers))

  (.drawElements gl (.-LINES gl) (count indices) (.-UNSIGNED_SHORT gl) 0)

  ;; clean
  (u/clear-all-arrays-buffers gl))

(defn init [gl controls]
  (.clearColor gl 0 0 0 1)
  (let [floor (js/Floor.)
        program (init-program gl)
        buffers (init-buffers gl program vertices indices)
        pallette (clj->js {:color1 "#FF0000"})]

    (js/console.log floor.vertices)
    (js/console.log floor.indices)
    
    ;; (.addFolder controls "Folder")
    ;; (.addColor controls pallette "color1")

    (draw gl buffers)))

(defnc page []
  (let [canvas (hooks/use-ref nil)]

    (hooks/use-effect
     :once
     (let [gl (u/get-context canvas)
           controls (dg/GUI.)]
       (init gl controls)

       (fn unmount []
         (.destroy (.getRoot controls))
         (js/console.log "unmount"))))

    (d/canvas {:ref canvas :className "webgl-canvas" :height 600 :width 800}
              "Your browser does not support HTML5 canvas.")))
