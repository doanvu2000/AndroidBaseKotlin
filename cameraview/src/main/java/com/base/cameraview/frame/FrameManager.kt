package com.base.cameraview.frame

import android.graphics.ImageFormat
import com.base.cameraview.CameraLogger
import com.base.cameraview.engine.offset.Angles
import com.base.cameraview.engine.offset.Axis
import com.base.cameraview.engine.offset.Reference
import com.base.cameraview.size.Size
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.ceil


/**
 * This class manages the allocation of [Frame] objects.
 * The FrameManager keeps a [.mPoolSize] integer that defines the number of instances to keep.
 *
 *
 * Main methods are:
 * - [.release]: to release. After release, a manager can be setUp again.
 * - [.getFrame]: gets a new [Frame].
 *
 *
 * For frames to get back to the FrameManager pool, all you have to do
 * is call [Frame.release] when done.
 */
abstract class FrameManager<T> protected constructor(
    /**
     * Returns the pool size.
     *
     * @return pool size
     */
    val poolSize: Int,
    /**
     * Returns the frame data class.
     *
     * @return frame data class
     */
    val frameDataClass: Class<T?>
) {
    /**
     * Returns the frame size in bytes.
     *
     * @return frame size in bytes
     */
    var frameBytes: Int = -1
        private set
    private var mFrameSize: Size? = null
    private var mFrameFormat = -1
    private val mFrameQueue: LinkedBlockingQueue<Frame?> = LinkedBlockingQueue<Frame?>(
        this.poolSize
    )
    private var mAngles: Angles? = null

    /**
     * Allocates a [.mPoolSize] number of buffers. Should be called once
     * the preview size and the image format value are known.
     *
     *
     * This method can be called again after [.release] has been called.
     *
     * @param format the image format
     * @param size   the frame size
     * @param angles angle object
     */
    open fun setUp(format: Int, size: Size, angles: Angles) {
//        if (this.isSetUp) {
//            // TODO throw or just reconfigure?
//        }
        mFrameSize = size
        mFrameFormat = format
        val bitsPerPixel = ImageFormat.getBitsPerPixel(format)
        val sizeInBits = (size.height * size.width * bitsPerPixel).toLong()
        this.frameBytes = ceil(sizeInBits / 8.0).toInt()
        for (i in 0..<this.poolSize) {
            mFrameQueue.offer(Frame(this))
        }
        mAngles = angles
    }

    protected val isSetUp: Boolean
        /**
         * Returns true after [.setUp]
         * but before [.release].
         * Returns false otherwise.
         *
         * @return true if set up
         */
        get() = mFrameSize != null

    /**
     * Returns a new Frame for the given data. This must be called
     * - after [.setUp], which sets the buffer size
     * - after the T data has been filled
     *
     * @param data data
     * @param time timestamp
     * @return a new frame
     */
    fun getFrame(data: T, time: Long): Frame? {
        check(this.isSetUp) {
            "Can't call getFrame() after releasing " +
                    "or before setUp."
        }

        val frame = mFrameQueue.poll()
        if (frame != null) {
            LOG.v("getFrame for time:", time, "RECYCLING.")
            val userRotation = mAngles!!.offset(
                Reference.SENSOR, Reference.OUTPUT,
                Axis.RELATIVE_TO_SENSOR
            )
            val viewRotation = mAngles!!.offset(
                Reference.SENSOR, Reference.VIEW,
                Axis.RELATIVE_TO_SENSOR
            )
            frame.setContent(data!!, time, userRotation, viewRotation, mFrameSize!!, mFrameFormat)
            return frame
        } else {
            LOG.i("getFrame for time:", time, "NOT AVAILABLE.")
            onFrameDataReleased(data, false)
            return null
        }
    }

    /**
     * Called by child frames when they are released.
     *
     * @param frame the released frame
     */
    fun onFrameReleased(frame: Frame, data: T) {
        if (!this.isSetUp) return
        // If frame queue is full, let's drop everything.
        // If frame queue accepts this frame, let's recycle the buffer as well.
        val recycled = mFrameQueue.offer(frame)
        onFrameDataReleased(data, recycled)
    }

    /**
     * Called when a Frame was released and its data is now available.
     * This might be called from old Frames that belong to an old 'setUp'
     * of this FrameManager instance. So the buffer size might be different,
     * for instance.
     *
     * @param data     data
     * @param recycled recycled
     */
    protected abstract fun onFrameDataReleased(data: T, recycled: Boolean)

    fun cloneFrameData(data: T): T {
        return onCloneFrameData(data)
    }

    protected abstract fun onCloneFrameData(data: T): T

    /**
     * Releases all frames controlled by this manager and
     * clears the pool.
     */
    open fun release() {
        if (!this.isSetUp) {
            LOG.w("release called twice. Ignoring.")
            return
        }

        LOG.i("release: Clearing the frame and buffer queue.")
        mFrameQueue.clear()
        this.frameBytes = -1
        mFrameSize = null
        mFrameFormat = -1
        mAngles = null
    }

    companion object {
        private val TAG: String = FrameManager::class.java.simpleName

        @JvmField
        protected val LOG: CameraLogger = CameraLogger.create(TAG)
    }
}
