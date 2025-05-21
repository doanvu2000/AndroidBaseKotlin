package com.base.cameraview.video.encoding;

import android.media.MediaCodec;

import java.nio.ByteBuffer;

/**
 * A Wrapper to MediaCodec that facilitates the use of API-dependent get{Input/Output}Buffer
 * methods, in order to prevent: http://stackoverflow.com/q/30646885
 */
class MediaCodecBuffers {

    private final MediaCodec mMediaCodec;
    private final ByteBuffer[] mInputBuffers;
    private ByteBuffer[] mOutputBuffers;

    MediaCodecBuffers(MediaCodec mediaCodec) {
        mMediaCodec = mediaCodec;

        mInputBuffers = mOutputBuffers = null;
    }

    ByteBuffer getInputBuffer(final int index) {
        return mMediaCodec.getInputBuffer(index);
    }

    ByteBuffer getOutputBuffer(final int index) {
        return mMediaCodec.getOutputBuffer(index);
    }

    void onOutputBuffersChanged() {
    }
}
