package com.base.cameraview.engine.metering

import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.MeteringRectangle
import com.base.cameraview.CameraLogger
import com.base.cameraview.engine.offset.Angles
import com.base.cameraview.engine.offset.Axis
import com.base.cameraview.engine.offset.Reference
import com.base.cameraview.metering.MeteringTransform
import com.base.cameraview.size.AspectRatio
import com.base.cameraview.size.Size
import kotlin.math.roundToInt

class Camera2MeteringTransform(
    private val angles: Angles,
    private val previewSize: Size,
    private val previewStreamSize: Size,
    private val previewIsCropping: Boolean,
    private val characteristics: CameraCharacteristics,
    private val builder: CaptureRequest.Builder
) : MeteringTransform<MeteringRectangle?> {
    override fun transformMeteringRegion(region: RectF, weight: Int): MeteringRectangle {
        val round = Rect()
        region.round(round)
        return MeteringRectangle(round, weight)
    }

    override fun transformMeteringPoint(point: PointF): PointF {
        // This is a good Q/A. https://stackoverflow.com/a/33181620/4288782
        // At first, the point is relative to the View system and does not account
        // our own cropping. Will keep updating these two below.
        val referencePoint = PointF(point.x, point.y)
        var referenceSize = previewSize

        // 1. Account for cropping.
        // This will enlarge the preview size so that aspect ratio matches.
        referenceSize = applyPreviewCropping(referenceSize, referencePoint)

        // 2. Scale to the preview stream coordinates.
        // This will move to the preview stream coordinates by scaling.
        referenceSize = applyPreviewScale(referenceSize, referencePoint)

        // 3. Rotate to the stream coordinate system.
        // This leaves us with sensor stream coordinates.
        referenceSize = applyPreviewToSensorRotation(referenceSize, referencePoint)

        // 4. Move to the crop region coordinate system.
        // The crop region is the union of all currently active streams.
        referenceSize = applyCropRegionCoordinates(referenceSize, referencePoint)

        // 5. Move to the active array coordinate system.
        referenceSize = applyActiveArrayCoordinates(referenceSize, referencePoint)
        LOG.i("input:", point, "output (before clipping):", referencePoint)

        // 6. Probably not needed, but make sure we clip.
        if (referencePoint.x < 0) referencePoint.x = 0f
        if (referencePoint.y < 0) referencePoint.y = 0f
        if (referencePoint.x > referenceSize.width) referencePoint.x =
            referenceSize.width.toFloat()
        if (referencePoint.y > referenceSize.height) referencePoint.y =
            referenceSize.height.toFloat()
        LOG.i("input:", point, "output (after clipping):", referencePoint)
        return referencePoint
    }


    private fun applyPreviewCropping(referenceSize: Size, referencePoint: PointF): Size {
        val previewStreamSize = this.previewStreamSize
        val previewSurfaceSize = referenceSize
        var referenceWidth = previewSurfaceSize.width
        var referenceHeight = previewSurfaceSize.height
        val previewStreamAspectRatio = AspectRatio.of(previewStreamSize)
        val previewSurfaceAspectRatio = AspectRatio.of(previewSurfaceSize)
        if (previewIsCropping) {
            if (previewStreamAspectRatio.toFloat() > previewSurfaceAspectRatio.toFloat()) {
                // Stream is larger. The x coordinate must be increased: a touch on the left side
                // of the surface is not on the left size of stream (it's more to the right).
                val scale = (previewStreamAspectRatio.toFloat()
                        / previewSurfaceAspectRatio.toFloat())
                referencePoint.x += previewSurfaceSize.width * (scale - 1f) / 2f
                referenceWidth = (previewSurfaceSize.width * scale).roundToInt()
            } else {
                // Stream is taller. The y coordinate must be increased: a touch on the top side
                // of the surface is not on the top size of stream (it's a bit lower).
                val scale = (previewSurfaceAspectRatio.toFloat()
                        / previewStreamAspectRatio.toFloat())
                referencePoint.y += previewSurfaceSize.height * (scale - 1f) / 2f
                referenceHeight = (previewSurfaceSize.height * scale).roundToInt()
            }
        }
        return Size(referenceWidth, referenceHeight)
    }

    private fun applyPreviewScale(referenceSize: Size, referencePoint: PointF): Size {
        // The referenceSize how has the same aspect ratio of the previewStreamSize, but they
        // can still have different size (that is, a scale operation is needed).
        val previewStreamSize = this.previewStreamSize
        referencePoint.x *= previewStreamSize.width.toFloat() / referenceSize.width
        referencePoint.y *= previewStreamSize.height.toFloat() / referenceSize.height
        return previewStreamSize
    }

    private fun applyPreviewToSensorRotation(
        referenceSize: Size,
        referencePoint: PointF
    ): Size {
        // Not elegant, but the sin/cos way was failing for some reason.
        val angle = angles.offset(Reference.SENSOR, Reference.VIEW, Axis.ABSOLUTE)
        val flip = angle % 180 != 0
        val tempX = referencePoint.x
        val tempY = referencePoint.y
        when (angle) {
            0 -> {
                referencePoint.x = tempX
                referencePoint.y = tempY
            }

            90 -> {
                referencePoint.x = tempY
                referencePoint.y = referenceSize.width - tempX
            }

            180 -> {
                referencePoint.x = referenceSize.width - tempX
                referencePoint.y = referenceSize.height - tempY
            }

            270 -> {
                referencePoint.x = referenceSize.height - tempY
                referencePoint.y = tempX
            }

            else -> {
                throw IllegalStateException("Unexpected angle $angle")
            }
        }
        return if (flip) referenceSize.flip() else referenceSize
    }

    private fun applyCropRegionCoordinates(
        referenceSize: Size,
        referencePoint: PointF
    ): Size {
        // The input point and size refer to the stream rect.
        // The stream rect is part of the 'crop region', as described below.
        // https://source.android.com/devices/camera/camera3_crop_reprocess.html
        val cropRect = builder.get(CaptureRequest.SCALER_CROP_REGION)
        // For now we don't care about x and y position. Rect should not be null, but let's be safe.
        val cropRectWidth = cropRect?.width() ?: referenceSize.width
        val cropRectHeight = cropRect?.height() ?: referenceSize.height
        // The stream is always centered inside the crop region, and one of the dimensions
        // should always match. We just increase the other one.
        referencePoint.x += (cropRectWidth - referenceSize.width) / 2f
        referencePoint.y += (cropRectHeight - referenceSize.height) / 2f
        return Size(cropRectWidth, cropRectHeight)
    }

    private fun applyActiveArrayCoordinates(
        referenceSize: Size,
        referencePoint: PointF
    ): Size {
        // The input point and size refer to the scaler crop region.
        // We can query for the crop region position inside the active array, so this is easy.
        val cropRect = builder.get(CaptureRequest.SCALER_CROP_REGION)
        referencePoint.x += (cropRect?.left ?: 0).toFloat()
        referencePoint.y += (cropRect?.top ?: 0).toFloat()
        // Finally, get the active rect width and height from characteristics.
        var activeRect =
            characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE)
        if (activeRect == null) { // Should never happen
            activeRect = Rect(
                0, 0, referenceSize.width,
                referenceSize.height
            )
        }
        return Size(activeRect.width(), activeRect.height())
    }

    companion object {
        private val TAG: String = Camera2MeteringTransform::class.java.simpleName
        private val LOG: CameraLogger = CameraLogger.create(TAG)
    }
}
