package com.base.cameraview.gesture

import android.content.res.TypedArray
import com.base.cameraview.R
import com.base.cameraview.gesture.GestureAction.Companion.fromValue

/**
 * Parses gestures from XML attributes.
 */
class GestureParser(array: TypedArray) {
    private val tapAction: Int = array.getInteger(
        R.styleable.CameraView_cameraGestureTap,
        GestureAction.DEFAULT_TAP.value()
    )
    private val longTapAction: Int = array.getInteger(
        R.styleable.CameraView_cameraGestureLongTap,
        GestureAction.DEFAULT_LONG_TAP.value()
    )
    private val pinchAction: Int = array.getInteger(
        R.styleable.CameraView_cameraGesturePinch,
        GestureAction.DEFAULT_PINCH.value()
    )
    private val horizontalScrollAction: Int = array.getInteger(
        R.styleable.CameraView_cameraGestureScrollHorizontal,
        GestureAction.DEFAULT_SCROLL_HORIZONTAL.value()
    )
    private val verticalScrollAction: Int = array.getInteger(
        R.styleable.CameraView_cameraGestureScrollVertical,
        GestureAction.DEFAULT_SCROLL_VERTICAL.value()
    )

    private fun get(which: Int): GestureAction? {
        return fromValue(which)
    }

    fun getTapAction(): GestureAction? {
        return get(tapAction)
    }

    fun getLongTapAction(): GestureAction? {
        return get(longTapAction)
    }

    fun getPinchAction(): GestureAction? {
        return get(pinchAction)
    }

    fun getHorizontalScrollAction(): GestureAction? {
        return get(horizontalScrollAction)
    }

    fun getVerticalScrollAction(): GestureAction? {
        return get(verticalScrollAction)
    }
}
