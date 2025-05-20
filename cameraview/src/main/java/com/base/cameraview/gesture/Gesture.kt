package com.base.cameraview.gesture;


import androidx.annotation.NonNull;


/**
 * Not every gesture can control a certain action. For example, pinch gestures can only control
 * continuous values, such as zoom or AE correction. Single point gestures, on the other hand,
 * can only control point actions such as focusing or capturing a picture.
 */
public enum Gesture {

    /**
     * Pinch gesture, typically assigned to the zoom control.
     * This gesture can be mapped to continuous actions:
     * <p>
     * - {@link GestureAction#ZOOM}
     * - {@link GestureAction#EXPOSURE_CORRECTION}
     * - {@link GestureAction#FILTER_CONTROL_1}
     * - {@link GestureAction#FILTER_CONTROL_2}
     * - {@link GestureAction#NONE}
     */
    PINCH(GestureType.CONTINUOUS),

    /**
     * Single tap gesture, typically assigned to the focus control.
     * This gesture can be mapped to one shot actions:
     * <p>
     * - {@link GestureAction#AUTO_FOCUS}
     * - {@link GestureAction#TAKE_PICTURE}
     * - {@link GestureAction#NONE}
     */
    TAP(GestureType.ONE_SHOT),

    /**
     * Long tap gesture.
     * This gesture can be mapped to one shot actions:
     * <p>
     * - {@link GestureAction#AUTO_FOCUS}
     * - {@link GestureAction#TAKE_PICTURE}
     * - {@link GestureAction#NONE}
     */
    LONG_TAP(GestureType.ONE_SHOT),

    /**
     * Horizontal scroll gesture.
     * This gesture can be mapped to continuous actions:
     * <p>
     * - {@link GestureAction#ZOOM}
     * - {@link GestureAction#EXPOSURE_CORRECTION}
     * - {@link GestureAction#FILTER_CONTROL_1}
     * - {@link GestureAction#FILTER_CONTROL_2}
     * - {@link GestureAction#NONE}
     */
    SCROLL_HORIZONTAL(GestureType.CONTINUOUS),

    /**
     * Vertical scroll gesture.
     * This gesture can be mapped to continuous actions:
     * <p>
     * - {@link GestureAction#ZOOM}
     * - {@link GestureAction#EXPOSURE_CORRECTION}
     * - {@link GestureAction#FILTER_CONTROL_1}
     * - {@link GestureAction#FILTER_CONTROL_2}
     * - {@link GestureAction#NONE}
     */
    SCROLL_VERTICAL(GestureType.CONTINUOUS);

    private GestureType type;

    Gesture(@NonNull GestureType type) {
        this.type = type;
    }

    /**
     * Whether this gesture can be assigned to the given {@link GestureAction}.
     *
     * @param action the action to be checked
     * @return true if assignable
     */
    public boolean isAssignableTo(@NonNull GestureAction action) {
        return action == GestureAction.NONE || action.type() == type;
    }

}
