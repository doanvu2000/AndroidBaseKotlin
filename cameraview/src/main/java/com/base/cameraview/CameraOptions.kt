package com.base.cameraview

import com.base.cameraview.controls.Audio
import com.base.cameraview.controls.AudioCodec
import com.base.cameraview.controls.Control
import com.base.cameraview.controls.Engine
import com.base.cameraview.controls.Facing
import com.base.cameraview.controls.Flash
import com.base.cameraview.controls.Grid
import com.base.cameraview.controls.Hdr
import com.base.cameraview.controls.Mode
import com.base.cameraview.controls.PictureFormat
import com.base.cameraview.controls.Preview
import com.base.cameraview.controls.VideoCodec
import com.base.cameraview.controls.WhiteBalance
import com.base.cameraview.gesture.GestureAction
import com.base.cameraview.size.AspectRatio
import com.base.cameraview.size.Size
import java.util.Collections


/**
 * Options telling you what is available and what is not.
 */
abstract class CameraOptions protected constructor() {
    protected var supportedWhiteBalance: MutableSet<WhiteBalance?> = HashSet(5)
    protected var supportedFacing: MutableSet<Facing?> = HashSet(2)
    protected var supportedFlash: MutableSet<Flash?> = HashSet(4)
    protected var supportedHdr: MutableSet<Hdr?> = HashSet(2)
    protected var supportedPictureSizes: MutableSet<Size?> = HashSet(15)
    protected var supportedVideoSizes: MutableSet<Size?> = HashSet(5)
    protected var supportedPictureAspectRatio: MutableSet<AspectRatio?> = HashSet(4)
    protected var supportedVideoAspectRatio: MutableSet<AspectRatio?> = HashSet(3)
    protected var supportedPictureFormats: MutableSet<PictureFormat?> = HashSet(2)
    protected var supportedFrameProcessingFormats: MutableSet<Int?> = HashSet(2)

    /**
     * Whether zoom is supported. If this is false, pinch-to-zoom
     * will not work and [CameraView.setZoom] will have no effect.
     *
     * @return whether zoom is supported.
     */
    var isZoomSupported: Boolean = false
        protected set

    /**
     * Whether exposure correction is supported. If this is false, calling
     * [CameraView.setExposureCorrection] has no effect.
     *
     * @return whether exposure correction is supported.
     * @see .getExposureCorrectionMinValue
     * @see .getExposureCorrectionMaxValue
     */
    var isExposureCorrectionSupported: Boolean = false
        protected set

    /**
     * The minimum value of negative exposure correction, in EV stops.
     * This is presumably negative or 0 if not supported.
     *
     * @return min EV value
     */
    var exposureCorrectionMinValue: Float = 0f
        protected set

    /**
     * The maximum value of positive exposure correction, in EV stops.
     * This is presumably positive or 0 if not supported.
     *
     * @return max EV value
     */
    var exposureCorrectionMaxValue: Float = 0f
        protected set

    /**
     * Whether touch metering (metering with respect to a specific region of the screen) is
     * supported. If it is, you can map gestures to [GestureAction.AUTO_FOCUS]
     * and metering will change on tap.
     *
     * @return whether auto focus is supported.
     */
    var isAutoFocusSupported: Boolean = false
        protected set

    /**
     * The minimum value for the preview frame rate, in frames per second (FPS).
     *
     * @return the min value
     */
    var previewFrameRateMinValue: Float = 0f
        protected set

    /**
     * The maximum value for the preview frame rate, in frames per second (FPS).
     *
     * @return the max value
     */
    var previewFrameRateMaxValue: Float = 0f
        protected set

    /**
     * Shorthand for getSupported*().contains(value).
     *
     * @param control value to check
     * @return whether it's supported
     */
    fun supports(control: Control): Boolean {
        return getSupportedControls(control.javaClass).contains(control)
    }

    /**
     * Shorthand for other methods in this class,
     * e.g. supports(GestureAction.ZOOM) == isZoomSupported().
     *
     * @param action value to be checked
     * @return whether it's supported
     */
    fun supports(action: GestureAction): Boolean {
        when (action) {
            GestureAction.AUTO_FOCUS -> return this.isAutoFocusSupported
            GestureAction.TAKE_PICTURE, GestureAction.FILTER_CONTROL_1, GestureAction.FILTER_CONTROL_2, GestureAction.NONE -> return true
            GestureAction.ZOOM -> return this.isZoomSupported
            GestureAction.EXPOSURE_CORRECTION -> return this.isExposureCorrectionSupported
            GestureAction.TAKE_PICTURE_SNAPSHOT -> {}
        }
        return false
    }

