package com.base.cameraview.filters

import android.opengl.GLES20
import com.base.cameraview.filter.BaseFilter
import com.base.cameraview.filter.OneParameterFilter
import com.otaliastudios.opengl.core.Egloo.checkGlError
import com.otaliastudios.opengl.core.Egloo.checkGlProgramLocation

/**
 * Sharpens the input frames.
 */
class SharpnessFilter : BaseFilter(), OneParameterFilter {
    private var scale = 0.5f
    private var width = 1
    private var height = 1
    private var scaleLocation = -1
    private var stepSizeXLocation = -1
    private var stepSizeYLocation = -1

    override fun setSize(width: Int, height: Int) {
        super.setSize(width, height)
        this.width = width
        this.height = height
    }

    var sharpness: Float
        /**
         * Returns the current sharpness.
         *
         * @return sharpness
         * @see .setSharpness
         */
        get() = scale
        /**
         * Sets the current sharpness value:
         * 0.0: no change.
         * 1.0: maximum sharpness.
         *
         * @param value new sharpness
         */
        set(value) {
            var value = value
            if (value < 0.0f) value = 0.0f
            if (value > 1.0f) value = 1.0f
            this.scale = value
        }

    override var parameter1: Float
        get() = this.sharpness
        set(value) {
            this.sharpness = value
        }

    override fun onCreate(programHandle: Int) {
        super.onCreate(programHandle)
        scaleLocation = GLES20.glGetUniformLocation(programHandle, "scale")
        checkGlProgramLocation(scaleLocation, "scale")
        stepSizeXLocation = GLES20.glGetUniformLocation(programHandle, "stepsizeX")
        checkGlProgramLocation(stepSizeXLocation, "stepsizeX")
        stepSizeYLocation = GLES20.glGetUniformLocation(programHandle, "stepsizeY")
        checkGlProgramLocation(stepSizeYLocation, "stepsizeY")
    }

    override fun onDestroy() {
        super.onDestroy()
        scaleLocation = -1
        stepSizeXLocation = -1
        stepSizeYLocation = -1
    }

    override fun onPreDraw(timestampUs: Long, transformMatrix: FloatArray) {
        super.onPreDraw(timestampUs, transformMatrix)
        GLES20.glUniform1f(scaleLocation, scale)
        checkGlError("glUniform1f")
        GLES20.glUniform1f(stepSizeXLocation, 1.0f / width)
        checkGlError("glUniform1f")
        GLES20.glUniform1f(stepSizeYLocation, 1.0f / height)
        checkGlError("glUniform1f")
    }

    override val fragmentShader: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        uniform samplerExternalOES sTexture;
        uniform float scale;
        uniform float stepsizeX;
        uniform float stepsizeY;
        varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
        void main() {
          vec3 nbr_color = vec3(0.0, 0.0, 0.0);
          vec2 coord;
          vec4 color = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
          coord.x = $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME.x - 0.5 * stepsizeX;
          coord.y = $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME.y - stepsizeY;
          nbr_color += texture2D(sTexture, coord).rgb - color.rgb;
          coord.x = $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME.x - stepsizeX;
          coord.y = $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME.y + 0.5 * stepsizeY;
          nbr_color += texture2D(sTexture, coord).rgb - color.rgb;
          coord.x = $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME.x + stepsizeX;
          coord.y = $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME.y - 0.5 * stepsizeY;
          nbr_color += texture2D(sTexture, coord).rgb - color.rgb;
          coord.x = $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME.x + stepsizeX;
          coord.y = $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME.y + 0.5 * stepsizeY;
          nbr_color += texture2D(sTexture, coord).rgb - color.rgb;
          gl_FragColor = vec4(color.rgb - 2.0 * scale * nbr_color, color.a);
        }
        """.trimIndent()
}
