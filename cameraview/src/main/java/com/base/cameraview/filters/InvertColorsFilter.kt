package com.base.cameraview.filters

import com.base.cameraview.filter.BaseFilter

/**
 * Inverts the input colors. This is also known as negative effect.
 */
class InvertColorsFilter : BaseFilter() {
    override val fragmentShader: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
        uniform samplerExternalOES sTexture;
        void main() {
          vec4 color = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
          float colorR = (1.0 - color.r) / 1.0;
          float colorG = (1.0 - color.g) / 1.0;
          float colorB = (1.0 - color.b) / 1.0;
          gl_FragColor = vec4(colorR, colorG, colorB, color.a);
        }""".trimIndent()
}