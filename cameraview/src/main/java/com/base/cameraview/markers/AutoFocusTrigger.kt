package com.base.cameraview.markers

/**
 * Gives information about what triggered the autofocus operation.
 */
enum class AutoFocusTrigger {
    /**
     * Autofocus was triggered by [com.base.cameraview.gesture.GestureAction.AUTO_FOCUS].
     */
    GESTURE,

    /**
     * Autofocus was triggered by the [com.base.cameraview.CameraView.startAutoFocus] method.
     */
    METHOD
}
