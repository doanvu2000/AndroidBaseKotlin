package com.base.cameraview.markers

import android.content.Context
import android.view.View
import android.view.ViewGroup

/**
 * A marker is an overlay over the [com.base.cameraview.CameraView] preview, which should be drawn
 * at specific times during the camera lifecycle.
 * Currently only [AutoFocusMarker] is available.
 */
interface Marker {
    /**
     * Marker is being attached to the CameraView. If a [View] is returned,
     * it becomes part of the hierarchy and is automatically translated (if possible)
     * to match the event place on screen, for example the point where autofocus was started
     * by the user finger.
     *
     * @param context   a context
     * @param container a container
     * @return a view or null
     */
    fun onAttach(context: Context, container: ViewGroup): View?
}
