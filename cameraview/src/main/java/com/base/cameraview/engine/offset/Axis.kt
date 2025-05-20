package com.base.cameraview.engine.offset

/**
 * The axis around which offsets are computed. We have two possibilities:
 * - an axis going out of the device screen towards the user's face
 * - an axis going out of the device screen towards the back
 *
 *
 * We are mostly interested in the first one, but some APIs will require
 * angles in the sensor reference, in which case, for front cameras, we are
 * required to use [.RELATIVE_TO_SENSOR].
 */
enum class Axis {
    /**
     * This rotation axis is the one going out of the device screen
     * towards the user's face.
     */
    ABSOLUTE,

    /**
     * This rotation axis takes into account the current
     */
    RELATIVE_TO_SENSOR
}
