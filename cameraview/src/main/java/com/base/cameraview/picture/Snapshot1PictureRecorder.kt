package com.base.cameraview.picture

import android.graphics.YuvImage
import android.hardware.Camera
import com.base.cameraview.PictureResult
import com.base.cameraview.engine.Camera1Engine
import com.base.cameraview.engine.offset.Reference
import com.base.cameraview.internal.CropHelper
import com.base.cameraview.internal.RotationHelper
import com.base.cameraview.internal.WorkerHandler
import com.base.cameraview.size.AspectRatio
import com.base.cameraview.size.Size
import java.io.ByteArrayOutputStream

/**
 * A [PictureRecorder] that uses standard APIs.
 */
class Snapshot1PictureRecorder(
    stub: PictureResult.Stub,
    engine: Camera1Engine,
    camera: Camera,
    outputRatio: AspectRatio
) : SnapshotPictureRecorder(stub, engine) {
    private var mEngine1: Camera1Engine?
    private var mCamera: Camera?
    private var mOutputRatio: AspectRatio?
    private var mFormat: Int

    init {
        mEngine1 = engine
        mCamera = camera
        mOutputRatio = outputRatio
        mFormat = camera.parameters.previewFormat
    }

    override fun take() {
        mCamera!!.setOneShotPreviewCallback { yuv, camera ->
            dispatchOnShutter(false)

            // Got to rotate the preview frame, since byte[] data here does not include
            // EXIF tags automatically set by camera. So either we add EXIF, or we rotate.
            // Adding EXIF to a byte array, unfortunately, is hard.
            val sensorToOutput = mResult!!.rotation
            val outputSize = mResult!!.size
            val previewStreamSize = mEngine1!!.getPreviewStreamSize(Reference.SENSOR)
            checkNotNull(previewStreamSize) {
                "Preview stream size " +
                        "should never be null here."
            }
            WorkerHandler.execute { // Rotate the picture, because no one will write EXIF data,
                // then crop if needed. In both cases, transform yuv to jpeg.
                var data = RotationHelper.rotate(yuv, previewStreamSize, sensorToOutput)
                val yuv = YuvImage(
                    data, mFormat, outputSize.width,
                    outputSize.height, null
                )

                val stream = ByteArrayOutputStream()
                val outputRect = CropHelper.computeCrop(outputSize, mOutputRatio!!)
                yuv.compressToJpeg(outputRect, 90, stream)
                data = stream.toByteArray()

                mResult!!.data = data
                mResult!!.size = Size(outputRect.width(), outputRect.height())
                mResult!!.rotation = 0
                dispatchResult()
            }

            // It seems that the buffers are already cleared here, so we need to allocate again.
            camera.setPreviewCallbackWithBuffer(null) // Release anything left
            camera.setPreviewCallbackWithBuffer(mEngine1) // Add ourselves
            mEngine1!!.frameManager
                .setUp(mFormat, previewStreamSize, mEngine1!!.angles)
        }
    }

    override fun dispatchResult() {
        mEngine1 = null
        mCamera = null
        mOutputRatio = null
        mFormat = 0
        super.dispatchResult()
    }
}
