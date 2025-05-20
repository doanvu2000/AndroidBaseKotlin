package com.base.cameraview.filters

import android.opengl.GLES20
import com.base.cameraview.filter.BaseFilter
import com.base.cameraview.filter.TwoParameterFilter
import com.otaliastudios.opengl.core.Egloo.checkGlError
import com.otaliastudios.opengl.core.Egloo.checkGlProgramLocation
import kotlin.math.sqrt

/**
 * Applies a vignette effect to input frames.
 */
class VignetteFilter : BaseFilter(), TwoParameterFilter {
    private var mScale = 0.85f // 0...1
    private var mShade = 0.5f // 0...1
    private var mWidth = 1
    private var mHeight = 1

    private var mRangeLocation = -1
    private var mMaxDistLocation = -1
    private var mShadeLocation = -1
    private var mScaleLocation = -1

    override fun setSize(width: Int, height: Int) {
        super.setSize(width, height)
        mWidth = width
        mHeight = height
    }

    var vignetteScale: Float
        /**
         * Gets the current vignette scale.
         *
         * @return scale
         * @see .setVignetteScale
         */
        get() = mScale
        /**
         * Sets the vignette effect scale (0.0 - 1.0).
         *
         * @param scale new scale
         */
        set(scale) {
            var scale = scale
            if (scale < 0.0f) scale = 0.0f
            if (scale > 1.0f) scale = 1.0f
            mScale = scale
        }

    var vignetteShade: Float
        /**
         * Gets the current vignette shade.
         *
         * @return shade
         * @see .setVignetteShade
         */
        get() = mShade
        /**
         * Sets the vignette effect shade (0.0 - 1.0).
         *
         * @param shade new shade
         */
        set(shade) {
            var shade = shade
            if (shade < 0.0f) shade = 0.0f
            if (shade > 1.0f) shade = 1.0f
            this.mShade = shade
        }

    override var parameter1: Float
        get() = this.vignetteScale
        set(value) {
            this.vignetteScale = value
        }

    override var parameter2: Float
        get() = this.vignetteShade
        set(value) {
            this.vignetteShade = value
        }

    override fun onCreate(programHandle: Int) {
        super.onCreate(programHandle)
        mRangeLocation = GLES20.glGetUniformLocation(programHandle, "range")
        checkGlProgramLocation(mRangeLocation, "range")
        mMaxDistLocation = GLES20.glGetUniformLocation(programHandle, "inv_max_dist")
        checkGlProgramLocation(mMaxDistLocation, "inv_max_dist")
        mShadeLocation = GLES20.glGetUniformLocation(programHandle, "shade")
        checkGlProgramLocation(mShadeLocation, "shade")
        mScaleLocation = GLES20.glGetUniformLocation(programHandle, "scale")
        checkGlProgramLocation(mScaleLocation, "scale")
    }

    override fun onDestroy() {
        super.onDestroy()
        mRangeLocation = -1
        mMaxDistLocation = -1
        mShadeLocation = -1
        mScaleLocation = -1
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
        GLES20.glUniform1f(mMaxDistLocation, 1f / maxDist)
        checkGlError("glUniform1f")

        GLES20.glUniform1f(mShadeLocation, mShade)
        checkGlError("glUniform1f")

        // The 'range' is between 1.3 to 0.6. When scale is zero then range is 1.3
        // which means no vignette at all because the luminousity difference is
        // less than 1/256 and will cause nothing.
        val range = (1.30f - sqrt(mScale.toDouble()).toFloat() * 0.7f)
        GLES20.glUniform1f(mRangeLocation, range)
        checkGlError("glUniform1f")
    }

    override val fragmentShader: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        uniform samplerExternalOES sTexture;
        uniform float range;
        uniform float inv_max_dist;
        uniform float shade;
        uniform vec2 scale;
        varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
        void main() {
          const float slope = 20.0;
          vec2 coord = $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME - vec2(0.5, 0.5);
          float dist = length(coord * scale);
          float lumen = shade / (1.0 + exp((dist * inv_max_dist - range) * slope)) + (1.0 - shade);
          vec4 color = texture2D(sTexture, $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME);
          gl_FragColor = vec4(color.rgb * lumen, color.a);
        }
        """.trimIndent()
}