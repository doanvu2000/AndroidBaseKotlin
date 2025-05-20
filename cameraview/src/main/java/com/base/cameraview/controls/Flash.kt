package com.base.cameraview.controls;

import androidx.annotation.NonNull;

import com.base.cameraview.CameraOptions;

/**
 * Flash value indicates the flash mode to be used.
 */
public enum Flash implements Control {

    /**
     * Flash is always off.
     */
    OFF(0),

    /**
     * Flash will be on when capturing.
     * This is not guaranteed to be supported.
     *
     * @see CameraOptions#getSupportedFlash()
     */
    ON(1),


    /**
     * Flash mode is chosen by the camera.
     * This is not guaranteed to be supported.
     *
     * @see CameraOptions#getSupportedFlash()
     */
    AUTO(2),


    /**
     * Flash is always on, working as a torch.
     * This is not guaranteed to be supported.
     *
     * @see CameraOptions#getSupportedFlash()
     */
    TORCH(3);

    static final Flash DEFAULT = OFF;

    private int value;

    Flash(int value) {
        this.value = value;
    }

    @NonNull
    static Flash fromValue(int value) {
        Flash[] list = Flash.values();
        for (Flash action : list) {
            if (action.value() == value) {
                return action;
            }
        }
        return DEFAULT;
    }

    int value() {
        return value;
    }
}
