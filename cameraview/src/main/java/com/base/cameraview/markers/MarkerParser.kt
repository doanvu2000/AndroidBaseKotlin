package com.base.cameraview.markers

import android.content.res.TypedArray
import com.base.cameraview.R

/**
 * Parses markers from XML attributes.
 */
class MarkerParser(array: TypedArray) {
    var autoFocusMarker: AutoFocusMarker? = null
        private set

    init {
        val autoFocusName = array.getString(R.styleable.CameraView_cameraAutoFocusMarker)
        if (autoFocusName != null) {
            try {
                val autoFocusClass = Class.forName(autoFocusName)
                autoFocusMarker = autoFocusClass.newInstance() as AutoFocusMarker
            } catch (ignore: Exception) {
            }
        }
    }
}
