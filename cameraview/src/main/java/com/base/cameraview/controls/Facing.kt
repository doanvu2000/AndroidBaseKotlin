package com.base.cameraview.controls;


import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.base.cameraview.CameraUtils;

/**
 * Facing value indicates which camera sensor should be used for the current session.
 */
public enum Facing implements Control {

    /**
     * Back-facing camera sensor.
     */
    BACK(0),

    /**
     * Front-facing camera sensor.
     */
    FRONT(1);

    private int value;

    Facing(int value) {
        this.value = value;
    }

    @NonNull
    static Facing DEFAULT(@Nullable Context context) {
        if (context == null) {
            return BACK;
        } else if (CameraUtils.hasCameraFacing(context, BACK)) {
            return BACK;
        } else if (CameraUtils.hasCameraFacing(context, FRONT)) {
            return FRONT;
        } else {
            // The controller will throw a CameraException.
            // This device has no cameras.
            return BACK;
        }
    }

    @Nullable
    static Facing fromValue(int value) {
        Facing[] list = Facing.values();
        for (Facing action : list) {
            if (action.value() == value) {
                return action;
            }
        }
        return null;
    }

    int value() {
        return value;
    }
}
