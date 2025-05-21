package com.base.cameraview

/**
 * Holds an error with the camera configuration.
 */
class CameraException : RuntimeException {
    var reason: Int = REASON_UNKNOWN
        private set

    constructor(cause: Throwable?) : super(cause)

    constructor(cause: Throwable?, reason: Int) : super(cause) {
        this.reason = reason
    }

    constructor(reason: Int) : super() {
        this.reason = reason
    }

    @get:Suppress("unused")
    val isUnrecoverable: Boolean
        /**
         * Whether this error is unrecoverable. If this function returns true,
         * the Camera has been closed (or will be soon) and it is likely showing a black preview.
         * This is the right moment to show an error dialog to the user.
         *
         * @return true if this error is unrecoverable
         */
        get() = when (this.reason) {
            REASON_FAILED_TO_CONNECT -> true
            REASON_FAILED_TO_START_PREVIEW -> true
            REASON_DISCONNECTED -> true
            else -> false
        }

    companion object {
        /**
         * Unknown error. No further info available.
         */
        const val REASON_UNKNOWN: Int = 0

        /**
         * We failed to connect to the camera service.
         * The camera might be in use by another app.
         */
        const val REASON_FAILED_TO_CONNECT: Int = 1

        /**
         * Failed to start the camera preview.
         * Again, the camera might be in use by another app.
         */
        const val REASON_FAILED_TO_START_PREVIEW: Int = 2

        /**
         * Camera was forced to disconnect.
         * In Camera1, this is thrown when android.hardware.Camera.CAMERA_ERROR_EVICTED
         * is caught.
         */
        const val REASON_DISCONNECTED: Int = 3

        /**
         * Could not take a picture or a picture snapshot,
         * for some not specified reason.
         */
        const val REASON_PICTURE_FAILED: Int = 4

        /**
         * Could not take a video or a video snapshot,
         * for some not specified reason.
         */
        const val REASON_VIDEO_FAILED: Int = 5

        /**
         * value.
         * This can be solved by changing the facing value and starting again.
         */
        const val REASON_NO_CAMERA: Int = 6
    }
}
