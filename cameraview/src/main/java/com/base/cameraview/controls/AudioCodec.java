package com.base.cameraview.controls;


import androidx.annotation.NonNull;


/**
 * Constants for selecting the encoder of audio recordings.
 * https://developer.android.com/guide/topics/media/media-formats.html#audio-formats
 */
public enum AudioCodec implements Control {

    /**
     * Let the device choose its codec.
     */
    DEVICE_DEFAULT(0),

    /**
     * The AAC codec.
     */
    AAC(1),

    /**
     * The HE_AAC codec.
     */
    HE_AAC(2),

    /**
     * The AAC_ELD codec.
     */
    AAC_ELD(3);

    static final AudioCodec DEFAULT = DEVICE_DEFAULT;

    private int value;

    AudioCodec(int value) {
        this.value = value;
    }

    @NonNull
    static AudioCodec fromValue(int value) {
        AudioCodec[] list = AudioCodec.values();
        for (AudioCodec action : list) {
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
