package com.base.cameraview.filters

import android.opengl.GLES20
import com.base.cameraview.filter.BaseFilter
import com.base.cameraview.filter.OneParameterFilter
import com.otaliastudios.opengl.core.Egloo.checkGlError
import com.otaliastudios.opengl.core.Egloo.checkGlProgramLocation

/**
 * Applies back-light filling to the frames.
 */
class FillLightFilter : BaseFilter(), OneParameterFilter {
    private var strength = 0.5f
    private var multiplierLocation = -1
    private var gammaLocation = -1

    /**
     * Returns the current strength.
     *
     * @return strength
     * @see .setStrength
     */
    @Suppress("unused")
    fun getStrength(): Float {
        return strength
    }

    /**
     * Sets the current strength.
     * 0.0: no change.
     * 1.0: max strength.
     *
     * @param strength strength
     */
    fun setStrength(strength: Float) {
        var strength = strength
        if (strength < 0.0f) strength = 0f
        if (strength > 1.0f) strength = 1f
        this.strength = strength
    }

    override var parameter1: Float
        get() = getStrength()
        set(value) {
            setStrength(value)
        }

    override fun onCreate(programHandle: Int) {
        super.onCreate(programHandle)
        multiplierLocation = GLES20.glGetUniformLocation(programHandle, "mult")
        checkGlProgramLocation(multiplierLocation, "mult")
        gammaLocation = GLES20.glGetUniformLocation(programHandle, "igamma")
        checkGlProgramLocation(gammaLocation, "igamma")
    }

    override fun onDestroy() {
        super.onDestroy()
        multiplierLocation = -1
        gammaLocation = -1
    }

    override fun onPreDraw(timestampUs: Long, transformMatrix: FloatArray) {
        super.onPreDraw(timestampUs, transformMatrix)
        val amount = 1.0f - strength
        val multiplier = 1.0f / (amount * 0.7f + 0.3f)
        GLES20.glUniform1f(multiplierLocation, multiplier)
        checkGlError("glUniform1f")

        val fadeGamma = 0.3f
        val faded = fadeGamma + (1.0f - fadeGamma) * multiplier
        val gamma = 1.0f / faded
        GLES20.glUniform1f(gammaLocation, gamma)
        checkGlError("glUniform1f")
    }

    override val fragmentShader: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        uniform samplerExternalOES sTexture;
        uniform float mult;
        uniform float igamma;
        varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
        void main() {
          const vec3 color_weights = vec3(0.25, 0.5, 0.25);
          vec4 color = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
          float lightmask = dot(color.rgb, color_weights);
          float backmask = (1.0 - lightmask);
          vec3 ones = vec3(1.0, 1.0, 1.0);
          vec3 diff = pow(mult * color.rgb, igamma * ones) - color.rgb;
          diff = min(diff, 1.0);
          vec3 new_color = min(color.rgb + diff * backmask, 1.0);
          gl_FragColor = vec4(new_color, color.a);
        }
        """.trimIndent()
}
