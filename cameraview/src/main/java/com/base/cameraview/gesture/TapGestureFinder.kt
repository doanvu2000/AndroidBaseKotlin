package com.base.cameraview.gesture

import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent

class TapGestureFinder(controller: Controller) : GestureFinder(controller, 1) {
    private val mDetector: GestureDetector
    private var mNotify = false

    init {
        mDetector = GestureDetector(
            controller.context,
            object : SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    mNotify = true
                    gesture = Gesture.TAP
                    return true
                }

                /*
            TODO should use onSingleTapConfirmed and enable this.
            public boolean onDoubleTap(MotionEvent e) {
                mNotify = true;
                mType = Gesture.DOUBLE_TAP;
                return true;
            } */
                override fun onLongPress(e: MotionEvent) {
                    mNotify = true
                    gesture = Gesture.LONG_TAP
                }
            })

        mDetector.setIsLongpressEnabled(true)
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
        if (mNotify) {
            getPoint(0).x = event.x
            getPoint(0).y = event.y
            return true
        }
        return false
    }

    public override fun getValue(currValue: Float, minValue: Float, maxValue: Float): Float {
        return 0f
    }
}
