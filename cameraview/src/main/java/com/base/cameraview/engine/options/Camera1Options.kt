package com.base.cameraview.engine.options

import android.graphics.ImageFormat
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import com.base.cameraview.CameraOptions
import com.base.cameraview.controls.Flash
import com.base.cameraview.controls.Hdr
import com.base.cameraview.controls.PictureFormat
import com.base.cameraview.engine.mappers.Camera1Mapper.Companion.get
import com.base.cameraview.internal.CamcorderProfiles
import com.base.cameraview.size.AspectRatio
import com.base.cameraview.size.Size
import kotlin.math.max
import kotlin.math.min

class Camera1Options(params: Camera.Parameters, cameraId: Int, flipSizes: Boolean) :
    CameraOptions() {
    init {
        val mapper = get()

        // Facing
        val cameraInfo = CameraInfo()
        var i = 0
        val count = Camera.getNumberOfCameras()
        while (i < count) {
            Camera.getCameraInfo(i, cameraInfo)
            val value = mapper.unmapFacing(cameraInfo.facing)
            if (value != null) supportedFacing.add(value)
            i++
        }

        // WB
        var strings = params.supportedWhiteBalance
        if (strings != null) {
            for (string in strings) {
                val value = mapper.unmapWhiteBalance(string)
                if (value != null) supportedWhiteBalance.add(value)
            }
        }

        // Flash
        supportedFlash.add(Flash.OFF)
        strings = params.supportedFlashModes
        if (strings != null) {
            for (string in strings) {
                val value = mapper.unmapFlash(string)
                if (value != null) supportedFlash.add(value)
            }
        }

        // Hdr
        supportedHdr.add(Hdr.OFF)
        strings = params.supportedSceneModes
        if (strings != null) {
            for (string in strings) {
                val value = mapper.unmapHdr(string)
                if (value != null) supportedHdr.add(value)
            }
        }

        // zoom
        zoomSupported = params.isZoomSupported

        // autofocus
        autoFocusSupported = params.supportedFocusModes
            .contains(Camera.Parameters.FOCUS_MODE_AUTO)

        // Exposure correction
        val step = params.exposureCompensationStep
        exposureCorrectionMinValue = params.minExposureCompensation.toFloat() * step
        exposureCorrectionMaxValue = params.maxExposureCompensation.toFloat() * step
        exposureCorrectionSupported = params.minExposureCompensation != 0
                || params.maxExposureCompensation != 0

        // Picture Sizes
        val sizes = params.supportedPictureSizes
        for (size in sizes) {
            val width = if (flipSizes) size.height else size.width
            val height = if (flipSizes) size.width else size.height
            supportedPictureSizes.add(Size(width, height))
            supportedPictureAspectRatio.add(AspectRatio.of(width, height))
        }

        // Video Sizes
        // As a safety measure, remove Sizes bigger than CamcorderProfile.highest
        val profile = CamcorderProfiles.get(
            cameraId,
            Size(Int.Companion.MAX_VALUE, Int.Companion.MAX_VALUE)
        )
        val videoMaxSize = Size(profile.videoFrameWidth, profile.videoFrameHeight)
        val vsizes = params.supportedVideoSizes
        if (vsizes != null) {
            for (size in vsizes) {
                if (size.width <= videoMaxSize.width
                    && size.height <= videoMaxSize.height
                ) {
                    val width = if (flipSizes) size.height else size.width
                    val height = if (flipSizes) size.width else size.height
                    supportedVideoSizes.add(Size(width, height))
                    supportedVideoAspectRatio.add(AspectRatio.of(width, height))
                }
            }
        } else {
            // StackOverflow threads seems to agree that if getSupportedVideoSizes is null,
            // previews can be used.
            val fallback = params.supportedPreviewSizes
            for (size in fallback) {
                if (size.width <= videoMaxSize.width
                    && size.height <= videoMaxSize.height
                ) {
                    val width = if (flipSizes) size.height else size.width
                    val height = if (flipSizes) size.width else size.height
                    supportedVideoSizes.add(Size(width, height))
                    supportedVideoAspectRatio.add(AspectRatio.of(width, height))
                }
            }
        }

        // Preview FPS
        previewFrameRateMinValue = Float.Companion.MAX_VALUE
        previewFrameRateMaxValue = -Float.Companion.MAX_VALUE
        val fpsRanges = params.supportedPreviewFpsRange
        for (fpsRange in fpsRanges) {
            val lower = fpsRange[0].toFloat() / 1000f
            val upper = fpsRange[1].toFloat() / 1000f
            previewFrameRateMinValue = min(previewFrameRateMinValue, lower)
            previewFrameRateMaxValue = max(previewFrameRateMaxValue, upper)
        }

        // Picture formats
        supportedPictureFormats.add(PictureFormat.JPEG)

        // Frame processing formats
        supportedFrameProcessingFormats.add(ImageFormat.NV21)
    }
}
