package com.base.cameraview.engine.options;

import static android.hardware.camera2.CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AE_COMPENSATION_RANGE;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AVAILABLE_SCENE_MODES;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_AWB_AVAILABLE_MODES;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_MAX_REGIONS_AE;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_MAX_REGIONS_AF;
import static android.hardware.camera2.CameraCharacteristics.CONTROL_MAX_REGIONS_AWB;
import static android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE;
import static android.hardware.camera2.CameraCharacteristics.LENS_FACING;
import static android.hardware.camera2.CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES;
import static android.hardware.camera2.CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM;
import static android.hardware.camera2.CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP;

import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.util.Range;
import android.util.Rational;

import androidx.annotation.NonNull;

import com.base.cameraview.CameraOptions;
import com.base.cameraview.controls.Facing;
import com.base.cameraview.controls.Flash;
import com.base.cameraview.controls.Hdr;
import com.base.cameraview.controls.PictureFormat;
import com.base.cameraview.controls.WhiteBalance;
import com.base.cameraview.engine.mappers.Camera2Mapper;
import com.base.cameraview.internal.CamcorderProfiles;
import com.base.cameraview.size.AspectRatio;
import com.base.cameraview.size.Size;

import java.util.Set;

public class Camera2Options extends CameraOptions {

    public Camera2Options(@NonNull CameraManager manager,
                          @NonNull String cameraId,
                          boolean flipSizes,
                          int pictureFormat) throws CameraAccessException {
        Camera2Mapper mapper = Camera2Mapper.get();
        CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraId);

        // Facing
        for (String cameraId1 : manager.getCameraIdList()) {
            CameraCharacteristics cameraCharacteristics1 = manager
                    .getCameraCharacteristics(cameraId1);
            Integer cameraFacing = cameraCharacteristics1.get(LENS_FACING);
            if (cameraFacing != null) {
                Facing value = mapper.unmapFacing(cameraFacing);
                if (value != null) supportedFacing.add(value);
            }
        }

        // WB
        int[] awbModes = cameraCharacteristics.get(CONTROL_AWB_AVAILABLE_MODES);
        //noinspection ConstantConditions
        for (int awbMode : awbModes) {
            WhiteBalance value = mapper.unmapWhiteBalance(awbMode);
            if (value != null) supportedWhiteBalance.add(value);
        }

        // Flash
        supportedFlash.add(Flash.OFF);
        Boolean hasFlash = cameraCharacteristics.get(FLASH_INFO_AVAILABLE);
        if (hasFlash != null && hasFlash) {
            int[] aeModes = cameraCharacteristics.get(CONTROL_AE_AVAILABLE_MODES);
            //noinspection ConstantConditions
            for (int aeMode : aeModes) {
                Set<Flash> flashes = mapper.unmapFlash(aeMode);
                supportedFlash.addAll(flashes);
            }
        }

        // HDR
        supportedHdr.add(Hdr.OFF);
        int[] sceneModes = cameraCharacteristics.get(CONTROL_AVAILABLE_SCENE_MODES);
        //noinspection ConstantConditions
        for (int sceneMode : sceneModes) {
            Hdr value = mapper.unmapHdr(sceneMode);
            if (value != null) supportedHdr.add(value);
        }

