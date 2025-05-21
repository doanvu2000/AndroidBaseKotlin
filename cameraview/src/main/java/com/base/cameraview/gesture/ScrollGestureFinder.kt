package com.base.cameraview.gesture

import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import com.base.cameraview.CameraLogger
import kotlin.math.abs

/**
 * A [GestureFinder] that detects [Gesture.SCROLL_HORIZONTAL]
 * and [Gesture.SCROLL_VERTICAL] gestures.
 */
class ScrollGestureFinder(controller: Controller) : GestureFinder(controller, 2) {
    private val mDetector: GestureDetector
    private var mNotify = false

    /* for tests */
    private var factor: Float = 0f

    init {
        mDetector = GestureDetector(
            controller.context,
            object : SimpleOnGestureListener() {
                override fun onScroll(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    distanceX: Float,
                    distanceY: Float
                ): Boolean {
                    val horizontal: Boolean
                    LOG.i("onScroll:", "distanceX=$distanceX", "distanceY=$distanceY")
                    if (e1 == null) return false // Got some crashes about this.

                    if (e1.x != getPoint(0).x || e1.y != getPoint(0).y) {
                        // First step. We choose now if it's a vertical or horizontal scroll, and
                        // stick to it for the whole gesture.
                        horizontal = abs(distanceX) >= abs(distanceY)
                        gesture =
                            if (horizontal) Gesture.SCROLL_HORIZONTAL else Gesture.SCROLL_VERTICAL
                        getPoint(0).set(e1.x, e1.y)
                    } else {
                        // Not the first step. We already defined the type.
                        horizontal = gesture == Gesture.SCROLL_HORIZONTAL
                    }
                    getPoint(1).set(e2.x, e2.y)
                    factor = if (horizontal)
                        (distanceX / controller.width)
                    else
                        (distanceY / controller.height)
                    factor =
                        if (horizontal) -factor else factor // When vertical, up = positive
                    mNotify = true
                    return true
                }
            })

        mDetector.setIsLongpressEnabled(false) // Looks important.
    }

    override fun handleTouchEvent(event: MotionEvent): Boolean {
        // Reset the mNotify flag on a new gesture.
        // This is to ensure that the mNotify flag stays on until the
        // previous gesture ends.
        if (event.action == MotionEvent.ACTION_DOWN) {
            mNotify = false
        }

        // Let's see if we detect something.
        mDetector.onTouchEvent(event)

        // Keep notifying CameraView as long as the gesture goes.
        if (mNotify) LOG.i("Notifying a gesture of type", gesture.name)
        return mNotify
    }

    public override fun getValue(currValue: Float, minValue: Float, maxValue: Float): Float {
        var delta = this.factor // -1 ... 1

        // ^ This works well if minValue = 0, maxValue = 1.
        // Account for the different range:
        delta *= (maxValue - minValue) // -(max-min) ... (max-min)
        delta *= 2f // Add some sensitivity.

        return currValue + delta
    }

    companion object {
        private val TAG: String = ScrollGestureFinder::class.java.simpleName
        private val LOG: CameraLogger = CameraLogger.create(TAG)
    }
}
