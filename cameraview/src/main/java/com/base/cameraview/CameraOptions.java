package com.base.cameraview;


import android.graphics.ImageFormat;

import androidx.annotation.NonNull;

import com.base.cameraview.controls.Audio;
import com.base.cameraview.controls.AudioCodec;
import com.base.cameraview.controls.Control;
import com.base.cameraview.controls.Engine;
import com.base.cameraview.controls.Facing;
import com.base.cameraview.controls.Flash;
import com.base.cameraview.controls.Grid;
import com.base.cameraview.controls.Hdr;
import com.base.cameraview.controls.Mode;
import com.base.cameraview.controls.PictureFormat;
import com.base.cameraview.controls.Preview;
import com.base.cameraview.controls.VideoCodec;
import com.base.cameraview.controls.WhiteBalance;
import com.base.cameraview.gesture.GestureAction;
import com.base.cameraview.size.AspectRatio;
import com.base.cameraview.size.Size;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Options telling you what is available and what is not.
 */
public abstract class CameraOptions {

    protected Set<WhiteBalance> supportedWhiteBalance = new HashSet<>(5);
    protected Set<Facing> supportedFacing = new HashSet<>(2);
    protected Set<Flash> supportedFlash = new HashSet<>(4);
    protected Set<Hdr> supportedHdr = new HashSet<>(2);
    protected Set<Size> supportedPictureSizes = new HashSet<>(15);
    protected Set<Size> supportedVideoSizes = new HashSet<>(5);
    protected Set<AspectRatio> supportedPictureAspectRatio = new HashSet<>(4);
    protected Set<AspectRatio> supportedVideoAspectRatio = new HashSet<>(3);
    protected Set<PictureFormat> supportedPictureFormats = new HashSet<>(2);
    protected Set<Integer> supportedFrameProcessingFormats = new HashSet<>(2);

    protected boolean zoomSupported;
    protected boolean exposureCorrectionSupported;
    protected float exposureCorrectionMinValue;
    protected float exposureCorrectionMaxValue;
    protected boolean autoFocusSupported;
    protected float previewFrameRateMinValue;
    protected float previewFrameRateMaxValue;

    protected CameraOptions() {
    }

    /**
     * Shorthand for getSupported*().contains(value).
     *
     * @param control value to check
     * @return whether it's supported
     */
    public final boolean supports(@NonNull Control control) {
        return getSupportedControls(control.getClass()).contains(control);
    }

