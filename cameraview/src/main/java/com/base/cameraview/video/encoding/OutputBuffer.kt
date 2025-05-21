package com.base.cameraview.video.encoding

import android.media.MediaCodec
import java.nio.ByteBuffer

/**
 * Represents an output buffer, which means,
 * an encoded buffer of data that should be passed
 * to the muxer.
 */
class OutputBuffer {
    @JvmField
    var info: MediaCodec.BufferInfo? = null

    @JvmField
    var trackIndex: Int = 0

    @JvmField
    var data: ByteBuffer? = null
}
