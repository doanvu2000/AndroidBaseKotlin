package com.base.cameraview.controls

/**
 * Audio values indicate whether to record audio stream when record video.
 */
enum class Audio(val value: Int) : Control {
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

    companion object {
        @JvmField
        val DEFAULT: Audio = Audio.ON

        @JvmStatic
        fun fromValue(value: Int): Audio {
            val list = Audio.entries.toTypedArray()
            for (action in list) {
                if (action.value == value) {
                    return action
                }
            }
            return DEFAULT
        }
    }
}
