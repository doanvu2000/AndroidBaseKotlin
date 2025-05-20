package com.base.cameraview.filters

import android.opengl.GLES20
import com.base.cameraview.filter.BaseFilter
import com.base.cameraview.filter.OneParameterFilter
import com.otaliastudios.opengl.core.Egloo.checkGlError
import com.otaliastudios.opengl.core.Egloo.checkGlProgramLocation

/**
 * Attempts to auto-fix the frames based on histogram equalization.
 */
class AutoFixFilter : BaseFilter(), OneParameterFilter {
    private var scale = 1.0f
    private var scaleLocation = -1

    /**
     * Returns the current scale.
     *
     * @return current scale
     * @see .setScale
     */
    fun getScale(): Float {
        return scale
    }

    /**
     * A parameter between 0 and 1. Zero means no adjustment, while 1 indicates
     * the maximum amount of adjustment.
     *
     * @param scale scale
     */
    fun setScale(scale: Float) {
        var scale = scale
        if (scale < 0.0f) scale = 0.0f
        if (scale > 1.0f) scale = 1.0f
        this.scale = scale
    }

    override var parameter1: Float
        get() = getScale()
        set(value) {
            setScale(value)
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
            uniform samplerExternalOES tex_sampler_0;
            uniform samplerExternalOES tex_sampler_1;
            uniform samplerExternalOES tex_sampler_2;
            uniform float scale;
            float shift_scale;
            float hist_offset;
            float hist_scale;
            float density_offset;
            float density_scale;
            varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
            void main() {
              shift_scale = ${1.0f / 256f};
              hist_offset = ${0.5f / 766f};
              hist_scale = ${765f / 766f};
              density_offset = ${0.5f / 1024f};
              density_scale = ${1023f / 1024f};
              const vec3 weights = vec3(0.33333, 0.33333, 0.33333);
              vec4 color = texture2D(tex_sampler_0, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
              float energy = dot(color.rgb, weights);
              float mask_value = energy - 0.5;
              float alpha;
              if (mask_value > 0.0) {
                alpha = (pow(2.0 * mask_value, 1.5) - 1.0) * scale + 1.0;
              } else { 
                alpha = (pow(2.0 * mask_value, 2.0) - 1.0) * scale + 1.0;
              }
              float index = energy * hist_scale + hist_offset;
              vec4 temp = texture2D(tex_sampler_1, vec2(index, 0.5));
              float value = temp.g + temp.r * shift_scale;
              index = value * density_scale + density_offset;
              temp = texture2D(tex_sampler_2, vec2(index, 0.5));
              value = temp.g + temp.r * shift_scale;
              float dst_energy = energy * alpha + value * (1.0 - alpha);
              float max_energy = energy / max(color.r, max(color.g, color.b));
              if (dst_energy > max_energy) {
                dst_energy = max_energy;
              }
              if (energy == 0.0) {
                gl_FragColor = color;
              } else {
                gl_FragColor = vec4(color.rgb * dst_energy / energy, color.a);
              }
            }
        """.trimIndent()
}
