package com.base.cameraview.filters

import com.base.cameraview.filter.BaseFilter

/**
 * Converts frames to sepia tone.
 */
class SepiaFilter : BaseFilter() {
    override val fragmentShader: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        uniform samplerExternalOES sTexture;
        mat3 matrix;
        varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
        void main() {
          matrix[0][0]=${805.0f / 2048.0f};
          matrix[0][1]=${715.0f / 2048.0f};
          matrix[0][2]=${557.0f / 2048.0f};
          matrix[1][0]=${1575.0f / 2048.0f};
          matrix[1][1]=${1405.0f / 2048.0f};
          matrix[1][2]=${1097.0f / 2048.0f};
          matrix[2][0]=${387.0f / 2048.0f};
          matrix[2][1]=${344.0f / 2048.0f};
          matrix[2][2]=${268.0f / 2048.0f};
          vec4 color = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
          vec3 new_color = min(matrix * color.rgb, 1.0);
          gl_FragColor = vec4(new_color.rgb, color.a);
        }
        """.trimIndent()
}
