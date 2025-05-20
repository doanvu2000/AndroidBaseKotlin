package com.base.cameraview.filters

import android.opengl.GLES20
import com.base.cameraview.filter.BaseFilter
import com.otaliastudios.opengl.core.Egloo.checkGlError
import com.otaliastudios.opengl.core.Egloo.checkGlProgramLocation
import java.util.Random
import kotlin.math.sqrt

/**
 * Applies a lomo-camera style effect to the input frames.
 */
class LomoishFilter : BaseFilter() {
    private var width = 1
    private var height = 1

    private var scaleLocation = -1
    private var maxDistLocation = -1
    private var stepSizeXLocation = -1
    private var stepSizeYLocation = -1

    public override fun setSize(width: Int, height: Int) {
        super.setSize(width, height)
        this.width = width
        this.height = height
    }

    public override fun onCreate(programHandle: Int) {
        super.onCreate(programHandle)
        scaleLocation = GLES20.glGetUniformLocation(programHandle, "scale")
        checkGlProgramLocation(scaleLocation, "scale")
        maxDistLocation = GLES20.glGetUniformLocation(programHandle, "inv_max_dist")
        checkGlProgramLocation(maxDistLocation, "inv_max_dist")
        stepSizeXLocation = GLES20.glGetUniformLocation(programHandle, "stepsizeX")
        checkGlProgramLocation(stepSizeXLocation, "stepsizeX")
        stepSizeYLocation = GLES20.glGetUniformLocation(programHandle, "stepsizeY")
        checkGlProgramLocation(stepSizeYLocation, "stepsizeY")
    }

    public override fun onDestroy() {
        super.onDestroy()
        scaleLocation = -1
        maxDistLocation = -1
        stepSizeXLocation = -1
        stepSizeYLocation = -1
    }

    override fun onPreDraw(timestampUs: Long, transformMatrix: FloatArray) {
        super.onPreDraw(timestampUs, transformMatrix)
        val scale = FloatArray(2)
        if (width > height) {
            scale[0] = 1f
            scale[1] = (height.toFloat()) / width
        } else {
            scale[0] = (width.toFloat()) / height
            scale[1] = 1f
        }
        val maxDist =
            (sqrt((scale[0] * scale[0] + scale[1] * scale[1]).toDouble()).toFloat()) * 0.5f
        GLES20.glUniform2fv(scaleLocation, 1, scale, 0)
        checkGlError("glUniform2fv")
        GLES20.glUniform1f(maxDistLocation, 1.0f / maxDist)
        checkGlError("glUniform1f")
        GLES20.glUniform1f(stepSizeXLocation, 1.0f / width)
        checkGlError("glUniform1f")
        GLES20.glUniform1f(stepSizeYLocation, 1.0f / height)
        checkGlError("glUniform1f")
    }

    private val RANDOM = Random()
    override val fragmentShader: String = ("#extension GL_OES_EGL_image_external : require\n"
            + "precision mediump float;\n"
            + "uniform samplerExternalOES sTexture;\n"
            + "uniform float stepsizeX;\n"
            + "uniform float stepsizeY;\n"
            + "uniform vec2 scale;\n"
            + "uniform float inv_max_dist;\n"
            + "vec2 seed;\n"
            + "float stepsize;\n"
            + "varying vec2 " + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + ";\n"
            + "float rand(vec2 loc) {\n"
            + "  float theta1 = dot(loc, vec2(0.9898, 0.233));\n"
            + "  float theta2 = dot(loc, vec2(12.0, 78.0));\n"
            + "  float value = cos(theta1) * sin(theta2) + sin(theta1) * cos(theta2);\n" // keep value of part1 in range: (2^-14 to 2^14).
            + "  float temp = mod(197.0 * value, 1.0) + value;\n"
            + "  float part1 = mod(220.0 * temp, 1.0) + temp;\n"
            + "  float part2 = value * 0.5453;\n"
            + "  float part3 = cos(theta1 + theta2) * 0.43758;\n"
            + "  return fract(part1 + part2 + part3);\n"
            + "}\n"
            + "void main() {\n"
            + "  seed[0] = " + RANDOM.nextFloat() + ";\n"
            + "  seed[1] = " + RANDOM.nextFloat() + ";\n"
            + "  stepsize = " + 1.0f / 255.0f + ";\n" // sharpen
            + "  vec3 nbr_color = vec3(0.0, 0.0, 0.0);\n"
            + "  vec2 coord;\n"
            + "  vec4 color = texture2D(sTexture, " + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + ");\n"
            + "  coord.x = " + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + ".x - 0.5 * stepsizeX;\n"
            + "  coord.y = " + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + ".y - stepsizeY;\n"
            + "  nbr_color += texture2D(sTexture, coord).rgb - color.rgb;\n"
            + "  coord.x = " + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + ".x - stepsizeX;\n"
            + "  coord.y = " + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + ".y + 0.5 * stepsizeY;\n"
            + "  nbr_color += texture2D(sTexture, coord).rgb - color.rgb;\n"
            + "  coord.x = " + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + ".x + stepsizeX;\n"
            + "  coord.y = " + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + ".y - 0.5 * stepsizeY;\n"
            + "  nbr_color += texture2D(sTexture, coord).rgb - color.rgb;\n"
            + "  coord.x = " + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + ".x + stepsizeX;\n"
            + "  coord.y = " + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + ".y + 0.5 * stepsizeY;\n"
            + "  nbr_color += texture2D(sTexture, coord).rgb - color.rgb;\n"
            + "  vec3 s_color = vec3(color.rgb + 0.3 * nbr_color);\n" // cross process
            + "  vec3 c_color = vec3(0.0, 0.0, 0.0);\n"
            + "  float value;\n"
            + "  if (s_color.r < 0.5) {\n"
            + "    value = s_color.r;\n"
            + "  } else {\n"
            + "    value = 1.0 - s_color.r;\n"
            + "  }\n"
            + "  float red = 4.0 * value * value * value;\n"
            + "  if (s_color.r < 0.5) {\n"
            + "    c_color.r = red;\n"
            + "  } else {\n"
            + "    c_color.r = 1.0 - red;\n"
            + "  }\n"
            + "  if (s_color.g < 0.5) {\n"
            + "    value = s_color.g;\n"
            + "  } else {\n"
            + "    value = 1.0 - s_color.g;\n"
            + "  }\n"
            + "  float green = 2.0 * value * value;\n"
            + "  if (s_color.g < 0.5) {\n"
            + "    c_color.g = green;\n"
            + "  } else {\n"
            + "    c_color.g = 1.0 - green;\n"
            + "  }\n"
            + "  c_color.b = s_color.b * 0.5 + 0.25;\n" // blackwhite
            + "  float dither = rand(" + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + " + seed);\n"
            + "  vec3 xform = clamp((c_color.rgb - 0.15) * 1.53846, 0.0, 1.0);\n"
            + "  vec3 temp = clamp((color.rgb + stepsize - 0.15) * 1.53846, 0.0, 1.0);\n"
            + "  vec3 bw_color = clamp(xform + (temp - xform) * (dither - 0.5), 0.0, 1.0);\n" // vignette
            + "  coord = " + DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME + " - vec2(0.5, 0.5);\n"
            + "  float dist = length(coord * scale);\n"
            + "  float lumen = 0.85 / (1.0 + exp((dist * inv_max_dist - 0.73) * 20.0)) + 0.15;\n"
            + "  gl_FragColor = vec4(bw_color * lumen, color.a);\n"
            + "}\n")
}
