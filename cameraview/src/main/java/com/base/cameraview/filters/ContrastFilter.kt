package com.base.cameraview.filters

import android.opengl.GLES20
import com.base.cameraview.filter.BaseFilter
import com.base.cameraview.filter.OneParameterFilter
import com.otaliastudios.opengl.core.Egloo.checkGlError
import com.otaliastudios.opengl.core.Egloo.checkGlProgramLocation

/**
 * Adjusts the contrast.
 */
class ContrastFilter : BaseFilter(), OneParameterFilter {
    private var contrast = 2f
    private var contrastLocation = -1

    /**
     * Returns the current contrast.
     *
     * @return contrast
     * @see .setContrast
     */
    @Suppress("unused")
    fun getContrast(): Float {
        return contrast
    }

    /**
     * Sets the current contrast adjustment.
     * 1.0: no adjustment
     * 2.0: increased contrast
     *
     * @param contrast contrast
     */
    fun setContrast(contrast: Float) {
        var contrast = contrast
        if (contrast < 1.0f) contrast = 1.0f
        if (contrast > 2.0f) contrast = 2.0f
        this.contrast = contrast
    }

    override var parameter1: Float
        get() =// parameter is 0...1, contrast is 1...2.
            getContrast() - 1f
        set(value) {
            // parameter is 0...1, contrast is 1...2.
            setContrast(value + 1)
        }

    override fun onCreate(programHandle: Int) {
        super.onCreate(programHandle)
        contrastLocation = GLES20.glGetUniformLocation(programHandle, "contrast")
        checkGlProgramLocation(contrastLocation, "contrast")
    }

    override fun onDestroy() {
        super.onDestroy()
        contrastLocation = -1
    }

    override fun onPreDraw(timestampUs: Long, transformMatrix: FloatArray) {
        super.onPreDraw(timestampUs, transformMatrix)
        GLES20.glUniform1f(contrastLocation, contrast)
        checkGlError("glUniform1f")
    }

    override val fragmentShader: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        uniform samplerExternalOES sTexture;
        uniform float contrast;
        varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
        void main() {
          vec4 color = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
          color -= 0.5;
          color *= contrast;
          color += 0.5;
          gl_FragColor = color;
        }
    """.trimIndent()
}
