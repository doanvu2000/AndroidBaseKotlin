package com.base.cameraview.controls;

import androidx.annotation.NonNull;

/**
 * Audio values indicate whether to record audio stream when record video.
 */
public enum Audio implements Control {

    /**
     * No audio.
     */
    OFF(0),

    /**
     * Audio on. The number of channels depends on the video configuration,
     * on the device capabilities and on the video type (e.g. we default to
     * mono for snapshots).
     */
    ON(1),

    /**
     * Force mono channel audio.
     */
    MONO(2),

    /**
     * Force stereo audio.
     */
    STEREO(3);

    final static Audio DEFAULT = ON;

    private int value;

    Audio(int value) {
        this.value = value;
    }

    @NonNull
    static Audio fromValue(int value) {
        Audio[] list = Audio.values();
        for (Audio action : list) {
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
