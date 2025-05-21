package com.base.cameraview.picture

import androidx.annotation.VisibleForTesting
import com.base.cameraview.PictureResult

/**
 * Interface for picture capturing.
 * Don't call start if already started. Don't call stop if already stopped.
 * Don't reuse.
 */
abstract class PictureRecorder(
    stub: PictureResult.Stub,
    listener: PictureResultListener?
) {
    protected var mError: Exception? = null

    @JvmField
    @VisibleForTesting(otherwise = VisibleForTesting.Companion.PROTECTED)
    var mResult: PictureResult.Stub?

    @VisibleForTesting
    var mListener: PictureResultListener?

    /**
     * Creates a new picture recorder.
     *
     * @param stub     a picture stub
     * @param listener a listener
     */
    init {
        mResult = stub
        mListener = listener
    }

    /**
     * Takes a picture.
     */
    abstract fun take()

    /**
     * Subclasses can call this to notify that the shutter was activated,
     * and whether it did play some sound or not.
     *
     * @param didPlaySound whether it played sounds
     */
    protected fun dispatchOnShutter(didPlaySound: Boolean) {
        if (mListener != null) mListener!!.onPictureShutter(didPlaySound)
    }

    /**
     * Subclasses can call this to notify that the result was obtained,
     * either with some error (null result) or with the actual stub, filled.
     */
    protected open fun dispatchResult() {
        if (mListener != null) {
            mListener!!.onPictureResult(mResult, mError)
            mListener = null
            mResult = null
        }
    }

    /**
     * Listens for picture recorder events.
     */
    interface PictureResultListener {
        /**
         * The shutter was activated.
         *
         * @param didPlaySound whether a sound was played
         */
        fun onPictureShutter(didPlaySound: Boolean)

        /**
         * Picture was taken or there was some error, if
         * the result is null.
         *
         * @param result the result or null if there was some error
         * @param error  the error or null if there wasn't any
         */
        fun onPictureResult(result: PictureResult.Stub?, error: Exception?)
    }
}
