package com.base.cameraview.controls

import android.content.Context
import com.base.cameraview.CameraUtils


/**
 * Facing value indicates which camera sensor should be used for the current session.
 */
enum class Facing(val value: Int) : Control {
    /**
     * Back-facing camera sensor.
     */
    BACK(0),

    /**
     * Front-facing camera sensor.
     */
    FRONT(1);

    companion object {
        @JvmStatic
        fun defaultFacing(context: Context?): Facing {
            return if (context == null) {
                BACK
            } else if (CameraUtils.hasCameraFacing(context, BACK)) {
                BACK
            } else if (CameraUtils.hasCameraFacing(context, FRONT)) {
                FRONT
            } else {
                // The controller will throw a CameraException.
                // This device has no cameras.
                BACK
            }
        }

        fun defaultFacing() = BACK

        @JvmStatic
        fun fromValue(value: Int): Facing? {
            val list = Facing.entries.toTypedArray()
            for (action in list) {
                if (action.value == value) {
                    return action
                }
            }
            return null
        }
    }
}
