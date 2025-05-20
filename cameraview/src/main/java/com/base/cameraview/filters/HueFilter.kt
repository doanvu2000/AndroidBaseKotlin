package com.base.cameraview.filters

import android.opengl.GLES20
import com.base.cameraview.filter.BaseFilter
import com.base.cameraview.filter.OneParameterFilter
import com.otaliastudios.opengl.core.Egloo.checkGlError
import com.otaliastudios.opengl.core.Egloo.checkGlProgramLocation

/**
 * Applies a hue effect on the input frames.
 */
class HueFilter : BaseFilter(), OneParameterFilter {
    private var hue = 0.0f
    private var hueLocation = -1

    /**
     * Returns the current hue value.
     *
     * @return hue
     * @see .setHue
     */
    fun getHue(): Float {
        return hue
    }

    /**
     * Sets the hue value in degrees. See the values chart:
     * https://cloud.githubusercontent.com/assets/2201511/21810115/b99ac22a-d74a-11e6-9f6c-ef74d15c88c7.jpg
     *
     * @param hue hue degrees
     */
    @Suppress("unused")
    fun setHue(hue: Float) {
        this.hue = hue % 360
    }

    override var parameter1: Float
        get() = getHue() / 360f
        set(value) {
            setHue(value * 360f)
        }

    override fun onCreate(programHandle: Int) {
        super.onCreate(programHandle)
        hueLocation = GLES20.glGetUniformLocation(programHandle, "hue")
        checkGlProgramLocation(hueLocation, "hue")
    }

    override fun onDestroy() {
        super.onDestroy()
        hueLocation = -1
    }

    override fun onPreDraw(timestampUs: Long, transformMatrix: FloatArray) {
        super.onPreDraw(timestampUs, transformMatrix)
        // map it on 360 degree circle
        val shaderHue = ((hue - 45) / 45f + 0.5f) * -1
        GLES20.glUniform1f(hueLocation, shaderHue)
        checkGlError("glUniform1f")
    }

    override val fragmentShader: String = """
            #extension GL_OES_EGL_image_external : require
            precision mediump float;
            varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
            uniform samplerExternalOES sTexture;
            uniform float hue;
            void main() {
              vec4 kRGBToYPrime = vec4 (0.299, 0.587, 0.114, 0.0);
              vec4 kRGBToI = vec4 (0.595716, -0.274453, -0.321263, 0.0);
              vec4 kRGBToQ = vec4 (0.211456, -0.522591, 0.31135, 0.0);
              vec4 kYIQToR = vec4 (1.0, 0.9563, 0.6210, 0.0);
              vec4 kYIQToG = vec4 (1.0, -0.2721, -0.6474, 0.0);
              vec4 kYIQToB = vec4 (1.0, -1.1070, 1.7046, 0.0);
              vec4 color = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
              float YPrime = dot(color, kRGBToYPrime);
              float I = dot(color, kRGBToI);
              float Q = dot(color, kRGBToQ);
              float chroma = sqrt (I * I + Q * Q);
              Q = chroma * sin (hue);
              I = chroma * cos (hue);
              vec4 yIQ = vec4 (YPrime, I, Q, 0.0);
              color.r = dot (yIQ, kYIQToR);
              color.g = dot (yIQ, kYIQToG);
              color.b = dot (yIQ, kYIQToB);
              gl_FragColor = color;
            }
        """.trimIndent()
}