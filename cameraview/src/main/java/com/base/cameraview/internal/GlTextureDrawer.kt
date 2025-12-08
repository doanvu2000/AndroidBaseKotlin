package com.base.cameraview.internal

import android.opengl.GLES11Ext
import android.opengl.GLES20
import com.base.cameraview.CameraLogger.Companion.create
import com.base.cameraview.filter.Filter
import com.base.cameraview.filter.NoFilter
import com.otaliastudios.opengl.core.Egloo.IDENTITY_MATRIX
import com.otaliastudios.opengl.core.Egloo.checkGlError
import com.otaliastudios.opengl.program.GlProgram
import com.otaliastudios.opengl.texture.GlTexture


class GlTextureDrawer(val texture: GlTexture) {
    var textureTransform: FloatArray = IDENTITY_MATRIX.clone()

    private var mFilter: Filter = NoFilter()
    private var mPendingFilter: Filter? = null
    private var mProgramHandle = -1

    @Suppress("unused")
    constructor() : this(GlTexture(TEXTURE_UNIT, TEXTURE_TARGET))

    @Suppress("unused")
    constructor(textureId: Int) : this(GlTexture(TEXTURE_UNIT, TEXTURE_TARGET, textureId))

    fun getFilter(): Filter {
        return mFilter
    }

    fun setFilter(filter: Filter) {
        mPendingFilter = filter
    }

    fun draw(timestampUs: Long) {
        mPendingFilter?.let {
            release()
            mFilter = mPendingFilter!!
            mPendingFilter = null
        }

        if (mProgramHandle == -1) {
            mProgramHandle = GlProgram.create(
                mFilter.vertexShader, mFilter.fragmentShader
            )
            mFilter.onCreate(mProgramHandle)
            checkGlError("program creation")
        }

        GLES20.glUseProgram(mProgramHandle)
        checkGlError("glUseProgram(handle)")
        texture.bind()
        mFilter.draw(timestampUs, this.textureTransform)
        texture.unbind()
        GLES20.glUseProgram(0)
        checkGlError("glUseProgram(0)")
    }

    fun release() {
        if (mProgramHandle == -1) return
        mFilter.onDestroy()
        GLES20.glDeleteProgram(mProgramHandle)
        mProgramHandle = -1
    }

    companion object {
        private val TAG: String = GlTextureDrawer::class.java.simpleName
        private val LOG = create(TAG)

        private const val TEXTURE_TARGET = GLES11Ext.GL_TEXTURE_EXTERNAL_OES
        private const val TEXTURE_UNIT = GLES20.GL_TEXTURE0
    }
}
