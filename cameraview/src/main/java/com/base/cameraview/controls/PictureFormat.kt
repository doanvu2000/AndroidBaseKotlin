package com.base.cameraview.controls


enum class PictureFormat(val value: Int) : Control {
    /**
     * The picture result data will be a JPEG file.
     * This value is always supported.
     */
    JPEG(0),

    /**
     * The picture result data will be a DNG file.
     * This is only supported with the [Engine.CAMERA2] engine and only on
     */
    DNG(1);

    companion object {
        @JvmField
        val DEFAULT: PictureFormat = JPEG

        @JvmStatic
        fun fromValue(value: Int): PictureFormat {
            val list = PictureFormat.entries.toTypedArray()
            for (action in list) {
                if (action.value == value) {
                    return action
                }
            }
            return DEFAULT
        }
    }
}
