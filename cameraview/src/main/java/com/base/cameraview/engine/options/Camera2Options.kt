package com.base.cameraview.engine.options

import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
import android.util.Range
import com.base.cameraview.CameraOptions
import com.base.cameraview.controls.Flash
import com.base.cameraview.controls.Hdr
import com.base.cameraview.controls.PictureFormat
import com.base.cameraview.engine.mappers.Camera2Mapper.Companion.get
import com.base.cameraview.internal.CamcorderProfiles
import com.base.cameraview.size.AspectRatio
import kotlin.math.max
import kotlin.math.min

class Camera2Options(
    manager: CameraManager, cameraId: String, flipSizes: Boolean, pictureFormat: Int
) : CameraOptions() {
    init {
        val mapper = get()
        val cameraCharacteristics = manager.getCameraCharacteristics(cameraId)

        // Facing
        for (cameraId1 in manager.cameraIdList) {
            val cameraCharacteristics1 = manager.getCameraCharacteristics(cameraId1)
            val cameraFacing = cameraCharacteristics1.get(CameraCharacteristics.LENS_FACING)
            if (cameraFacing != null) {
                val value = mapper.unmapFacing(cameraFacing)
                if (value != null) supportedFacing.add(value)
            }
        }

        // WB
        val awbModes =
            cameraCharacteristics.get(CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES)
        for (awbMode in awbModes!!) {
            val value = mapper.unmapWhiteBalance(awbMode)
            if (value != null) supportedWhiteBalance.add(value)
        }

        // Flash
        supportedFlash.add(Flash.OFF)
        val hasFlash =
            cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)
        if (hasFlash != null && hasFlash) {
            val aeModes =
                cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES)
            for (aeMode in aeModes!!) {
                val flashes = mapper.unmapFlash(aeMode)
                supportedFlash.addAll(flashes)
            }
        }

        // HDR
        supportedHdr.add(Hdr.OFF)
        val sceneModes =
            cameraCharacteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES)
        for (sceneMode in sceneModes!!) {
            val value = mapper.unmapHdr(sceneMode)
            if (value != null) supportedHdr.add(value)
        }

        // Zoom
        val maxZoom =
            cameraCharacteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)
        if (maxZoom != null) {
            zoomSupported = maxZoom > 1
        }


        // AutoFocus
        // This now means 3A metering with respect to a specific region of the screen.
        // Some controls (AF, AE) have special triggers that might or might not be supported.
        // But they can also be on some continuous search mode so that the trigger is not needed.
        // What really matters in my opinion is the availability of regions.
        val afRegions =
            cameraCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF)
        val aeRegions =
            cameraCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE)
        val awbRegions =
            cameraCharacteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AWB)
        autoFocusSupported =
            (afRegions != null && afRegions > 0) || (aeRegions != null && aeRegions > 0) || (awbRegions != null && awbRegions > 0)

        // Exposure correction
        val exposureRange =
            cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE)
        val exposureStep =
            cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP)
        if (exposureRange != null && exposureStep != null && exposureStep.toFloat() != 0f) {
            exposureCorrectionMinValue = exposureRange.getLower()!! / exposureStep.toFloat()
            exposureCorrectionMaxValue = exposureRange.getUpper()!! / exposureStep.toFloat()
        }
        exposureCorrectionSupported =
            exposureCorrectionMinValue != 0f && exposureCorrectionMaxValue != 0f


        // Picture Sizes
        val streamMap = cameraCharacteristics.get(
            CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
        )
        if (streamMap == null) {
            throw RuntimeException("StreamConfigurationMap is null. Should not happen.")
        }
        val pictureFormats = streamMap.outputFormats
        var hasPictureFormat = false
        for (picFormat in pictureFormats) {
            if (picFormat == pictureFormat) {
                hasPictureFormat = true
                break
            }
        }
        check(hasPictureFormat) { "Picture format not supported: $pictureFormat" }
        val pSizes = streamMap.getOutputSizes(pictureFormat)
        for (size in pSizes) {
            val width = if (flipSizes) size.height else size.width
            val height = if (flipSizes) size.width else size.height
            supportedPictureSizes.add(com.base.cameraview.size.Size(width, height))
            supportedPictureAspectRatio.add(AspectRatio.of(width, height))
        }

        // Video Sizes
        // As a safety measure, remove Sizes bigger than CamcorderProfile.highest
        val profile = CamcorderProfiles.get(
            cameraId,
            com.base.cameraview.size.Size(Int.Companion.MAX_VALUE, Int.Companion.MAX_VALUE)
        )
        val videoMaxSize =
            com.base.cameraview.size.Size(profile.videoFrameWidth, profile.videoFrameHeight)
        val sizes = streamMap.getOutputSizes(MediaRecorder::class.java)
        for (size in sizes) {
            if (size.width <= videoMaxSize.width && size.height <= videoMaxSize.height) {
                val width = if (flipSizes) size.height else size.width
                val height = if (flipSizes) size.width else size.height
                supportedVideoSizes.add(com.base.cameraview.size.Size(width, height))
                supportedVideoAspectRatio.add(AspectRatio.of(width, height))
            }
        }

        // Preview FPS
        val range: Array<Range<Int>>? =
            cameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES) as Array<Range<Int>>?
        if (range != null) {
            previewFrameRateMinValue = Float.Companion.MAX_VALUE
            previewFrameRateMaxValue = -Float.Companion.MAX_VALUE
            for (fpsRange in range) {
                previewFrameRateMinValue =
                    min(previewFrameRateMinValue, fpsRange.getLower()!!.toFloat())
                previewFrameRateMaxValue =
                    max(previewFrameRateMaxValue, fpsRange.getUpper()!!.toFloat())
            }
        } else {
            previewFrameRateMinValue = 0f
            previewFrameRateMaxValue = 0f
        }

        // Picture formats
        supportedPictureFormats.add(PictureFormat.JPEG)
        val caps = cameraCharacteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
        if (caps != null) {
            for (cap in caps) {
                if (cap == CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) {
                    supportedPictureFormats.add(PictureFormat.DNG)
                }
            }
        }

        // Frame processing formats
        supportedFrameProcessingFormats.add(ImageFormat.YUV_420_888)
        val outputFormats = streamMap.outputFormats
        for (outputFormat in outputFormats) {
            // Ensure it is a raw format
            if (ImageFormat.getBitsPerPixel(outputFormat) > 0) {
                supportedFrameProcessingFormats.add(outputFormat)
            }
        }
    }
}
