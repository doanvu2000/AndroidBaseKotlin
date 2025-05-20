package com.base.cameraview.filters

import android.opengl.GLES20
import com.base.cameraview.filter.BaseFilter
import com.base.cameraview.filter.OneParameterFilter
import com.otaliastudios.opengl.core.Egloo.checkGlError
import com.otaliastudios.opengl.core.Egloo.checkGlProgramLocation

/**
 * Adjusts color saturation.
 */
class SaturationFilter : BaseFilter(), OneParameterFilter {
    private var scale = 1f // -1...1
    private var scaleLocation = -1
    private var exponentsLocation = -1

    var saturation: Float
        /**
         * Returns the current saturation.
         *
         * @return saturation
         * @see .setSaturation
         */
        get() = scale
        /**
         * Sets the saturation correction value:
         * -1.0: fully desaturated, grayscale.
         * 0.0: no change.
         * +1.0: fully saturated.
         *
         * @param value new value
         */
        set(value) {
            var value = value
            if (value < -1f) value = -1f
            if (value > 1f) value = 1f
            scale = value
        }

    override var parameter1: Float
        get() = (this.saturation + 1f) / 2f
        set(value) {
            this.saturation = 2f * value - 1f
        }

    override fun onCreate(programHandle: Int) {
        super.onCreate(programHandle)
        scaleLocation = GLES20.glGetUniformLocation(programHandle, "scale")
        checkGlProgramLocation(scaleLocation, "scale")
        exponentsLocation = GLES20.glGetUniformLocation(programHandle, "exponents")
        checkGlProgramLocation(exponentsLocation, "exponents")
    }

    override fun onDestroy() {
        super.onDestroy()
        scaleLocation = -1
        exponentsLocation = -1
    }

    override fun onPreDraw(timestampUs: Long, transformMatrix: FloatArray) {
        super.onPreDraw(timestampUs, transformMatrix)
        if (scale > 0.0f) {
            GLES20.glUniform1f(scaleLocation, 0f)
            checkGlError("glUniform1f")
            GLES20.glUniform3f(
                exponentsLocation,
                (0.9f * scale) + 1.0f,
                (2.1f * scale) + 1.0f,
                (2.7f * scale) + 1.0f
            )
            checkGlError("glUniform3f")
        } else {
            GLES20.glUniform1f(scaleLocation, 1.0f + scale)
            checkGlError("glUniform1f")
            GLES20.glUniform3f(exponentsLocation, 0f, 0f, 0f)
            checkGlError("glUniform3f")
        }
    }

    override val fragmentShader: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        uniform samplerExternalOES sTexture;
        uniform float scale;
        uniform vec3 exponents;
        float shift;
        vec3 weights;
        varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
        void main() {
          weights[0] = ${2f / 8f};
          weights[1] = ${5f / 8f};
          weights[2] = ${1f / 8f};
          shift = ${1.0f / 255.0f};
          vec4 oldcolor = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
          float kv = dot(oldcolor.rgb, weights) + shift;
          vec3 new_color = scale * oldcolor.rgb + (1.0 - scale) * kv;
          gl_FragColor = vec4(new_color, oldcolor.a);
          vec4 color = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
          float de = dot(color.rgb, weights);
          float inv_de = 1.0 / de;
          vec3 verynew_color = de * pow(color.rgb * inv_de, exponents);
          float max_color = max(max(max(verynew_color.r, verynew_color.g), verynew_color.b), 1.0);
          gl_FragColor = gl_FragColor+vec4(verynew_color / max_color, color.a);
        }
    """.trimIndent()
}
