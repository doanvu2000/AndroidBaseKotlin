package com.base.cameraview.video.encoding

import android.media.MediaCodec
import com.base.cameraview.internal.Pool

/**
 * A simple [] implementation for output buffers.
 */
internal class OutputBufferPool(trackIndex: Int) :
    Pool<OutputBuffer?>(Int.Companion.MAX_VALUE, Factory {
        val buffer = OutputBuffer()
        buffer.trackIndex = trackIndex
        buffer.info = MediaCodec.BufferInfo()
        buffer
    })
