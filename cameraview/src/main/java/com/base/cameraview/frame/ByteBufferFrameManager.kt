package com.base.cameraview.frame

import com.base.cameraview.engine.offset.Angles
import com.base.cameraview.size.Size
import java.util.concurrent.LinkedBlockingQueue


class ByteBufferFrameManager(poolSize: Int, callback: BufferCallback?) :
    FrameManager<ByteArray?>(
        poolSize,
        ByteArray::class.java as Class<ByteArray?>
    ) {
    private val mBufferMode: Int
    private var mBufferQueue: LinkedBlockingQueue<ByteArray?>? = null
    private var mBufferCallback: BufferCallback? = null

    /**
     * Construct a new frame manager.
     *
     * @param poolSize the size of the backing pool.
     * @param callback a callback
     */
    init {
        if (callback != null) {
            mBufferCallback = callback
            mBufferMode = BUFFER_MODE_DISPATCH
        } else {
            mBufferQueue = LinkedBlockingQueue<ByteArray?>(poolSize)
            mBufferMode = BUFFER_MODE_ENQUEUE
        }
    }

    override fun setUp(format: Int, size: Size, angles: Angles) {
        super.setUp(format, size, angles)
        val bytes = frameBytes
        for (i in 0..<poolSize) {
            if (mBufferMode == BUFFER_MODE_DISPATCH) {
                mBufferCallback!!.onBufferAvailable(ByteArray(bytes))
            } else {
                mBufferQueue!!.offer(ByteArray(bytes))
            }
        }
    }

    val buffer: ByteArray?
        /**
         * Returns a new byte buffer than can be filled.
         * This can only be called in [.BUFFER_MODE_ENQUEUE] mode! Where the frame
         * manager also holds a queue of the byte buffers.
         *
         *
         * If not null, the buffer returned by this method can be filled and used to get
         * a new frame through [FrameManager.getFrame].
         *
         * @return a buffer, or null
         */
        get() {
            check(mBufferMode == BUFFER_MODE_ENQUEUE) {
                "Can't call getBuffer() " +
                        "when not in BUFFER_MODE_ENQUEUE."
            }
            return mBufferQueue!!.poll()
        }

    /**
     * Can be called if the buffer obtained by [.getBuffer]
     * was not used to construct a frame, so it can be put back into the queue.
     *
     * @param buffer a buffer
     */
    fun onBufferUnused(buffer: ByteArray) {
        check(mBufferMode == BUFFER_MODE_ENQUEUE) {
            "Can't call onBufferUnused() " +
                    "when not in BUFFER_MODE_ENQUEUE."
        }

        if (isSetUp) {
            mBufferQueue!!.offer(buffer)
        } else {
            LOG.w("onBufferUnused: buffer was returned but we're not set up anymore.")
        }
    }

    override fun onFrameDataReleased(data: ByteArray?, recycled: Boolean) {
        if (recycled && data?.size == frameBytes) {
            if (mBufferMode == BUFFER_MODE_DISPATCH) {
                mBufferCallback!!.onBufferAvailable(data)
            } else {
                mBufferQueue!!.offer(data)
            }
        }
    }

    override fun onCloneFrameData(data: ByteArray?): ByteArray? {
        val clone = ByteArray(data?.size ?: 0)
        if (data != null) {
            System.arraycopy(data, 0, clone, 0, data.size)
        }
        return clone
    }

    /**
     * Releases all frames controlled by this manager and
     * clears the pool.
     * In BUFFER_MODE_ENQUEUE, releases also all the buffers.
     */
    override fun release() {
        super.release()
        if (mBufferMode == BUFFER_MODE_ENQUEUE) {
            mBufferQueue!!.clear()
        }
    }

    /**
     * Receives callbacks on buffer availability
     * (when a Frame is released, we reuse its buffer).
     */
    interface BufferCallback {
        fun onBufferAvailable(buffer: ByteArray)
    }

    companion object {
        /**
         * In this mode, we have a [.mBufferCallback] and dispatch
         * new buffers to the callback.
         */
        private const val BUFFER_MODE_DISPATCH = 0

        /**
         * In this mode, we have a [.mBufferQueue] where we store
         * buffers and only dispatch when requested.
         */
        private const val BUFFER_MODE_ENQUEUE = 1
    }
}
