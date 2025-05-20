package com.base.cameraview.filters

import com.base.cameraview.filter.BaseFilter

/**
 * Applies a posterization effect to the input frames.
 */
class PosterizeFilter : BaseFilter() {
    override val fragmentShader: String = """
                #extension GL_OES_EGL_image_external : require
                precision mediump float;
                uniform samplerExternalOES sTexture;
                varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
                void main() {
                  vec4 color = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
                  vec3 pcolor;
                  pcolor.r = (color.r >= 0.5) ? 0.75 : 0.25;
                  pcolor.g = (color.g >= 0.5) ? 0.75 : 0.25;
                  pcolor.b = (color.b >= 0.5) ? 0.75 : 0.25;
                  gl_FragColor = vec4(pcolor, color.a);
                }
                """.trimIndent()
}
