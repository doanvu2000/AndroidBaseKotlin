package com.base.cameraview.video.encoding;

import android.media.MediaCodec;

import com.base.cameraview.internal.Pool;

/**
 * A simple {@link Pool(int, Factory)} implementation for output buffers.
 */
class OutputBufferPool extends Pool<OutputBuffer> {

    OutputBufferPool(final int trackIndex) {
        super(Integer.MAX_VALUE, new Factory<OutputBuffer>() {
            @Override
            public OutputBuffer create() {
                OutputBuffer buffer = new OutputBuffer();
                buffer.trackIndex = trackIndex;
                buffer.info = new MediaCodec.BufferInfo();
                return buffer;
            }
        });
    }
}
