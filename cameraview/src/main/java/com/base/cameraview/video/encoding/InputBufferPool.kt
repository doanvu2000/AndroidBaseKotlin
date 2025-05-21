package com.base.cameraview.video.encoding

import com.base.cameraview.internal.Pool

/**
 * A simple [] implementation for input buffers.
 */
internal class InputBufferPool :
    Pool<InputBuffer?>(Int.Companion.MAX_VALUE, Factory { InputBuffer() })
