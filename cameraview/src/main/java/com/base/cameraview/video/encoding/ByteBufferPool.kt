package com.base.cameraview.video.encoding

import com.base.cameraview.internal.Pool
import java.nio.ByteBuffer

/**
 * A simple [] implementation for byte buffers.
 */
internal class ByteBufferPool(bufferSize: Int, maxPoolSize: Int) : Pool<ByteBuffer?>(
    maxPoolSize, Factory {
        ByteBuffer.allocateDirect(bufferSize)
    })
