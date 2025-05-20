package com.base.cameraview.controls


/**
 * Constants for selecting the encoder of audio recordings.
 * [...](https://developer.android.com/guide/topics/media/media-formats.html#audio-formats)
 */
enum class AudioCodec(val value: Int) : Control {
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

    companion object {
        @JvmField
        val DEFAULT: AudioCodec = DEVICE_DEFAULT

        @JvmStatic
        fun fromValue(value: Int): AudioCodec {
            val list = AudioCodec.entries.toTypedArray()
            for (action in list) {
                if (action.value == value) {
                    return action
                }
            }
            return DEFAULT
        }
    }
}
