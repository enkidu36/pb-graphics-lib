(ns pbranes.page
  (:require [helix.core :refer [defnc $ <>]]
            [helix.dom :as d]
            [helix.hooks :as hooks]
            [pbranes.webgl.utils :as u]
            [pbranes.webgl.constants :refer [ARRAY-BUFFER
                                             ELEMENT-ARRAY-BUFFER
                                             FLOAT
                                             FRAGMENT-SHADER
                                             LINK-STATUS
                                             TRIANGLES
                                             STATIC-DRAW
                                             UNSIGNED-SHORT
                                             VERTEX-SHADER

]]
            ["dat.gui" :as dg]))

(set! *warn-on-infer* false)

;; Square vertices in clip space.
;; Clipspace coordinates go from -1 to 1 regardless of size of canvas
(def vertices
  [-0.5  0.5 0
   -0.5 -0.5 0
    0.5 -0.5 0
    0.5  0.5 0
   ])

(def indices
  [0 1 2
   0 2 3])

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
      fragColor = vec4(1.0, 0.0, 1.0, 1.0);
  }
")

(defn init-program [gl]
  (let [vertex-shader (u/compile-shader gl vs-shader VERTEX-SHADER)
        fragment-shader (u/compile-shader gl fs-shader FRAGMENT-SHADER)
        program (.createProgram gl)]

    (.attachShader gl program vertex-shader)
    (.attachShader gl program fragment-shader)
    (.linkProgram gl program)

    (when (not (.getProgramParameter gl program LINK-STATUS))
      (js/console.error "Could not initialize shaders"))

    ;; Use this program instance
    (.useProgram gl program)

      ;; Attaching for easy access in the code
    (set! (.-aVertexPosition program) (.getAttribLocation gl program  "aVertexPosition"))

    ;; return program
    program))

(defn init-buffers [gl program vs is]
  (let [vertex-array (u/create-vertex-array gl)
        vertex-buffer (u/create-vertex-buffer gl vs)
        index-buffer (u/create-index-buffer gl is)]

    ;; Create vertex array object
    (.bindVertexArray gl vertex-array)
    (.bindBuffer gl ARRAY-BUFFER vertex-buffer)
    (.bufferData gl ARRAY-BUFFER (js/Float32Array. vs) STATIC-DRAW )

    ;; Provide instructions for VAO to use later in Draw
    (.enableVertexAttribArray gl (.-aVertexPosition program))
    (.vertexAttribPointer gl (.-aVertexPosition program) 3 FLOAT false 0 0)

    ;; Setting up the IBO
    (.bindBuffer gl ELEMENT-ARRAY-BUFFER  index-buffer)
    (.bufferData gl ELEMENT-ARRAY-BUFFER (js/Uint16Array. is) STATIC-DRAW)

    (u/clear-all-arrays-buffers gl)

    {:vertex-array vertex-array
     :index-buffer index-buffer}))

(defn draw [gl buffers]
  ;; clear the scene
  (u/clear-scene gl)

  (.bindVertexArray gl (:vertex-array buffers))
  (.bindBuffer gl ELEMENT-ARRAY-BUFFER (:index-buffer buffers))

  (.drawElements gl TRIANGLES (count indices) UNSIGNED-SHORT 0)

  ;; clean
  (u/clear-all-arrays-buffers gl))

(defn init [gl controls]

  ;; Set canvas size and clear color
  (u/auto-resize-canvas (.-canvas gl))
  (.clearColor gl 0 0 0 1)

  (let [program (init-program gl)
        buffers (init-buffers gl program vertices indices)]
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
