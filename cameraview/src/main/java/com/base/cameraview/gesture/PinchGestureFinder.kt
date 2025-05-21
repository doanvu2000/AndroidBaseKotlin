package com.base.cameraview.gesture

import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener

class PinchGestureFinder(controller: Controller) : GestureFinder(controller, 2) {
    private val mDetector: ScaleGestureDetector
    private var mNotify = false

    /* for tests */
    private var factor: Float = 0f

    init {
        gesture = Gesture.PINCH

        mDetector = ScaleGestureDetector(
            controller.context,
            object : SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    mNotify = true
                    factor = ((detector.scaleFactor - 1) * ADD_SENSITIVITY)
                    return true
                }
            })

        mDetector.isQuickScaleEnabled = false
    }

    override fun handleTouchEvent(event: MotionEvent): Boolean {
        // Reset the mNotify flag on a new gesture.
        // This is to ensure that the mNotify flag stays on until the
        // previous gesture ends.
        if (event.action == MotionEvent.ACTION_DOWN) {
            mNotify = false
        }

        // Let's see if we detect something. This will call onScale().
        mDetector.onTouchEvent(event)

        // Keep notifying CameraView as long as the gesture goes.
        if (mNotify) {
            getPoint(0).x = event.getX(0)
            getPoint(0).y = event.getY(0)
            if (event.pointerCount > 1) {
                getPoint(1).x = event.getX(1)
                getPoint(1).y = event.getY(1)
            }
            return true
        }
        return false
    }

    public override fun getValue(currValue: Float, minValue: Float, maxValue: Float): Float {
        var add = this.factor
        // ^ This works well if minValue = 0, maxValue = 1.
        // Account for the different range:
        add *= (maxValue - minValue)

        // ^ This works well if currValue = 0.
        // Account for a different starting point:
        /* if (add > 0) {
            add *= (maxValue - currValue);
        } else if (add < 0) {
            add *= (currValue - minValue);
        } Nope, I don't like this, it slows everything down. */
        return currValue + add
    }


    companion object {
        private const val ADD_SENSITIVITY = 2f
    }
}
