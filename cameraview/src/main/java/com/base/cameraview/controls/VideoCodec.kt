package com.base.cameraview.controls


/**
 * Constants for selecting the encoder of video recordings.
 * https://developer.android.com/guide/topics/media/media-formats.html#video-formats
 */
enum class VideoCodec(val value: Int) : Control {
    /**
     * Let the device choose its codec.
     */
    DEVICE_DEFAULT(0),

    /**
     * The H.263 codec.
     */
    H_263(1),

    /**
     * The H.264 codec.
     */
    H_264(2);

    companion object {
        @JvmField
        val DEFAULT: VideoCodec = DEVICE_DEFAULT

        @JvmStatic
        fun fromValue(value: Int): VideoCodec {
            val list = VideoCodec.entries.toTypedArray()
            for (action in list) {
                if (action.value == value) {
                    return action
                }
            }
            return DEFAULT
        }
    }
}
