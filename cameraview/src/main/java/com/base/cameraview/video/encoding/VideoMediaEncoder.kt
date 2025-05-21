package com.base.cameraview.video.encoding

import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Bundle
import android.view.Surface
import com.base.cameraview.CameraLogger.Companion.create

/**
 * Base class for video encoding.
 *
 *
 * This uses [MediaCodec.createInputSurface] to create an input [Surface]
 * into which we can write and that MediaCodec itself can read.
 *
 *
 * This makes everything easier with respect to the process explained in [MediaEncoder]
 * docs. We can skip the whole input part of acquiring an InputBuffer, filling it with data
 * and returning it to the encoder with [.encodeInputBuffer].
 *
 *
 * All of this is automatically done by MediaCodec as long as we keep writing data into the
 * given [Surface]. This class alone does not do this - subclasses are required to do so.
 *
 * @param <C> the config object.
</C> */
internal abstract class VideoMediaEncoder<C : VideoConfig?>(config: C) :
    MediaEncoder("VideoEncoder") {
    @JvmField
    protected var mConfig: C = config

    @JvmField
    protected var mSurface: Surface? = null

    @JvmField
    protected var mFrameNumber: Int = -1

    private var mSyncFrameFound = false

    @EncoderThread
    override fun onPrepare(controller: MediaEncoderEngine.Controller, maxLengthUs: Long) {
        try {
            val format = MediaFormat.createVideoFormat(
                mConfig!!.mimeType!!,
                mConfig!!.width,
                mConfig!!.height
            )
            format.setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            format.setInteger(MediaFormat.KEY_BIT_RATE, mConfig!!.bitRate)
            format.setInteger(MediaFormat.KEY_FRAME_RATE, mConfig!!.frameRate)
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1) // seconds between key frames!
            format.setInteger("rotation-degrees", mConfig!!.rotation)
            mMediaCodec = if (mConfig!!.encoder != null) {
                MediaCodec.createByCodecName(mConfig!!.encoder!!)
            } else {
                MediaCodec.createEncoderByType(mConfig!!.mimeType!!)
            }
            mMediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
            mSurface = mMediaCodec.createInputSurface()
            mMediaCodec.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @EncoderThread
    override fun onStart() {
        // Nothing to do here. Waiting for the first frame.
        mFrameNumber = 0
    }

    @EncoderThread
    override fun onStop() {
        LOG.i("onStop", "setting mFrameNumber to 1 and signaling the end of input stream.")
        mFrameNumber = -1
        // Signals the end of input stream. This is a Video only API, as in the normal case,
        // we use input buffers to signal the end. In the video case, we don't have input buffers
        // because we use an input surface instead.
        mMediaCodec.signalEndOfInputStream()
        drainOutput(true)
    }

    /**
     * The first frame that we write MUST have the BUFFER_FLAG_SYNC_FRAME flag set.
     * It sometimes doesn't because we might drop some frames in [.drainOutput],
     * basically if, at the time, the muxer was not started yet, due to Audio setup being slow.
     *
     *
     * We can't add the BUFFER_FLAG_SYNC_FRAME flag to the first frame just because we'd like to.
     * But we can drop frames until we get a sync one.
     *
     * @param pool   the buffer pool
     * @param buffer the buffer
     */
    override fun onWriteOutput(pool: OutputBufferPool, buffer: OutputBuffer) {
        if (!mSyncFrameFound) {
            LOG.w("onWriteOutput:", "sync frame not found yet. Checking.")
            val flag = MediaCodec.BUFFER_FLAG_KEY_FRAME
            val hasFlag = (buffer.info!!.flags and flag) == flag
            if (hasFlag) {
                LOG.w("onWriteOutput:", "SYNC FRAME FOUND!")
                mSyncFrameFound = true
                super.onWriteOutput(pool, buffer)
            } else {
                LOG.w("onWriteOutput:", "DROPPING FRAME and requesting a sync frame soon.")
                val params = Bundle()
                params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0)
                mMediaCodec.setParameters(params)
                pool.recycle(buffer)
            }
        } else {
            super.onWriteOutput(pool, buffer)
        }
    }

    protected override fun getEncodedBitRate(): Int {
        return mConfig!!.bitRate
    }

    protected open fun shouldRenderFrame(timestampUs: Long): Boolean {
        if (timestampUs == 0L) return false // grafika said so

        if (mFrameNumber < 0) return false // We were asked to stop.

        if (hasReachedMaxLength()) return false // We were not asked yet, but we'll be soon.

        mFrameNumber++
        return true
    }

    companion object {
        private val TAG: String = VideoMediaEncoder::class.java.simpleName
        private val LOG = create(TAG)
    }
}
