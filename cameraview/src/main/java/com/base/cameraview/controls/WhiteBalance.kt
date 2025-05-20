package com.base.cameraview.controls


/**
 * White balance values control the white balance settings.
 */
enum class WhiteBalance(val value: Int) : Control {
    /**
     * Automatic white balance selection (AWB).
     * This is not guaranteed to be supported.
     *
     * @see com.base.cameraview.CameraOptions.getSupportedWhiteBalance
     */
    AUTO(0),

    /**
     * White balance appropriate for incandescent light.
     * This is not guaranteed to be supported.
     *
     * @see com.base.cameraview.CameraOptions.getSupportedWhiteBalance
     */
    INCANDESCENT(1),

    /**
     * White balance appropriate for fluorescent light.
     * This is not guaranteed to be supported.
     *
     * @see com.base.cameraview.CameraOptions.getSupportedWhiteBalance
     */
    FLUORESCENT(2),

    /**
     * White balance appropriate for daylight captures.
     * This is not guaranteed to be supported.
     *
     * @see com.base.cameraview.CameraOptions.getSupportedWhiteBalance
     */
    DAYLIGHT(3),

    /**
     * White balance appropriate for pictures in cloudy conditions.
     * This is not guaranteed to be supported.
     *
     * @see com.base.cameraview.CameraOptions.getSupportedWhiteBalance
     */
    CLOUDY(4);

    companion object {
        @JvmField
        val DEFAULT: WhiteBalance = AUTO

        @JvmStatic
        fun fromValue(value: Int): WhiteBalance {
            val list = WhiteBalance.entries.toTypedArray()
            for (action in list) {
                if (action.value == value) {
                    return action
                }
            }
            return DEFAULT
        }
    }
}