package com.base.cameraview.filters

import com.base.cameraview.filter.BaseFilter

/**
 * Converts frames to gray scale.
 */
class GrayscaleFilter : BaseFilter() {
    override val fragmentShader: String = """
                #extension GL_OES_EGL_image_external : require
                precision mediump float;
                uniform samplerExternalOES sTexture;
                varying vec2 ${DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME};
                void main() {
                  vec4 color = texture2D(sTexture, ${DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME});
                  float y = dot(color, vec4(0.299, 0.587, 0.114, 0));
                  gl_FragColor = vec4(y, y, y, color.a);
                }""".trimIndent()
}
