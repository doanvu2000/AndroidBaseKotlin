package com.base.cameraview.picture

import android.hardware.Camera
import androidx.exifinterface.media.ExifInterface
import com.base.cameraview.PictureResult
import com.base.cameraview.engine.Camera1Engine
import com.base.cameraview.engine.offset.Reference
import com.base.cameraview.engine.orchestrator.CameraState
import com.base.cameraview.internal.ExifHelper.getOrientation
import java.io.ByteArrayInputStream
import java.io.IOException

/**
 * A [PictureResult] that uses standard APIs.
 */
class Full1PictureRecorder(
    stub: PictureResult.Stub,
    private val mEngine: Camera1Engine,
    private val mCamera: Camera
) : FullPictureRecorder(
    stub,
    mEngine
) {
    init {
        // We set the rotation to the camera parameters, but we don't know if the result will be
        // already rotated with 0 exif, or original with non zero exif. we will have to read EXIF.
        val params = mCamera.parameters
        params.setRotation(mResult?.rotation ?: 0)
        mCamera.parameters = params
    }

    override fun take() {
        LOG.i("take() called.")
        // Stopping the preview callback is important on older APIs / emulators,
        // or takePicture can hang and leave the camera in a bad state.
        mCamera.setPreviewCallbackWithBuffer(null)
        mEngine.frameManager.release()
        try {
            mCamera.takePicture(
                {
                    LOG.i("take(): got onShutter callback.")
                    dispatchOnShutter(true)
                },
                null,
                null
            ) { data, camera ->
                LOG.i("take(): got picture callback.")
                var exifRotation: Int
                try {
                    val exif = ExifInterface(ByteArrayInputStream(data))
                    val exifOrientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    exifRotation = getOrientation(exifOrientation)
                } catch (e: IOException) {
                    exifRotation = 0
                }
                mResult?.data = data
                mResult?.rotation = exifRotation
                LOG.i("take(): starting preview again. ", Thread.currentThread())

                // It's possible that by the time this callback is invoked, we're not previewing
                // anymore, so check before restarting preview.
                if (mEngine.state.isAtLeast(CameraState.PREVIEW)) {
                    camera.setPreviewCallbackWithBuffer(mEngine)
                    val previewStreamSize = mEngine.getPreviewStreamSize(Reference.SENSOR)
                    checkNotNull(previewStreamSize) {
                        "Preview stream size " +
                                "should never be null here."
                    }
                    // Need to re-setup the frame manager, otherwise no frames are processed
                    // after takePicture() is called
                    mEngine.frameManager.setUp(
                        mEngine.frameProcessingFormat,
                        previewStreamSize,
                        mEngine.angles
                    )
                    camera.startPreview()
                }
                dispatchResult()
            }
            LOG.i("take() returned.")
        } catch (e: Exception) {
            mError = e
            dispatchResult()
        }
    }

    override fun dispatchResult() {
        LOG.i("dispatching result. Thread:", Thread.currentThread())
        super.dispatchResult()
    }
}
