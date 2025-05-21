package com.base.cameraview.markers

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.PointF
import android.view.View
import android.widget.FrameLayout

/**
 * Manages markers and provides an hierarchy / Canvas for them.
 * It is responsible for calling [Marker.onAttach].
 */
class MarkerLayout(context: Context) : FrameLayout(context) {
    @SuppressLint("UseSparseArrays")
    private val mViews = HashMap<Int?, View?>()

    /**
     * Notifies that a new marker was added, possibly replacing another one.
     *
     * @param type   the marker type
     * @param marker the marker
     */
    fun onMarker(type: Int, marker: Marker?) {
        // First check if we have a view for a previous marker of this type.
        val oldView = mViews.get(type)
        if (oldView != null) removeView(oldView)
        // If new marker is null, we're done.
        if (marker == null) return
        // Now see if we have a new view.
        val newView = marker.onAttach(context, this)
        if (newView != null) {
            mViews.put(type, newView)
            addView(newView)
        }
    }

    /**
     * The event that should trigger the drawing is about to be dispatched to
     * markers. If we have a valid View, cancel any animations on it and reposition
     * it.
     *
     * @param type   the event type
     * @param points the position
     */
    fun onEvent(type: Int, points: Array<PointF>) {
        val view = mViews.get(type)
        if (view == null) return
        view.clearAnimation()
        if (type == TYPE_AUTOFOCUS) {
            // TODO can't be sure that getWidth and getHeight are available here.
            val point = points[0]
            val x = (point.x - view.width / 2).toInt().toFloat()
            val y = (point.y - view.height / 2).toInt().toFloat()
            view.translationX = x
            view.translationY = y
        }
    }

    companion object {
        const val TYPE_AUTOFOCUS: Int = 1
    }
}
