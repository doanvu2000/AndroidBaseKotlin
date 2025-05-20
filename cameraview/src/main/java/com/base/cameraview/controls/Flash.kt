package com.base.cameraview.controls

/**
 * Flash value indicates the flash mode to be used.
 */
enum class Flash(val value: Int) : Control {
    /**
     * Flash is always off.
     */
    OFF(0),

    /**
     * Flash will be on when capturing.
     * This is not guaranteed to be supported.
     *
     * @see com.base.cameraview.CameraOptions.getSupportedFlash
     */
    ON(1),


    /**
     * Flash mode is chosen by the camera.
     * This is not guaranteed to be supported.
     *
     * @see com.base.cameraview.CameraOptions.getSupportedFlash
     */
    AUTO(2),


    /**
     * Flash is always on, working as a torch.
     * This is not guaranteed to be supported.
     *
     * @see com.base.cameraview.CameraOptions.getSupportedFlash
     */
    TORCH(3);

    companion object {
        @JvmField
        val DEFAULT: Flash = OFF

        @JvmStatic
        fun fromValue(value: Int): Flash {
            val list = Flash.entries.toTypedArray()
            for (action in list) {
                if (action.value == value) {
                    return action
                }
            }
            return DEFAULT
        }
    }
}
