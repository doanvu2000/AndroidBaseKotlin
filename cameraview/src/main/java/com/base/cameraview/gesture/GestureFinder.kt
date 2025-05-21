package com.base.cameraview.gesture

import android.content.Context
import android.graphics.PointF
import android.view.MotionEvent
import androidx.annotation.VisibleForTesting

/**
 * Base class for gesture finders.
 * Gesture finders are passed down touch events to detect gestures.
 */
abstract class GestureFinder internal constructor(
    /**
     * Returns the controller for this finder.
     *
     * @return the controller
     */
    protected val controller: Controller, points: Int
) {
    @VisibleForTesting
    var mType: Gesture? = null
    /**
     * Whether this instance is active, which means, it is listening
     * to events and identifying new gestures.
     *
     * @return true if active
     */
    /**
     * Makes this instance active, which means, listening to events.
     *
     * @param active whether this should be active or not
     */
    var isActive: Boolean = false

    /**
     * Returns an array of points that identify the currently
     * detected gesture. If no gesture was detected, this returns
     * an array of points with x and y set to 0.
     *
     * @return array of gesture points
     */
    val points: Array<PointF> = arrayOfNulls<PointF>(points) as Array<PointF>

    init {
        for (i in 0..<points) {
            this.points[i] = PointF(0f, 0f)
        }
    }

    /**
     * Called when new events are available.
     * If true is returned, users will call [.getGesture], [.getPoints]
     * and maybe [.getValue] to know more about the gesture.
     *
     * @param event the new event
     * @return true if a gesture was detected
     */
    fun onTouchEvent(event: MotionEvent): Boolean {
        if (!this.isActive) return false
        return handleTouchEvent(event)
    }

    /**
     * Called when new events are available.
     * If true is returned, users will call [.getGesture], [.getPoints]
     * and maybe [.getValue] to know more about the gesture.
     *
     * @param event the new event
     * @return true if a gesture was detected
     */
    protected abstract fun handleTouchEvent(event: MotionEvent): Boolean

    var gesture: Gesture
        /**
         * Returns the gesture that this instance is currently detecting.
         * This is mutable - for instance, a scroll layout can detect both
         * horizontal and vertical scroll gestures.
         *
         * @return the current gesture
         */
        get() = mType!!
        /**
         * Sets the currently detected gesture.
         *
         * @param gesture the current gesture
         * @see .getGesture
         */
        protected set(gesture) {
            mType = gesture
        }

    /**
     * Utility function to access an item in the
     * [.getPoints] array.
     *
     * @param which the array position
     * @return the point
     */
    protected fun getPoint(which: Int): PointF {
        return this.points[which]
    }

    /**
     * For [GestureType.CONTINUOUS] gestures, returns the float value at the current
     * gesture state. This means, for example, scaling the old value with a pinch factor,
     * taking into account the minimum and maximum values.
     *
     * @param currValue the last value
     * @param minValue  the min possible value
     * @param maxValue  the max possible value
     * @return the new continuous value
     */
    fun computeValue(currValue: Float, minValue: Float, maxValue: Float): Float {
        return capValue(currValue, getValue(currValue, minValue, maxValue), minValue, maxValue)
    }

    /**
     * For [GestureType.CONTINUOUS] gestures, returns the float value at the current
     * gesture state. This means, for example, scaling the old value with a pinch factor,
     * taking into account the minimum and maximum values.
     *
     * @param currValue the last value
     * @param minValue  the min possible value
     * @param maxValue  the max possible value
     * @return the new continuous value
     */
    protected abstract fun getValue(currValue: Float, minValue: Float, maxValue: Float): Float

    interface Controller {
        val context: Context
        val width: Int
        val height: Int
    }

    companion object {
        // The number of possible values between minValue and maxValue, for the getValue method.
        // We could make this non-static (e.g. larger granularity for exposure correction).
        private const val GRANULARITY = 50

        /**
         * Checks for newValue to be between minValue and maxValue,
         * and checks that it is 'far enough' from the oldValue, in order
         * to reduce useless updates.
         */
        private fun capValue(
            oldValue: Float,
            newValue: Float,
            minValue: Float,
            maxValue: Float
        ): Float {
            var newValue = newValue
            if (newValue < minValue) newValue = minValue
            if (newValue > maxValue) newValue = maxValue

            val distance = (maxValue - minValue) / GRANULARITY.toFloat()
            val half = distance / 2
            if (newValue >= oldValue - half && newValue <= oldValue + half) {
                // Too close! Return the oldValue.
                return oldValue
            }
            return newValue
        }
    }
}
