package com.base.cameraview.filters

import android.opengl.GLES20
import com.base.cameraview.filter.BaseFilter
import com.base.cameraview.filter.TwoParameterFilter
import com.otaliastudios.opengl.core.Egloo.checkGlError
import com.otaliastudios.opengl.core.Egloo.checkGlProgramLocation

/**
 * Applies a whirlpool distortion effect to frames.
 */
class WhirlPoolFilter : BaseFilter(), TwoParameterFilter {
    private var strength = 5.0f // 0.0 to 10.0
    private var radius = 300.0f // pixels
    private var width = 1
    private var height = 1

    private var strengthLocation = -1
    private var radiusLocation = -1
    private var resolutionLocation = -1

    override fun setSize(width: Int, height: Int) {
        super.setSize(width, height)
        this.width = width
        this.height = height
    }

    /**
     * Gets the current whirlpool strength.
     *
     * @return strength (0.0 - 10.0)
     * @see .setStrength
     */
    fun getStrength(): Float {
        return strength
    }

    /**
     * Sets the whirlpool effect strength (0.0 - 10.0).
     * 0.0: no effect
     * 5.0: moderate whirlpool
     * 10.0: maximum whirlpool
     *
     * @param strength new strength
     */
    fun setStrength(strength: Float) {
        var strength = strength
        if (strength < 0.0f) strength = 0.0f
        if (strength > 10.0f) strength = 10.0f
        this.strength = strength
    }

    /**
     * Gets the current whirlpool radius.
     *
     * @return radius in pixels
     * @see .setRadius
     */
    fun getRadius(): Float {
        return radius
    }

    /**
     * Sets the whirlpool effect radius in pixels (50.0 - 1000.0).
     *
     * @param radius new radius
     */
    fun setRadius(radius: Float) {
        var radius = radius
        if (radius < 50.0f) radius = 50.0f
        if (radius > 1000.0f) radius = 1000.0f
        this.radius = radius
    }

    override var parameter1: Float
        get() = getStrength() / 10f
        set(value) {
            setStrength(value * 10f)
        }

    override var parameter2: Float
        get() = (getRadius() - 50f) / 950f
        set(value) {
            setRadius(50f + value * 950f)
        }

    override fun onCreate(programHandle: Int) {
        super.onCreate(programHandle)
        strengthLocation = GLES20.glGetUniformLocation(programHandle, "u_Strength")
        checkGlProgramLocation(strengthLocation, "u_Strength")
        radiusLocation = GLES20.glGetUniformLocation(programHandle, "u_Radius")
        checkGlProgramLocation(radiusLocation, "u_Radius")
        resolutionLocation = GLES20.glGetUniformLocation(programHandle, "u_Resolution")
        checkGlProgramLocation(resolutionLocation, "u_Resolution")
    }

    override fun onDestroy() {
        super.onDestroy()
        strengthLocation = -1
        radiusLocation = -1
        resolutionLocation = -1
    }

    override fun onPreDraw(timestampUs: Long, transformMatrix: FloatArray) {
        super.onPreDraw(timestampUs, transformMatrix)
        GLES20.glUniform1f(strengthLocation, strength)
        checkGlError("glUniform1f")
        GLES20.glUniform1f(radiusLocation, radius)
        checkGlError("glUniform1f")
        GLES20.glUniform2f(resolutionLocation, width.toFloat(), height.toFloat())
        checkGlError("glUniform2f")
    }

    override val fragmentShader: String = """
        #extension GL_OES_EGL_image_external : require
        precision mediump float;
        
        uniform samplerExternalOES sTexture;
        uniform vec2 u_Resolution;
        uniform float u_Strength;
        uniform float u_Radius;
        
        varying vec2 $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
        
        void main() {
            vec2 uv = $DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME;
            vec2 center = vec2(0.5, 0.5);
            vec2 delta = uv - center;
            float dist = length(delta);
        
            float radius = u_Radius / min(u_Resolution.x, u_Resolution.y);
        
            if (dist > radius) {
                gl_FragColor = texture2D(sTexture, uv);
                return;
            }
        
            float t = (radius - dist) / radius;
            float angle = u_Strength * t * t;
        
            float s = sin(angle);
            float c = cos(angle);
        
            vec2 rotated = vec2(
                delta.x * c - delta.y * s,
                delta.x * s + delta.y * c
            );
        
            vec2 whirlUV = center + rotated;
            gl_FragColor = texture2D(sTexture, whirlUV);
        }
        """.trimIndent()
}