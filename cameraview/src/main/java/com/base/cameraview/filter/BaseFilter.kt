package com.base.cameraview.filter

import androidx.annotation.VisibleForTesting
import com.base.cameraview.CameraLogger
import com.base.cameraview.size.Size
import com.otaliastudios.opengl.draw.GlDrawable
import com.otaliastudios.opengl.draw.GlRect
import com.otaliastudios.opengl.program.GlTextureProgram

/**
 * A base implementation of [Filter] that just leaves the fragment shader to subclasses.
 * See [NoFilter] for a non-abstract implementation.
 *
 *
 * This class offers a default vertex shader implementation which in most cases is not required
 * to be changed. Most effects can be rendered by simply changing the fragment shader, thus
 * by overriding [.getFragmentShader].
 *
 *
 * All [BaseFilter]s should have a no-arguments public constructor.
 * This class will try to automatically implement [.copy] thanks to this.
 * NOTE - This class expects variable to have a certain name:
 * - [.vertexPositionName]
 * - [.vertexTransformMatrixName]
 * - [.vertexModelViewProjectionMatrixName]
 * - [.vertexTextureCoordinateName]
 * - [.fragmentTextureCoordinateName]
 * You can either change these variables, for example in your constructor, or change your
 * vertex and fragment shader code to use them.
 *
 *
 * NOTE - the [android.graphics.SurfaceTexture] restrictions apply:
 * We only support the [android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES] texture target
 * and it must be specified in the fragment shader as a samplerExternalOES texture.
 * You also have to explicitly require the extension: see
 * [.createDefaultFragmentShader].
 */
abstract class BaseFilter : Filter {
    protected var vertexPositionName: String = DEFAULT_VERTEX_POSITION_NAME
    protected var vertexTextureCoordinateName: String = DEFAULT_VERTEX_TEXTURE_COORDINATE_NAME
    protected var vertexModelViewProjectionMatrixName: String = DEFAULT_VERTEX_MVP_MATRIX_NAME
    protected var vertexTransformMatrixName: String = DEFAULT_VERTEX_TRANSFORM_MATRIX_NAME

    @Suppress("unused")
    protected var fragmentTextureCoordinateName: String = DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME

    @VisibleForTesting
    var program: GlTextureProgram? = null

    @VisibleForTesting
    var size: Size? = null
    private var programDrawable: GlDrawable? = null

    protected fun createDefaultVertexShader(): String {
        return createDefaultVertexShader(
            vertexPositionName,
            vertexTextureCoordinateName,
            vertexModelViewProjectionMatrixName,
            vertexTransformMatrixName,
            fragmentTextureCoordinateName
        )
    }

    protected fun createDefaultFragmentShader(): String {
        return createDefaultFragmentShader(fragmentTextureCoordinateName)
    }

    override fun onCreate(programHandle: Int) {
        program = GlTextureProgram(
            programHandle,
            vertexPositionName,
            vertexModelViewProjectionMatrixName,
            vertexTextureCoordinateName,
            vertexTransformMatrixName
        )
        programDrawable = GlRect()
    }

    override fun onDestroy() {
        // Since we used the handle constructor of GlTextureProgram, calling release here
        // will NOT destroy the GL program. This is important because Filters are not supposed
        // to have ownership of programs. Creation and deletion happen outside, and deleting twice
        // would cause an error.
        program!!.release()
        program = null
        programDrawable = null
    }

    override val vertexShader: String
        get() = createDefaultVertexShader()

    override fun setSize(width: Int, height: Int) {
        size = Size(width, height)
    }

    override fun draw(timestampUs: Long, transformMatrix: FloatArray) {
        if (program == null) {
            LOG.w(
                "Filter.draw() called after destroying the filter. " + "This can happen rarely because of threading."
            )
        } else {
            onPreDraw(timestampUs, transformMatrix)
            onDraw(timestampUs)
            onPostDraw(timestampUs)
        }
    }

    protected open fun onPreDraw(timestampUs: Long, transformMatrix: FloatArray) {
        program!!.textureTransform = transformMatrix
        program!!.onPreDraw(programDrawable!!, programDrawable!!.modelMatrix)
    }

    protected fun onDraw(@Suppress("unused") timestampUs: Long) {
        try {
            program!!.onDraw(programDrawable!!)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    protected fun onPostDraw(@Suppress("unused") timestampUs: Long) {
        program!!.onPostDraw(programDrawable!!)
    }

    override fun copy(): BaseFilter {
        val copy = onCopy()
        if (size != null) {
            copy.setSize(size!!.width, size!!.height)
        }
        if (this is OneParameterFilter) {
            (copy as OneParameterFilter).parameter1 = (this as OneParameterFilter).parameter1
        }
        if (this is TwoParameterFilter) {
            (copy as TwoParameterFilter).parameter2 = (this as TwoParameterFilter).parameter2
        }
        return copy
    }

    protected open fun onCopy(): BaseFilter {
        try {
            return javaClass.newInstance()
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Filters should have a public no-arguments constructor.", e)
        } catch (e: InstantiationException) {
            throw RuntimeException("Filters should have a public no-arguments constructor.", e)
        }
    }

    companion object {
        const val DEFAULT_VERTEX_POSITION_NAME: String = "aPosition"
        const val DEFAULT_VERTEX_TEXTURE_COORDINATE_NAME: String = "aTextureCoord"
        const val DEFAULT_VERTEX_MVP_MATRIX_NAME: String = "uMVPMatrix"
        const val DEFAULT_VERTEX_TRANSFORM_MATRIX_NAME: String = "uTexMatrix"
        const val DEFAULT_FRAGMENT_TEXTURE_COORDINATE_NAME: String = "vTextureCoord"
        private val TAG: String = BaseFilter::class.java.simpleName
        private val LOG: CameraLogger = CameraLogger.create(TAG)
        private fun createDefaultVertexShader(
            vertexPositionName: String,
            vertexTextureCoordinateName: String,
            vertexModelViewProjectionMatrixName: String,
            vertexTransformMatrixName: String,
            fragmentTextureCoordinateName: String
        ): String {
            return ("uniform mat4 $vertexModelViewProjectionMatrixName;\nuniform mat4 $vertexTransformMatrixName;\nattribute vec4 $vertexPositionName;\nattribute vec4 $vertexTextureCoordinateName;\nvarying vec2 $fragmentTextureCoordinateName;\nvoid main() {\n    gl_Position = $vertexModelViewProjectionMatrixName * $vertexPositionName;\n    $fragmentTextureCoordinateName = ($vertexTransformMatrixName * $vertexTextureCoordinateName).xy;\n}\n")
        }

        private fun createDefaultFragmentShader(
            fragmentTextureCoordinateName: String
        ): String {
            return ("#extension GL_OES_EGL_image_external : require\nprecision mediump float;\nvarying vec2 $fragmentTextureCoordinateName;\nuniform samplerExternalOES sTexture;\nvoid main() {\n  gl_FragColor = texture2D(sTexture, $fragmentTextureCoordinateName);\n}\n")
        }
    }
}