    fun <T : Control?> getSupportedControls(
        controlClass: Class<T>
    ): MutableCollection<T> {
        return when (controlClass) {
            Audio::class.java -> {
                Audio.entries as MutableCollection<T>
            }

            Facing::class.java -> {
                getSupportedFacing() as MutableCollection<T>
            }

            Flash::class.java -> {
                getSupportedFlash() as MutableCollection<T>
            }

            Grid::class.java -> {
                Grid.entries as MutableCollection<T>
            }

            Hdr::class.java -> {
                getSupportedHdr() as MutableCollection<T>
            }

            Mode::class.java -> {
                Mode.entries as MutableCollection<T>
            }

            VideoCodec::class.java -> {
                VideoCodec.entries as MutableCollection<T>
            }

            AudioCodec::class.java -> {
                AudioCodec.entries as MutableCollection<T>
            }

            WhiteBalance::class.java -> {
                getSupportedWhiteBalance() as MutableCollection<T>
            }

            Engine::class.java -> {
                Engine.entries as MutableCollection<T>
            }

            Preview::class.java -> {
                Preview.entries as MutableCollection<T>
            }

            PictureFormat::class.java -> {
                getSupportedPictureFormats() as MutableCollection<T>
            }
            // Unrecognized control.
            else -> mutableListOf()
        }
    }

    /**
     * Set of supported picture sizes for the currently opened camera.
     *
     * @return a collection of supported values.
     */
    fun getSupportedPictureSizes(): MutableCollection<Size?> {
        return Collections.unmodifiableSet<Size?>(supportedPictureSizes)
    }

    val supportedPictureAspectRatios: MutableCollection<AspectRatio?>
        /**
         * Set of supported picture aspect ratios for the currently opened camera.
         *
         * @return a collection of supported values.
         */
        get() = Collections.unmodifiableSet<AspectRatio?>(supportedPictureAspectRatio)

    /**
     * Set of supported video sizes for the currently opened camera.
     *
     * @return a collection of supported values.
     */
    fun getSupportedVideoSizes(): MutableCollection<Size?> {
        return Collections.unmodifiableSet<Size?>(supportedVideoSizes)
    }

    val supportedVideoAspectRatios: MutableCollection<AspectRatio?>
        /**
         * Set of supported picture aspect ratios for the currently opened camera.
         *
         * @return a set of supported values.
         */
        get() = Collections.unmodifiableSet<AspectRatio?>(supportedVideoAspectRatio)

    /**
     * Set of supported facing values.
     *
     * @return a collection of supported values.
     * @see Facing.BACK
     *
     * @see Facing.FRONT
     */
    fun getSupportedFacing(): MutableCollection<Facing?> {
        return Collections.unmodifiableSet<Facing?>(supportedFacing)
    }

    /**
     * Set of supported flash values.
     *
     * @return a collection of supported values.
     * @see Flash.AUTO
     *
     * @see Flash.OFF
     *
     * @see Flash.ON
     *
     * @see Flash.TORCH
     */
    fun getSupportedFlash(): MutableCollection<Flash?> {
        return Collections.unmodifiableSet<Flash?>(supportedFlash)
    }

    /**
     * Set of supported white balance values.
     *
     * @return a collection of supported values.
     * @see WhiteBalance.AUTO
     *
     * @see WhiteBalance.INCANDESCENT
     *
     * @see WhiteBalance.FLUORESCENT
     *
     * @see WhiteBalance.DAYLIGHT
     *
     * @see WhiteBalance.CLOUDY
     */
    fun getSupportedWhiteBalance(): MutableCollection<WhiteBalance?> {
        return Collections.unmodifiableSet<WhiteBalance?>(supportedWhiteBalance)
    }

    /**
     * Set of supported hdr values.
     *
     * @return a collection of supported values.
     * @see Hdr.OFF
     *
     * @see Hdr.ON
     */
    fun getSupportedHdr(): MutableCollection<Hdr?> {
        return Collections.unmodifiableSet<Hdr?>(supportedHdr)
    }

    /**
     * Set of supported picture formats.
     *
     * @return a collection of supported values.
     * @see PictureFormat.JPEG
     *
     * @see PictureFormat.DNG
     */
    fun getSupportedPictureFormats(): MutableCollection<PictureFormat?> {
        return Collections.unmodifiableSet<PictureFormat?>(supportedPictureFormats)
    }

    /**
     * Set of supported formats for frame processing,
     * as [android.graphics.ImageFormat] constants.
     *
     * @return a collection of supported values.
     * @see CameraView.setFrameProcessingFormat
     */
    fun getSupportedFrameProcessingFormats(): MutableCollection<Int?> {
        return Collections.unmodifiableSet<Int?>(supportedFrameProcessingFormats)
    }
}
