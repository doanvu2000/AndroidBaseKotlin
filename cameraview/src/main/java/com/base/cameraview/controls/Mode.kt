package com.base.cameraview.controls


/**
 * Type of the session to be opened or to move to.
 * Session modes have influence over the capture and preview size, ability to shoot pictures,
 * focus modes, runtime permissions needed.
 */
enum class Mode(val value: Int) : Control {
    /**
     * Session used to capture pictures.
     *
     *
     * - [com.base.cameraview.CameraView.takeVideo] will throw an exception
     * - Only the camera permission is requested
     * - Capture size is chosen according to the current picture size selector
     */
    PICTURE(0),

    /**
     * Session used to capture videos.
     *
     *
     * - [com.base.cameraview.CameraView.takePicture] will throw an exception
     * - Camera and audio record permissions are requested
     * - Capture size is chosen according to the current video size selector
     */
    VIDEO(1);


    companion object {
        @JvmField
        val DEFAULT: Mode = PICTURE

        @JvmStatic
        fun fromValue(value: Int): Mode {
            val list = Mode.entries.toTypedArray()
            for (action in list) {
                if (action.value == value) {
                    return action
                }
            }
            return DEFAULT
        }
    }
}
