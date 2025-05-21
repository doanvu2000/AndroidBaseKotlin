package com.base.cameraview.video.encoding

import android.media.MediaCodec
import java.nio.ByteBuffer

/**
 * A Wrapper to MediaCodec that facilitates the use of API-dependent get{Input/Output}Buffer
 * methods, in order to prevent: [...](http://stackoverflow.com/q/30646885)
 */
internal class MediaCodecBuffers(private val mMediaCodec: MediaCodec) {
    private val mInputBuffers: Array<ByteBuffer?>?
    private val mOutputBuffers: Array<ByteBuffer?>? = null

    init {
        mInputBuffers = mOutputBuffers
    }

    fun getInputBuffer(index: Int): ByteBuffer? {
        return mMediaCodec.getInputBuffer(index)
    }

    fun getOutputBuffer(index: Int): ByteBuffer? {
        return mMediaCodec.getOutputBuffer(index)
    }

    fun onOutputBuffersChanged() {
    }
}
