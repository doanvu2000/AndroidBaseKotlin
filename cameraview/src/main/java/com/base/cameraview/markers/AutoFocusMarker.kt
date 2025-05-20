package com.base.cameraview.markers

import android.graphics.PointF

/**
 * A marker for the autofocus operations. Receives callback when focus starts,
 * ends successfully or failed, and can be used to draw on screen.
 *
 *
 * The point coordinates are meant with respect to [com.base.cameraview.CameraView] width and height,
 * so a 0, 0 point means that focus is happening on the top-left visible corner.
 */
interface AutoFocusMarker : Marker {
    /**
     * Called when the autofocus process has started.
     *
     * @param trigger the autofocus trigger
     * @param point   coordinates
     */
    fun onAutoFocusStart(trigger: AutoFocusTrigger, point: PointF)


    /**
     * Called when the autofocus process has ended, and the camera converged
     * to a new focus or failed while trying to do so.
     *
     * @param trigger    the autofocus trigger
     * @param successful whether the operation succeeded
     * @param point      coordinates
     */
    fun onAutoFocusEnd(
        trigger: AutoFocusTrigger,
        successful: Boolean,
        point: PointF
    )
}
