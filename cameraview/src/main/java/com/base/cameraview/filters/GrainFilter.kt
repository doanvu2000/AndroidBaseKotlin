package com.base.cameraview.filters

import android.opengl.GLES20
import com.base.cameraview.filter.BaseFilter
import com.base.cameraview.filter.OneParameterFilter
import com.otaliastudios.opengl.core.Egloo.checkGlError
import com.otaliastudios.opengl.core.Egloo.checkGlProgramLocation
import java.util.Random

/**
 * Applies film grain effect to the frames.
 */
class GrainFilter : BaseFilter(), OneParameterFilter {
    private var strength = 0.5f
    private var width = 1
    private var height = 1
    private var strengthLocation = -1
    private var stepXLocation = -1
    private var stepYLocation = -1

    override fun setSize(width: Int, height: Int) {
        super.setSize(width, height)
        this.width = width
        this.height = height
    }

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
     * Sets the current distortion strength.
     * 0.0: no distortion.
     * 1.0: maximum distortion.
     *
     * @param strength strength
     */
    fun setStrength(strength: Float) {
        var strength = strength
        if (strength < 0.0f) strength = 0.0f
        if (strength > 1.0f) strength = 1.0f
        this.strength = strength
    }

    override var parameter1: Float
        get() = getStrength()
        set(value) {
            setStrength(value)
        }

    override fun onCreate(programHandle: Int) {
        super.onCreate(programHandle)
        strengthLocation = GLES20.glGetUniformLocation(programHandle, "scale")
        checkGlProgramLocation(strengthLocation, "scale")
        stepXLocation = GLES20.glGetUniformLocation(programHandle, "stepX")
        checkGlProgramLocation(stepXLocation, "stepX")
        stepYLocation = GLES20.glGetUniformLocation(programHandle, "stepY")
        checkGlProgramLocation(stepYLocation, "stepY")
    }

    override fun onDestroy() {
        super.onDestroy()
        strengthLocation = -1
        stepXLocation = -1
        stepYLocation = -1
    }

    override fun onPreDraw(timestampUs: Long, transformMatrix: FloatArray) {
        super.onPreDraw(timestampUs, transformMatrix)
        GLES20.glUniform1f(strengthLocation, strength)
        checkGlError("glUniform1f")
        GLES20.glUniform1f(stepXLocation, 0.5f / width)
        checkGlError("glUniform1f")
        GLES20.glUniform1f(stepYLocation, 0.5f / height)
        checkGlError("glUniform1f")
    }

    private val RANDOM = Random()
    override val fragmentShader: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        vec2 seed;
        varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
        uniform samplerExternalOES tex_sampler_0;
        uniform samplerExternalOES tex_sampler_1;
        uniform float scale;
        uniform float stepX;
        uniform float stepY;
        float rand(vec2 loc) {
          float theta1 = dot(loc, vec2(0.9898, 0.233));
          float theta2 = dot(loc, vec2(12.0, 78.0));
          float value = cos(theta1) * sin(theta2) + sin(theta1) * cos(theta2); // keep value of part1 in range: (2^-14 to 2^14).
          float temp = mod(197.0 * value, 1.0) + value;
          float part1 = mod(220.0 * temp, 1.0) + temp;
          float part2 = value * 0.5453;
          float part3 = cos(theta1 + theta2) * 0.43758;
          float sum = (part1 + part2 + part3);
          return fract(sum)*scale;
        }
        void main() {
          seed[0] = ${RANDOM.nextFloat()};
          seed[1] = ${RANDOM.nextFloat()};
          float noise = texture2D(tex_sampler_1, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + vec2(-stepX, -stepY)).r * 0.224;
          noise += texture2D(tex_sampler_1, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + vec2(-stepX, stepY)).r * 0.224;
          noise += texture2D(tex_sampler_1, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + vec2(stepX, -stepY)).r * 0.224;
          noise += texture2D(tex_sampler_1, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + vec2(stepX, stepY)).r * 0.224;
          noise += 0.4448;
          noise *= scale;
          vec4 color = texture2D(tex_sampler_0, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
          float energy = 0.33333 * color.r + 0.33333 * color.g + 0.33333 * color.b;
          float mask = (1.0 - sqrt(energy));
          float weight = 1.0 - 1.333 * mask * noise;
          gl_FragColor = vec4(color.rgb * weight, color.a);
          gl_FragColor = gl_FragColor+vec4(rand($DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + seed), rand($DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + seed),rand($DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + seed),1);
        }
        """.trimIndent()
}