    /**
     * Shorthand for other methods in this class,
     * e.g. supports(GestureAction.ZOOM) == isZoomSupported().
     *
     * @param action value to be checked
     * @return whether it's supported
     */
    public final boolean supports(@NonNull GestureAction action) {
        switch (action) {
            case AUTO_FOCUS:
                return isAutoFocusSupported();
            case TAKE_PICTURE:
            case FILTER_CONTROL_1:
            case FILTER_CONTROL_2:
            case NONE:
                return true;
            case ZOOM:
                return isZoomSupported();
            case EXPOSURE_CORRECTION:
                return isExposureCorrectionSupported();
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    @NonNull
    public final <T extends Control> Collection<T> getSupportedControls(
            @NonNull Class<T> controlClass) {
        if (controlClass.equals(Audio.class)) {
            return (Collection<T>) Arrays.asList(Audio.values());
        } else if (controlClass.equals(Facing.class)) {
            return (Collection<T>) getSupportedFacing();
        } else if (controlClass.equals(Flash.class)) {
            return (Collection<T>) getSupportedFlash();
        } else if (controlClass.equals(Grid.class)) {
            return (Collection<T>) Arrays.asList(Grid.values());
        } else if (controlClass.equals(Hdr.class)) {
            return (Collection<T>) getSupportedHdr();
        } else if (controlClass.equals(Mode.class)) {
            return (Collection<T>) Arrays.asList(Mode.values());
        } else if (controlClass.equals(VideoCodec.class)) {
            return (Collection<T>) Arrays.asList(VideoCodec.values());
        } else if (controlClass.equals(AudioCodec.class)) {
            return (Collection<T>) Arrays.asList(AudioCodec.values());
        } else if (controlClass.equals(WhiteBalance.class)) {
            return (Collection<T>) getSupportedWhiteBalance();
        } else if (controlClass.equals(Engine.class)) {
            return (Collection<T>) Arrays.asList(Engine.values());
        } else if (controlClass.equals(Preview.class)) {
            return (Collection<T>) Arrays.asList(Preview.values());
        } else if (controlClass.equals(PictureFormat.class)) {
            return (Collection<T>) getSupportedPictureFormats();
        }
        // Unrecognized control.
        return Collections.emptyList();
    }

    /**
     * Set of supported picture sizes for the currently opened camera.
     *
     * @return a collection of supported values.
     */
    @NonNull
    public final Collection<Size> getSupportedPictureSizes() {
        return Collections.unmodifiableSet(supportedPictureSizes);
    }

    /**
     * Set of supported picture aspect ratios for the currently opened camera.
     *
     * @return a collection of supported values.
     */
    @NonNull
    public final Collection<AspectRatio> getSupportedPictureAspectRatios() {
        return Collections.unmodifiableSet(supportedPictureAspectRatio);
    }

    /**
     * Set of supported video sizes for the currently opened camera.
     *
     * @return a collection of supported values.
     */
    @NonNull
    public final Collection<Size> getSupportedVideoSizes() {
        return Collections.unmodifiableSet(supportedVideoSizes);
    }

    /**
     * Set of supported picture aspect ratios for the currently opened camera.
     *
     * @return a set of supported values.
     */
    @NonNull
    public final Collection<AspectRatio> getSupportedVideoAspectRatios() {
        return Collections.unmodifiableSet(supportedVideoAspectRatio);
    }

    /**
     * Set of supported facing values.
     *
     * @return a collection of supported values.
     * @see Facing#BACK
     * @see Facing#FRONT
     */
    @NonNull
    public final Collection<Facing> getSupportedFacing() {
        return Collections.unmodifiableSet(supportedFacing);
    }

    /**
     * Set of supported flash values.
     *
     * @return a collection of supported values.
     * @see Flash#AUTO
     * @see Flash#OFF
     * @see Flash#ON
     * @see Flash#TORCH
     */
    @NonNull
    public final Collection<Flash> getSupportedFlash() {
        return Collections.unmodifiableSet(supportedFlash);
    }

    /**
     * Set of supported white balance values.
     *
     * @return a collection of supported values.
     * @see WhiteBalance#AUTO
     * @see WhiteBalance#INCANDESCENT
     * @see WhiteBalance#FLUORESCENT
     * @see WhiteBalance#DAYLIGHT
     * @see WhiteBalance#CLOUDY
     */
    @NonNull
    public final Collection<WhiteBalance> getSupportedWhiteBalance() {
        return Collections.unmodifiableSet(supportedWhiteBalance);
    }

    /**
     * Set of supported hdr values.
     *
     * @return a collection of supported values.
     * @see Hdr#OFF
     * @see Hdr#ON
     */
    @NonNull
    public final Collection<Hdr> getSupportedHdr() {
        return Collections.unmodifiableSet(supportedHdr);
    }

    /**
     * Set of supported picture formats.
     *
     * @return a collection of supported values.
     * @see PictureFormat#JPEG
     * @see PictureFormat#DNG
     */
    @NonNull
    public final Collection<PictureFormat> getSupportedPictureFormats() {
        return Collections.unmodifiableSet(supportedPictureFormats);
    }

    /**
     * Set of supported formats for frame processing,
     * as {@link ImageFormat} constants.
     *
     * @return a collection of supported values.
     * @see CameraView#setFrameProcessingFormat(int)
     */
    @NonNull
    public final Collection<Integer> getSupportedFrameProcessingFormats() {
        return Collections.unmodifiableSet(supportedFrameProcessingFormats);
    }

    /**
     * Whether zoom is supported. If this is false, pinch-to-zoom
     * will not work and {@link CameraView#setZoom(float)} will have no effect.
     *
     * @return whether zoom is supported.
     */
    public final boolean isZoomSupported() {
        return zoomSupported;
    }


    /**
     * Whether touch metering (metering with respect to a specific region of the screen) is
     * supported. If it is, you can map gestures to {@link GestureAction#AUTO_FOCUS}
     * and metering will change on tap.
     *
     * @return whether auto focus is supported.
     */
    public final boolean isAutoFocusSupported() {
        return autoFocusSupported;
    }

    /**
     * Whether exposure correction is supported. If this is false, calling
     * {@link CameraView#setExposureCorrection(float)} has no effect.
     *
     * @return whether exposure correction is supported.
     * @see #getExposureCorrectionMinValue()
     * @see #getExposureCorrectionMaxValue()
     */
    public final boolean isExposureCorrectionSupported() {
        return exposureCorrectionSupported;
    }

    /**
     * The minimum value of negative exposure correction, in EV stops.
     * This is presumably negative or 0 if not supported.
     *
     * @return min EV value
     */
    public final float getExposureCorrectionMinValue() {
        return exposureCorrectionMinValue;
    }


    /**
     * The maximum value of positive exposure correction, in EV stops.
     * This is presumably positive or 0 if not supported.
     *
     * @return max EV value
     */
    public final float getExposureCorrectionMaxValue() {
        return exposureCorrectionMaxValue;
    }

    /**
     * The minimum value for the preview frame rate, in frames per second (FPS).
     *
     * @return the min value
     */
    public final float getPreviewFrameRateMinValue() {
        return previewFrameRateMinValue;
    }

    /**
     * The maximum value for the preview frame rate, in frames per second (FPS).
     *
     * @return the max value
     */
    public final float getPreviewFrameRateMaxValue() {
        return previewFrameRateMaxValue;
    }
}
