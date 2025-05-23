package com.base.cameraview

import android.graphics.Bitmap
import androidx.annotation.UiThread

/**
 * Receives callbacks about a bitmap decoding operation.
 */
interface BitmapCallback {
    /**
     * Notifies that the bitmap was successfully decoded.
     * This is run on the UI thread.
     * Returns a null object if an [OutOfMemoryError] was encountered.
     *
     * @param bitmap decoded bitmap, or null
     */
    @UiThread
    fun onBitmapReady(bitmap: Bitmap?)
}
