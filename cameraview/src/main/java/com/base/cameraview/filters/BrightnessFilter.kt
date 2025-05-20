package com.base.cameraview.filters

import android.opengl.GLES20
import com.base.cameraview.filter.BaseFilter
import com.base.cameraview.filter.OneParameterFilter
import com.otaliastudios.opengl.core.Egloo.checkGlError
import com.otaliastudios.opengl.core.Egloo.checkGlProgramLocation

/**
 * Adjusts the brightness of the frames.
 */
class BrightnessFilter : BaseFilter(), OneParameterFilter {
    private var brightness = 2.0f // 1.0F...2.0F
    private var brightnessLocation = -1


    /**
     * Returns the current brightness.
     *
     * @return brightness
     * @see .setBrightness
     */
    @Suppress("unused")
    fun getBrightness(): Float {
        return brightness
    }

    /**
     * Sets the brightness adjustment.
     * 1.0: normal brightness.
     * 2.0: high brightness.
     *
     * @param brightness brightness.
     */
    @Suppress("unused")
    fun setBrightness(brightness: Float) {
        var brightness = brightness
        if (brightness < 1.0f) brightness = 1.0f
        if (brightness > 2.0f) brightness = 2.0f
        this.brightness = brightness
    }

    override var parameter1: Float
        get() =// parameter is 0...1, brightness is 1...2.
            getBrightness() - 1f
        set(value) {
            // parameter is 0...1, brightness is 1...2.
            setBrightness(value + 1)
        }

    override fun onCreate(programHandle: Int) {
        super.onCreate(programHandle)
        brightnessLocation = GLES20.glGetUniformLocation(programHandle, "brightness")
        checkGlProgramLocation(brightnessLocation, "brightness")
    }

    override fun onDestroy() {
        super.onDestroy()
        brightnessLocation = -1
    }

    override fun onPreDraw(timestampUs: Long, transformMatrix: FloatArray) {
        super.onPreDraw(timestampUs, transformMatrix)
        GLES20.glUniform1f(brightnessLocation, brightness)
        checkGlError("glUniform1f")
    }

    override val fragmentShader: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        uniform samplerExternalOES sTexture;
        uniform float brightness;
        varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
        void main() {
          vec4 color = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
          gl_FragColor = brightness * color;
        }
        """.trimIndent()
}