        // Zoom
        Float maxZoom = cameraCharacteristics.get(SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
        if (maxZoom != null) {
            zoomSupported = maxZoom > 1;
        }


        // AutoFocus
        // This now means 3A metering with respect to a specific region of the screen.
        // Some controls (AF, AE) have special triggers that might or might not be supported.
        // But they can also be on some continuous search mode so that the trigger is not needed.
        // What really matters in my opinion is the availability of regions.
        Integer afRegions = cameraCharacteristics.get(CONTROL_MAX_REGIONS_AF);
        Integer aeRegions = cameraCharacteristics.get(CONTROL_MAX_REGIONS_AE);
        Integer awbRegions = cameraCharacteristics.get(CONTROL_MAX_REGIONS_AWB);
        autoFocusSupported = (afRegions != null && afRegions > 0)
                || (aeRegions != null && aeRegions > 0)
                || (awbRegions != null && awbRegions > 0);

        // Exposure correction
        Range<Integer> exposureRange = cameraCharacteristics.get(CONTROL_AE_COMPENSATION_RANGE);
        Rational exposureStep = cameraCharacteristics.get(CONTROL_AE_COMPENSATION_STEP);
        if (exposureRange != null && exposureStep != null && exposureStep.floatValue() != 0) {
            exposureCorrectionMinValue = exposureRange.getLower() / exposureStep.floatValue();
            exposureCorrectionMaxValue = exposureRange.getUpper() / exposureStep.floatValue();
        }
        exposureCorrectionSupported = exposureCorrectionMinValue != 0
                && exposureCorrectionMaxValue != 0;


        // Picture Sizes
        StreamConfigurationMap streamMap = cameraCharacteristics.get(
                SCALER_STREAM_CONFIGURATION_MAP);
        if (streamMap == null) {
            throw new RuntimeException("StreamConfigurationMap is null. Should not happen.");
        }
        int[] pictureFormats = streamMap.getOutputFormats();
        boolean hasPictureFormat = false;
        for (int picFormat : pictureFormats) {
            if (picFormat == pictureFormat) {
                hasPictureFormat = true;
                break;
            }
        }
        if (!hasPictureFormat) {
            throw new IllegalStateException("Picture format not supported: " + pictureFormat);
        }
        android.util.Size[] psizes = streamMap.getOutputSizes(pictureFormat);
        for (android.util.Size size : psizes) {
            int width = flipSizes ? size.getHeight() : size.getWidth();
            int height = flipSizes ? size.getWidth() : size.getHeight();
            supportedPictureSizes.add(new Size(width, height));
            supportedPictureAspectRatio.add(AspectRatio.of(width, height));
        }

        // Video Sizes
        // As a safety measure, remove Sizes bigger than CamcorderProfile.highest
        CamcorderProfile profile = CamcorderProfiles.get(cameraId,
                new Size(Integer.MAX_VALUE, Integer.MAX_VALUE));
        Size videoMaxSize = new Size(profile.videoFrameWidth, profile.videoFrameHeight);
        android.util.Size[] vsizes = streamMap.getOutputSizes(MediaRecorder.class);
        for (android.util.Size size : vsizes) {
            if (size.getWidth() <= videoMaxSize.getWidth()
                    && size.getHeight() <= videoMaxSize.getHeight()) {
                int width = flipSizes ? size.getHeight() : size.getWidth();
                int height = flipSizes ? size.getWidth() : size.getHeight();
                supportedVideoSizes.add(new Size(width, height));
                supportedVideoAspectRatio.add(AspectRatio.of(width, height));
            }
        }

        // Preview FPS
        Range<Integer>[] range = cameraCharacteristics.get(CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
        if (range != null) {
            previewFrameRateMinValue = Float.MAX_VALUE;
            previewFrameRateMaxValue = -Float.MAX_VALUE;
            for (Range<Integer> fpsRange : range) {
                previewFrameRateMinValue = Math.min(previewFrameRateMinValue, fpsRange.getLower());
                previewFrameRateMaxValue = Math.max(previewFrameRateMaxValue, fpsRange.getUpper());
            }
        } else {
            previewFrameRateMinValue = 0F;
            previewFrameRateMaxValue = 0F;
        }

        // Picture formats
        supportedPictureFormats.add(PictureFormat.JPEG);
        int[] caps = cameraCharacteristics.get(REQUEST_AVAILABLE_CAPABILITIES);
        if (caps != null) {
            for (int cap : caps) {
                if (cap == CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) {
                    supportedPictureFormats.add(PictureFormat.DNG);
                }
            }
        }

        // Frame processing formats
        supportedFrameProcessingFormats.add(ImageFormat.YUV_420_888);
        int[] outputFormats = streamMap.getOutputFormats();
        for (int outputFormat : outputFormats) {
            // Ensure it is a raw format
            if (ImageFormat.getBitsPerPixel(outputFormat) > 0) {
                supportedFrameProcessingFormats.add(outputFormat);
            }
        }
    }
}
