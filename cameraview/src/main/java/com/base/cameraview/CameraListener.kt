package com.base.cameraview

import android.graphics.PointF
import androidx.annotation.UiThread

@Suppress("unused")
abstract class CameraListener {
    /**
     * Notifies that the camera was opened.
     * The [CameraOptions] object collects all supported options by the current camera.
     *
     * @param options camera supported options
     */
    @UiThread
    open fun onCameraOpened(options: CameraOptions) {
    }

    /**
     * Notifies that the camera session was closed.
     */
    @UiThread
    open fun onCameraClosed() {
    }

    /**
     * Notifies about an error during the camera setup or configuration.
     *
     *
     * At this point you should inspect the [CameraException] reason using
     * [CameraException.getReason] and see what should be done, if anything.
     * If the error is unrecoverable, this is the right moment to show an error dialog, for example.
     *
     * @param exception the error
     */
    @UiThread
    open fun onCameraError(exception: CameraException) {
    }

    /**
     * Notifies that a picture previously captured with [CameraView.takePicture]
     * or [CameraView.takePictureSnapshot] is ready to be shown or saved to file.
     *
     *
     * If planning to show a bitmap, you can use
     * to decode the byte array
     * taking care about orientation and threading.
     *
     * @param result captured picture
     */
    @UiThread
    open fun onPictureTaken(result: PictureResult) {
    }

    /**
     * Notifies that a video capture has just ended.
     *
     * @param result the video result
     */
    @UiThread
    open fun onVideoTaken(result: VideoResult) {
    }

    /**
     * Notifies that the device was tilted or the window offset changed.
     * The orientation passed is exactly the counter-clockwise rotation that a View should have,
     * in order to appear correctly oriented to the user, considering the way she is
     * holding the device, and the native activity orientation.
     *
     *
     * This is meant to be used for aligning views (e.g. buttons) to the current camera viewport.
     *
     * @param orientation either 0, 90, 180 or 270
     */
    @UiThread
    fun onOrientationChanged(orientation: Int) {
    }

    /**
     * Notifies that user interacted with the screen and started metering with a gesture,
     * and touch metering routine is trying to focus around that area.
     * This callback can be used to draw things on screen.
     * Can also be triggered by [CameraView.startAutoFocus].
     *
     * @param point coordinates with respect to CameraView.getWidth() and CameraView.getHeight()
     */
    @UiThread
    fun onAutoFocusStart(point: PointF) {
    }

    /**
     * Notifies that a touch metering event just ended, and the camera converged
     * to a new focus, exposure and possibly white balance.
     * This might succeed or not.
     * Can also be triggered by [CameraView.startAutoFocus].
     *
     * @param successful whether metering succeeded
     * @param point      coordinates with respect to CameraView.getWidth() and CameraView.getHeight()
     */
    @UiThread
    fun onAutoFocusEnd(successful: Boolean, point: PointF) {
    }

    /**
     * Notifies that a finger gesture just caused the camera zoom
     * to be changed. This can be used to draw, for example, a seek bar.
     *
     * @param newValue the new zoom value
     * @param bounds   min and max bounds for newValue (fixed to 0 ... 1)
     * @param fingers  finger positions that caused the event, null if not caused by touch
     */
    @UiThread
    fun onZoomChanged(
        newValue: Float,
        bounds: FloatArray,
        fingers: Array<PointF?>?
    ) {
    }

    /**
     * Noitifies that a finger gesture just caused the camera exposure correction
     * to be changed. This can be used to draw, for example, a seek bar.
     *
     * @param newValue the new correction value
     * @param bounds   min and max bounds for newValue, as returned by [CameraOptions]
     * @param fingers  finger positions that caused the event, null if not caused by touch
     */
    @UiThread
    fun onExposureCorrectionChanged(
        newValue: Float,
        bounds: FloatArray,
        fingers: Array<PointF?>?
    ) {
    }

    /**
     * Notifies that the actual video recording has started.
     * This is the time when actual frames recording starts.
     *
     *
     * This can be used to show some UI indicator for video recording or counting time.
     *
     * @see .onVideoRecordingEnd
     */
    @UiThread
    open fun onVideoRecordingStart() {
    }

    /**
     * Notifies that the actual video recording has ended.
     * At this point recording has ended, though the file might still be processed.
     * The [.onVideoTaken] callback will be called soon.
     *
     *
     * This can be used to remove UI indicators for video recording.
     *
     * @see .onVideoRecordingStart
     */
    @UiThread
    open fun onVideoRecordingEnd() {
    }

    /**
     * Notifies that the picture capture has started. Can be used to update the UI for visual
     * confirmation or sound effects.
     */
    @UiThread
    open fun onPictureShutter() {
    }
}
