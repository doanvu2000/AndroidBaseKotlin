package com.base.cameraview.filters

import android.opengl.GLES20
import com.base.cameraview.filter.BaseFilter
import com.base.cameraview.filter.OneParameterFilter
import com.otaliastudios.opengl.core.Egloo.checkGlError
import com.otaliastudios.opengl.core.Egloo.checkGlProgramLocation

/**
 * Applies gamma correction to the frames.
 */
class GammaFilter : BaseFilter(), OneParameterFilter {
    private var gamma = 2.0f
    private var gammaLocation = -1

    /**
     * Returns the current gamma.
     *
     * @return gamma
     * @see .setGamma
     */
    fun getGamma(): Float {
        return gamma
    }

    /**
     * Sets the new gamma value in the 0.0 - 2.0 range.
     * The 1.0 value means no correction will be applied.
     *
     * @param gamma gamma value
     */
    fun setGamma(gamma: Float) {
        var gamma = gamma
        if (gamma < 0.0f) gamma = 0.0f
        if (gamma > 2.0f) gamma = 2.0f
        this.gamma = gamma
    }

    override var parameter1: Float
        get() = getGamma() / 2f
        set(value) {
            setGamma(value * 2f)
        }

    override fun onCreate(programHandle: Int) {
        super.onCreate(programHandle)
        gammaLocation = GLES20.glGetUniformLocation(programHandle, "gamma")
        checkGlProgramLocation(gammaLocation, "gamma")
    }

    override fun onDestroy() {
        super.onDestroy()
        gammaLocation = -1
    }

    override fun onPreDraw(timestampUs: Long, transformMatrix: FloatArray) {
        super.onPreDraw(timestampUs, transformMatrix)
        GLES20.glUniform1f(gammaLocation, gamma)
        checkGlError("glUniform1f")
    }

    override val fragmentShader: String = """
                #extension GL_OES_EGL_image_external : require
                precision mediump float;
                varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
                uniform samplerExternalOES sTexture;
                uniform float gamma;
                void main() {
                  vec4 textureColor = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
                  gl_FragColor = vec4(pow(textureColor.rgb, vec3(gamma)), textureColor.w);
                }
                """.trimIndent()
}