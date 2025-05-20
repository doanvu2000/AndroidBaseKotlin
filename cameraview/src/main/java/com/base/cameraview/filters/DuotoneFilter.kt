package com.base.cameraview.filters

import android.graphics.Color
import android.opengl.GLES20
import androidx.annotation.ColorInt
import com.base.cameraview.filter.BaseFilter
import com.base.cameraview.filter.TwoParameterFilter
import com.otaliastudios.opengl.core.Egloo.checkGlError
import com.otaliastudios.opengl.core.Egloo.checkGlProgramLocation

/**
 * Representation of input frames using only two color tones.
 */
class DuotoneFilter : BaseFilter(), TwoParameterFilter {
    // Default values
    private var mFirstColor = Color.MAGENTA
    private var mSecondColor = Color.YELLOW
    private var mFirstColorLocation = -1
    private var mSecondColorLocation = -1

    /**
     * Sets the two duotone ARGB colors.
     *
     * @param firstColor  first
     * @param secondColor second
     */
    @Suppress("unused")
    fun setColors(@ColorInt firstColor: Int, @ColorInt secondColor: Int) {
        this.firstColor = firstColor
        this.secondColor = secondColor
    }

    @get:ColorInt
    @get:Suppress("unused")
    var firstColor: Int
        /**
         * Returns the first color.
         *
         * @return first
         * @see .setFirstColor
         */
        get() = mFirstColor
        /**
         * Sets the first of the duotone ARGB colors.
         * Defaults to [Color.MAGENTA].
         *
         * @param color first color
         */
        set(color) {
            // Remove any alpha.
            mFirstColor = Color.rgb(
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
        }

    @get:ColorInt
    @get:Suppress("unused")
    var secondColor: Int
        /**
         * Returns the second color.
         *
         * @return second
         * @see .setSecondColor
         */
        get() = mSecondColor
        /**
         * Sets the second of the duotone ARGB colors.
         * Defaults to [Color.YELLOW].
         *
         * @param color second color
         */
        set(color) {
            // Remove any alpha.
            mSecondColor = Color.rgb(
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
        }

    override var parameter1: Float
        get() {
            var color = this.firstColor
            color = Color.argb(
                0,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
            return color.toFloat() / 0xFFFFFF
        }
        set(value) {
            // no easy way to transform 0...1 into a color.
            this.firstColor = (value * 0xFFFFFF).toInt()
        }

    override var parameter2: Float
        get() {
            var color = this.secondColor
            color = Color.argb(
                0,
                Color.red(color),
                Color.green(color),
                Color.blue(color)
            )
            return color.toFloat() / 0xFFFFFF
        }
        set(value) {
            // no easy way to transform 0...1 into a color.
            this.secondColor = (value * 0xFFFFFF).toInt()
        }

    override fun onCreate(programHandle: Int) {
        super.onCreate(programHandle)
        mFirstColorLocation = GLES20.glGetUniformLocation(programHandle, "first")
        checkGlProgramLocation(mFirstColorLocation, "first")
        mSecondColorLocation = GLES20.glGetUniformLocation(programHandle, "second")
        checkGlProgramLocation(mSecondColorLocation, "second")
    }

    override fun onPreDraw(timestampUs: Long, transformMatrix: FloatArray) {
        super.onPreDraw(timestampUs, transformMatrix)
        val first = floatArrayOf(
            Color.red(mFirstColor) / 255f,
            Color.green(mFirstColor) / 255f,
            Color.blue(mFirstColor) / 255f
        )
        val second = floatArrayOf(
            Color.red(mSecondColor) / 255f,
            Color.green(mSecondColor) / 255f,
            Color.blue(mSecondColor) / 255f
        )
        GLES20.glUniform3fv(mFirstColorLocation, 1, first, 0)
        checkGlError("glUniform3fv")
        GLES20.glUniform3fv(mSecondColorLocation, 1, second, 0)
        checkGlError("glUniform3fv")
    }

    override fun onDestroy() {
        super.onDestroy()
        mFirstColorLocation = -1
        mSecondColorLocation = -1
    }

    override val fragmentShader: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        uniform samplerExternalOES sTexture;
        uniform vec3 first;
        uniform vec3 second;
        varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
        void main() {
          vec4 color = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
          float energy = (color.r + color.g + color.b) * 0.3333;
          vec3 new_color = (1.0 - energy) * first + energy * second;
          gl_FragColor = vec4(new_color.rgb, color.a);
        }
    """.trimIndent()
}