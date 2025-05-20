package com.base.cameraview.filters

import android.opengl.GLES20
import com.base.cameraview.filter.BaseFilter
import com.otaliastudios.opengl.core.Egloo.checkGlError
import com.otaliastudios.opengl.core.Egloo.checkGlProgramLocation
import java.util.Random
import kotlin.math.sqrt

/**
 * Applies black and white documentary style effect.
 */
class DocumentaryFilter : BaseFilter() {
    private var mWidth = 1
    private var mHeight = 1
    private var mScaleLocation = -1
    private var mMaxDistLocation = -1

    public override fun setSize(width: Int, height: Int) {
        super.setSize(width, height)
        mWidth = width
        mHeight = height
    }

    public override fun onCreate(programHandle: Int) {
        super.onCreate(programHandle)
        mScaleLocation = GLES20.glGetUniformLocation(programHandle, "scale")
        checkGlProgramLocation(mScaleLocation, "scale")
        mMaxDistLocation = GLES20.glGetUniformLocation(programHandle, "inv_max_dist")
        checkGlProgramLocation(mMaxDistLocation, "inv_max_dist")
    }

    public override fun onDestroy() {
        super.onDestroy()
        mScaleLocation = -1
        mMaxDistLocation = -1
    }

    override fun onPreDraw(timestampUs: Long, transformMatrix: FloatArray) {
        super.onPreDraw(timestampUs, transformMatrix)
        val scale = FloatArray(2)
        if (mWidth > mHeight) {
            scale[0] = 1f
            scale[1] = (mHeight.toFloat()) / mWidth
        } else {
            scale[0] = (mWidth.toFloat()) / mHeight
            scale[1] = 1f
        }
        GLES20.glUniform2fv(mScaleLocation, 1, scale, 0)
        checkGlError("glUniform2fv")

        val maxDist =
            (sqrt((scale[0] * scale[0] + scale[1] * scale[1]).toDouble()).toFloat()) * 0.5f
        val invMaxDist = 1f / maxDist
        GLES20.glUniform1f(mMaxDistLocation, invMaxDist)
        checkGlError("glUniform1f")
    }

    private val RANDOM = Random()
    override val fragmentShader: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        uniform samplerExternalOES sTexture;
        vec2 seed;
        float stepsize;
        uniform float inv_max_dist;
        uniform vec2 scale;
        varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
        float rand(vec2 loc) {
          float theta1 = dot(loc, vec2(0.9898, 0.233));
          float theta2 = dot(loc, vec2(12.0, 78.0));
          float value = cos(theta1) * sin(theta2) + sin(theta1) * cos(theta2);
          // keep value of part1 in range: (2^-14 to 2^14).
          float temp = mod(197.0 * value, 1.0) + value;
          float part1 = mod(220.0 * temp, 1.0) + temp;
          float part2 = value * 0.5453;
          float part3 = cos(theta1 + theta2) * 0.43758;
          return fract(part1 + part2 + part3);
        }
        void main() {
          seed[0] = ${RANDOM.nextFloat()};
          seed[1] = ${RANDOM.nextFloat()};
          stepsize = ${1.0f / 255.0f}; // black white
          vec4 color = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
          float dither = rand($DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + seed);
          vec3 xform = clamp(2.0 * color.rgb, 0.0, 1.0);
          vec3 temp = clamp(2.0 * (color.rgb + stepsize), 0.0, 1.0);
          vec3 new_color = clamp(xform + (temp - xform) * (dither - 0.5), 0.0, 1.0); // grayscale
          float gray = dot(new_color, vec3(0.299, 0.587, 0.114));
          new_color = vec3(gray, gray, gray); // vignette
          vec2 coord = $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME - vec2(0.5, 0.5);
          float dist = length(coord * scale);
          float lumen = 0.85 / (1.0 + exp((dist * inv_max_dist - 0.83) * 20.0)) + 0.15;
          gl_FragColor = vec4(new_color * lumen, color.a);
        }
        """.trimIndent()
}
