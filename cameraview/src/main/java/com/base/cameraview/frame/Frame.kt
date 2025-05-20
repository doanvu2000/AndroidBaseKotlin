package com.base.cameraview.frame

import android.annotation.SuppressLint
import com.base.cameraview.CameraLogger
import com.base.cameraview.size.Size

/**
 * A preview frame to be processed by [FrameProcessor]s.
 */
class Frame internal constructor(private val mManager: FrameManager<Any?>) {
    /**
     * Returns the class returned by [.getData].
     * This class depends on the engine that produced this frame.
     * - [Engine.CAMERA1] will produce byte[] arrays
     * - [Engine.CAMERA2] will produce [android.media.Image]s
     *
     * @return the data class
     */
    val dataClass: Class<*> = mManager.frameDataClass

    private var mData: Any? = null
    private var mTime: Long = -1
    private var mLastTime: Long = -1
    private var mUserRotation = 0
    private var mViewRotation = 0
    private var mSize: Size? = null
    private var mFormat = -1

    fun setContent(
        data: Any, time: Long, userRotation: Int, viewRotation: Int,
        size: Size, format: Int
    ) {
        mData = data
        mTime = time
        mLastTime = time
        mUserRotation = userRotation
        mViewRotation = viewRotation
        mSize = size
        mFormat = format
    }

    private fun hasContent(): Boolean {
        return mData != null
    }

    private fun ensureHasContent() {
        if (!hasContent()) {
            LOG.e("Frame is dead! time:", mTime, "lastTime:", mLastTime)
            throw RuntimeException(
                "You should not access a released frame. " +
                        "If this frame was passed to a FrameProcessor, you can only use its contents " +
                        "synchronously, for the duration of the process() method."
            )
        }
    }


    override fun equals(obj: Any?): Boolean {
        // We want a super fast implementation here, do not compare arrays.
        return obj is Frame && obj.mTime == mTime
    }

    /**
     * Clones the frame, returning a frozen content that will not be overwritten.
     * This can be kept or safely passed to other threads.
     * Using freeze without clearing with [.release] can result in memory leaks.
     *
     * @return a frozen Frame
     */
    @SuppressLint("NewApi")
    fun freeze(): Frame {
        ensureHasContent()
        val other = Frame(mManager)
        val data: Any = mManager.cloneFrameData(getData())!!
        other.setContent(data, mTime, mUserRotation, mViewRotation, mSize!!, mFormat)
        return other
    }

    /**
     * Disposes the contents of this frame. Can be useful for frozen frames
     * that are not useful anymore.
     */
    fun release() {
        if (!hasContent()) return
        LOG.v("Frame with time", mTime, "is being released.")
        val data = mData
        mData = null
        mUserRotation = 0
        mViewRotation = 0
        mTime = -1
        mSize = null
        mFormat = -1
        // After the manager is notified, this frame instance can be taken by
        // someone else, possibly from another thread. So this should be the
        // last call in this method. If we null data after, we can have issues.
        mManager.onFrameReleased(this, data)
    }

    /**
     * Returns the frame data.
     *
     * @return the frame data
     */
    fun <T> getData(): T {
        ensureHasContent()
        return mData as T
    }

    val time: Long
        /**
         * Returns the milliseconds epoch for this frame,
         * in the [System.currentTimeMillis] reference.
         *
         * @return time data
         */
        get() {
            ensureHasContent()
            return mTime
        }

    @get:Deprecated("use {@link #getRotationToUser()} instead")
    val rotation: Int
        get() = this.rotationToUser

    val rotationToUser: Int
        /**
         * Returns the clock-wise rotation that should be applied on the data
         * array, such that the resulting frame matches what the user is seeing
         * on screen. Knowing this can help in the processing phase.
         *
         * @return clock-wise rotation
         */
        get() {
            ensureHasContent()
            return mUserRotation
        }

    val rotationToView: Int
        /**
         * Returns the clock-wise rotation that should be applied on the data
         * array, such that the resulting frame matches the View / Activity orientation.
         * Knowing this can help in the drawing / rendering phase.
         *
         * @return clock-wise rotation
         */
        get() {
            ensureHasContent()
            return mViewRotation
        }

    val size: Size
        /**
         * Returns the frame size.
         *
         * @return frame size
         */
        get() {
            ensureHasContent()
            return mSize!!
        }

    val format: Int
        /**
         * Returns the data format, in one of the
         * [android.graphics.ImageFormat] constants.
         * This will always be [android.graphics.ImageFormat.NV21] for now.
         *
         * @return the data format
         * @see android.graphics.ImageFormat
         */
        get() {
            ensureHasContent()
            return mFormat
        }

    companion object {
        private val TAG: String = Frame::class.java.simpleName
        private val LOG: CameraLogger = CameraLogger.create(TAG)
    }
}
