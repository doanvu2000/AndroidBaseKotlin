package com.base.cameraview.gesture


/**
 * Not every gesture can control a certain action. For example, pinch gestures can only control
 * continuous values, such as zoom or AE correction. Single point gestures, on the other hand,
 * can only control point actions such as focusing or capturing a picture.
 */
enum class GestureAction(private val value: Int, private val type: GestureType) {
    /**
     * No action. This can be mapped to any gesture to disable it.
     */
    NONE(0, GestureType.ONE_SHOT),

    /**
     * Touch metering control, typically assigned to the tap gesture.
     * This action can be mapped to one shot gestures:
     *
     *
     * - [Gesture.TAP]
     * - [Gesture.LONG_TAP]
     */
    AUTO_FOCUS(1, GestureType.ONE_SHOT),

    /**
     * When triggered, this action will fire a picture shoot.
     * This action can be mapped to one shot gestures:
     *
     *
     * - [Gesture.TAP]
     * - [Gesture.LONG_TAP]
     */
    TAKE_PICTURE(2, GestureType.ONE_SHOT),

    /**
     * When triggered, this action will fire a picture snapshot.
     * This action can be mapped to one shot gestures:
     *
     *
     * - [Gesture.TAP]
     * - [Gesture.LONG_TAP]
     */
    TAKE_PICTURE_SNAPSHOT(3, GestureType.ONE_SHOT),

    /**
     * Zoom control, typically assigned to the pinch gesture.
     * This action can be mapped to continuous gestures:
     *
     *
     * - [Gesture.PINCH]
     * - [Gesture.SCROLL_HORIZONTAL]
     * - [Gesture.SCROLL_VERTICAL]
     */
    ZOOM(4, GestureType.CONTINUOUS),

    /**
     * Exposure correction control.
     * This action can be mapped to continuous gestures:
     *
     *
     * - [Gesture.PINCH]
     * - [Gesture.SCROLL_HORIZONTAL]
     * - [Gesture.SCROLL_VERTICAL]
     */
    EXPOSURE_CORRECTION(5, GestureType.CONTINUOUS),

    /**
     * Controls the first parameter of a real-time [com.base.cameraview.filter.Filter],
     * if it accepts one. This action can be mapped to continuous gestures:
     *
     *
     * - [Gesture.PINCH]
     * - [Gesture.SCROLL_HORIZONTAL]
     * - [Gesture.SCROLL_VERTICAL]
     */
    FILTER_CONTROL_1(6, GestureType.CONTINUOUS),

    /**
     * Controls the second parameter of a real-time [com.base.cameraview.filter.Filter],
     * if it accepts one. This action can be mapped to continuous gestures:
     *
     *
     * - [Gesture.PINCH]
     * - [Gesture.SCROLL_HORIZONTAL]
     * - [Gesture.SCROLL_VERTICAL]
     */
    FILTER_CONTROL_2(7, GestureType.CONTINUOUS);

    fun value(): Int {
        return value
    }

    fun type(): GestureType {
        return type
    }

    companion object {
        @JvmField
        val DEFAULT_PINCH: GestureAction = NONE

        @JvmField
        val DEFAULT_TAP: GestureAction = NONE

        @JvmField
        val DEFAULT_LONG_TAP: GestureAction = NONE

        @JvmField
        val DEFAULT_SCROLL_HORIZONTAL: GestureAction = NONE

        @JvmField
        val DEFAULT_SCROLL_VERTICAL: GestureAction = NONE

        @JvmStatic
        fun fromValue(value: Int): GestureAction? {
            val list = GestureAction.entries.toTypedArray()
            for (action in list) {
                if (action.value() == value) {
                    return action
                }
            }
            return null
        }
    }
}
