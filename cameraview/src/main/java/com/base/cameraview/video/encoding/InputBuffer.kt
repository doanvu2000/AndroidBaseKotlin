package com.base.cameraview.video.encoding

import java.nio.ByteBuffer

/**
 * Represents an input buffer, which means,
 * raw data that should be encoded by MediaCodec.
 */
class InputBuffer {
    @JvmField
    var data: ByteBuffer? = null

    @JvmField
    var source: ByteBuffer? = null

    @JvmField
    var index: Int = 0

    @JvmField
    var length: Int = 0

    @JvmField
    var timestamp: Long = 0

    @JvmField
    var isEndOfStream: Boolean = false
}
