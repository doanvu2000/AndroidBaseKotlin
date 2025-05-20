package com.base.cameraview.engine.offset

import androidx.annotation.VisibleForTesting
import com.base.cameraview.CameraLogger
import com.base.cameraview.controls.Facing

/**
 * These offsets are computed based on the [.setSensorOffset],
 * [.setDisplayOffset] and [.setDeviceOrientation] values that are coming
 * from outside.
 *
 *
 * When communicating with the sensor, [Axis.RELATIVE_TO_SENSOR] should probably be used.
 * This means inverting the offset when using the front camera.
 * This is often the case when calling offset(SENSOR, OUTPUT), for example when passing a JPEG
 * rotation to the sensor. That is meant to be consumed as relative to the sensor plane.
 *
 *
 * For all other usages, [Axis.ABSOLUTE] is probably a better choice.
 */
class Angles {
    @VisibleForTesting
    var mSensorOffset: Int = 0

    @VisibleForTesting
    var mDisplayOffset: Int = 0

    @VisibleForTesting
    var mDeviceOrientation: Int = 0
    private var mSensorFacing: Facing? = null

    /**
     * We want to keep everything in the [Axis.ABSOLUTE] reference,
     * so a front facing sensor offset must be inverted.
     *
     * @param sensorFacing sensor facing value
     * @param sensorOffset sensor offset
     */
    fun setSensorOffset(sensorFacing: Facing, sensorOffset: Int) {
        sanitizeInput(sensorOffset)
        mSensorFacing = sensorFacing
        mSensorOffset = sensorOffset
        if (mSensorFacing == Facing.FRONT) {
            mSensorOffset = sanitizeOutput(360 - mSensorOffset)
        }
        print()
    }

    /**
     * Sets the display offset.
     *
     * @param displayOffset the display offset
     */
    fun setDisplayOffset(displayOffset: Int) {
        sanitizeInput(displayOffset)
        mDisplayOffset = displayOffset
        print()
    }

    /**
     * Sets the device orientation.
     *
     * @param deviceOrientation the device orientation
     */
    fun setDeviceOrientation(deviceOrientation: Int) {
        sanitizeInput(deviceOrientation)
        mDeviceOrientation = deviceOrientation
        print()
    }

    private fun print() {
        LOG.i(
            "Angles changed:",
            "sensorOffset:", mSensorOffset,
            "displayOffset:", mDisplayOffset,
            "deviceOrientation:", mDeviceOrientation
        )
    }

    /**
     * Returns the offset between two reference systems, computed along the given axis.
     *
     * @param from the source reference system
     * @param to   the destination reference system
     * @param axis the axis
     * @return the offset
     */
    fun offset(from: Reference, to: Reference, axis: Axis): Int {
        var offset = absoluteOffset(from, to)
        if (axis == Axis.RELATIVE_TO_SENSOR) {
            if (mSensorFacing == Facing.FRONT) {
                offset = sanitizeOutput(360 - offset)
            }
        }
        return offset
    }

    private fun absoluteOffset(from: Reference, to: Reference): Int {
        if (from == to) {
            return 0
        } else if (to == Reference.BASE) {
            return sanitizeOutput(360 - absoluteOffset(to, from))
        } else if (from == Reference.BASE) {
            return when (to) {
                Reference.VIEW -> sanitizeOutput(360 - mDisplayOffset)
                Reference.OUTPUT -> sanitizeOutput(mDeviceOrientation)
                Reference.SENSOR -> sanitizeOutput(360 - mSensorOffset)
                else -> throw RuntimeException("Unknown reference: $to")
            }
        } else {
            return sanitizeOutput(
                absoluteOffset(Reference.BASE, to)
                        - absoluteOffset(Reference.BASE, from)
            )
        }
    }

    /**
     * Whether the two references systems are flipped.
     *
     * @param from source
     * @param to   destination
     * @return true if flipped
     */
    fun flip(from: Reference, to: Reference): Boolean {
        return offset(from, to, Axis.ABSOLUTE) % 180 != 0
    }

    private fun sanitizeInput(value: Int) {
        check(!(value != 0 && value != 90 && value != 180 && value != 270)) { "This value is not sanitized: $value" }
    }

    private fun sanitizeOutput(value: Int): Int {
        return (value + 360) % 360
    }

    companion object {
        private val TAG: String = Angles::class.java.simpleName
        private val LOG: CameraLogger = CameraLogger.create(TAG)
    }
}
