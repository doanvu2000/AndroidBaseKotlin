package com.base.cameraview.filters

import com.base.cameraview.filter.BaseFilter

/**
 * Applies a cross process effect, in which the red and green channels
 * are enhanced while the blue channel is restricted.
 */
class CrossProcessFilter : BaseFilter() {
    override val fragmentShader: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        uniform samplerExternalOES sTexture;
        varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
        void main() {
          vec4 color = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
          vec3 ncolor = vec3(0.0, 0.0, 0.0);
          float value;
          if (color.r < 0.5) {
            value = color.r;
          } else {
            value = 1.0 - color.r;
          }
          float red = 4.0 * value * value * value;
          if (color.r < 0.5) {
            ncolor.r = red;
          } else {
            ncolor.r = 1.0 - red;
          }
          if (color.g < 0.5) {
            value = color.g;
          } else {
            value = 1.0 - color.g;
          }
          float green = 2.0 * value * value;
          if (color.g < 0.5) {
            ncolor.g = green;
          } else {
            ncolor.g = 1.0 - green;
          }
          ncolor.b = color.b * 0.5 + 0.25;
          gl_FragColor = vec4(ncolor.rgb, color.a);
        }
    """.trimIndent()
}

