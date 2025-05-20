package com.base.cameraview.gesture


/**
 * Not every gesture can control a certain action. For example, pinch gestures can only control
 * continuous values, such as zoom or AE correction. Single point gestures, on the other hand,
 * can only control point actions such as focusing or capturing a picture.
 */
enum class Gesture(private val type: GestureType) {
    /**
     * Pinch gesture, typically assigned to the zoom control.
     * This gesture can be mapped to continuous actions:
     *
     *
     * - [GestureAction.ZOOM]
     * - [GestureAction.EXPOSURE_CORRECTION]
     * - [GestureAction.FILTER_CONTROL_1]
     * - [GestureAction.FILTER_CONTROL_2]
     * - [GestureAction.NONE]
     */
    PINCH(GestureType.CONTINUOUS),

    /**
     * Single tap gesture, typically assigned to the focus control.
     * This gesture can be mapped to one shot actions:
     *
     *
     * - [GestureAction.AUTO_FOCUS]
     * - [GestureAction.TAKE_PICTURE]
     * - [GestureAction.NONE]
     */
    TAP(GestureType.ONE_SHOT),

    /**
     * Long tap gesture.
     * This gesture can be mapped to one shot actions:
     *
     *
     * - [GestureAction.AUTO_FOCUS]
     * - [GestureAction.TAKE_PICTURE]
     * - [GestureAction.NONE]
     */
    LONG_TAP(GestureType.ONE_SHOT),

    /**
     * Horizontal scroll gesture.
     * This gesture can be mapped to continuous actions:
     *
     *
     * - [GestureAction.ZOOM]
     * - [GestureAction.EXPOSURE_CORRECTION]
     * - [GestureAction.FILTER_CONTROL_1]
     * - [GestureAction.FILTER_CONTROL_2]
     * - [GestureAction.NONE]
     */
    SCROLL_HORIZONTAL(GestureType.CONTINUOUS),

    /**
     * Vertical scroll gesture.
     * This gesture can be mapped to continuous actions:
     *
     *
     * - [GestureAction.ZOOM]
     * - [GestureAction.EXPOSURE_CORRECTION]
     * - [GestureAction.FILTER_CONTROL_1]
     * - [GestureAction.FILTER_CONTROL_2]
     * - [GestureAction.NONE]
     */
    SCROLL_VERTICAL(GestureType.CONTINUOUS);

    /**
     * Whether this gesture can be assigned to the given [GestureAction].
     *
     * @param action the action to be checked
     * @return true if assignable
     */
    fun isAssignableTo(action: GestureAction): Boolean {
        return action == GestureAction.NONE || action.type() == type
    }
}
