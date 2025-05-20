package com.base.cameraview.filters

import android.opengl.GLES20
import com.base.cameraview.filter.BaseFilter
import com.base.cameraview.filter.OneParameterFilter
import com.otaliastudios.opengl.core.Egloo.checkGlError
import com.otaliastudios.opengl.core.Egloo.checkGlProgramLocation

/**
 * Adjusts color temperature.
 */
class TemperatureFilter : BaseFilter(), OneParameterFilter {
    private var scale = 1f // -1...1
    private var scaleLocation = -1

    var temperature: Float
        /**
         * Returns the current temperature.
         *
         * @return temperature
         * @see .setTemperature
         */
        get() = scale
        /**
         * Sets the new temperature value:
         * -1.0: cool colors
         * 0.0: no change
         * 1.0: warm colors
         *
         * @param value new value
         */
        set(value) {
            var value = value
            if (value < -1f) value = -1f
            if (value > 1f) value = 1f
            this.scale = value
        }

    override var parameter1: Float
        get() = (this.temperature + 1f) / 2f
        set(value) {
            this.temperature = (2f * value - 1f)
        }

    override fun onCreate(programHandle: Int) {
        super.onCreate(programHandle)
        scaleLocation = GLES20.glGetUniformLocation(programHandle, "scale")
        checkGlProgramLocation(scaleLocation, "scale")
    }

    override fun onDestroy() {
        super.onDestroy()
        scaleLocation = -1
    }

    override fun onPreDraw(timestampUs: Long, transformMatrix: FloatArray) {
        super.onPreDraw(timestampUs, transformMatrix)
        GLES20.glUniform1f(scaleLocation, scale)
        checkGlError("glUniform1f")
    }

    override val fragmentShader: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        uniform samplerExternalOES sTexture;
        uniform float scale;
        varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
        void main() {
          vec4 color = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
          vec3 new_color = color.rgb;
          new_color.r = color.r + color.r * ( 1.0 - color.r) * scale;
          new_color.b = color.b - color.b * ( 1.0 - color.b) * scale;
          if (scale > 0.0) { 
            new_color.g = color.g + color.g * ( 1.0 - color.g) * scale * 0.25;
          }
          float max_value = max(new_color.r, max(new_color.g, new_color.b));
          if (max_value > 1.0) { 
             new_color /= max_value;
          } 
          gl_FragColor = vec4(new_color, color.a);
        }""".trimIndent()
}
