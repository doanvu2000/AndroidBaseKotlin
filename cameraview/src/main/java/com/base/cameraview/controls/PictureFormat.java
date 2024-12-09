package com.base.cameraview.controls;


import androidx.annotation.NonNull;


public enum PictureFormat implements Control {

    /**
     * The picture result data will be a JPEG file.
     * This value is always supported.
     */
    JPEG(0),

    /**
     * The picture result data will be a DNG file.
     * This is only supported with the {@link Engine#CAMERA2} engine and only on
     */
    DNG(1);

    static final PictureFormat DEFAULT = JPEG;

    private int value;

    PictureFormat(int value) {
        this.value = value;
    }

    @NonNull
    static PictureFormat fromValue(int value) {
        PictureFormat[] list = PictureFormat.values();
        for (PictureFormat action : list) {
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
