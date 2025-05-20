package com.base.cameraview.filters

import android.graphics.Color
import android.opengl.GLES20
import androidx.annotation.ColorInt
import com.base.cameraview.filter.BaseFilter
import com.base.cameraview.filter.OneParameterFilter
import com.otaliastudios.opengl.core.Egloo.checkGlError
import com.otaliastudios.opengl.core.Egloo.checkGlProgramLocation

/**
 * Tints the frames with specified color.
 */
class TintFilter : BaseFilter(), OneParameterFilter {
    private var tint = Color.RED
    private var tintLocation = -1

    /**
     * Returns the current tint.
     *
     * @return tint
     * @see .setTint
     */
    @ColorInt
    fun getTint(): Int {
        return tint
    }

    /**
     * Sets the current tint.
     *
     * @param color current tint
     */
    fun setTint(@ColorInt color: Int) {
        // Remove any alpha.
        this.tint = Color.rgb(Color.red(color), Color.green(color), Color.blue(color))
    }

    override var parameter1: Float
        get() {
            var color = getTint()
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
            setTint((value * 0xFFFFFF).toInt())
        }

    override fun onCreate(programHandle: Int) {
        super.onCreate(programHandle)
        tintLocation = GLES20.glGetUniformLocation(programHandle, "tint")
        checkGlProgramLocation(tintLocation, "tint")
    }

    override fun onDestroy() {
        super.onDestroy()
        tintLocation = -1
    }

    override fun onPreDraw(timestampUs: Long, transformMatrix: FloatArray) {
        super.onPreDraw(timestampUs, transformMatrix)
        val channels = floatArrayOf(
            Color.red(tint) / 255f,
            Color.green(tint) / 255f,
            Color.blue(tint) / 255f
        )
        GLES20.glUniform3fv(tintLocation, 1, channels, 0)
        checkGlError("glUniform3fv")
    }

    override val fragmentShader: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        uniform samplerExternalOES sTexture;
        uniform vec3 tint;
        vec3 color_ratio;
        varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
        void main() {
          color_ratio[0] = ${0.21f};
          color_ratio[1] = ${0.71f};
          color_ratio[2] = ${0.07f};
          vec4 color = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
          float avg_color = dot(color_ratio, color.rgb);
          vec3 new_color = min(0.8 * avg_color + 0.2 * tint, 1.0);
          gl_FragColor = vec4(new_color.rgb, color.a);
        }""".trimIndent()
}
