package com.base.cameraview.filters

import com.base.cameraview.filter.BaseFilter

/**
 * Converts the frames into black and white colors.
 */
class BlackAndWhiteFilter : BaseFilter() {
    override val fragmentShader: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
        uniform samplerExternalOES sTexture;
        void main() {
          vec4 color = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
          float colorR = (color.r + color.g + color.b) / 3.0;
          float colorG = (color.r + color.g + color.b) / 3.0;
          float colorB = (color.r + color.g + color.b) / 3.0;
          gl_FragColor = vec4(colorR, colorG, colorB, color.a);
        }
        """.trimIndent()
}

