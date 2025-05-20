package com.base.cameraview.engine

import android.location.Location
import androidx.annotation.CallSuper
import androidx.annotation.VisibleForTesting
import com.base.cameraview.CameraException
import com.base.cameraview.CameraOptions
import com.base.cameraview.PictureResult
import com.base.cameraview.VideoResult
import com.base.cameraview.controls.Audio
import com.base.cameraview.controls.AudioCodec
import com.base.cameraview.controls.Facing
import com.base.cameraview.controls.Flash
import com.base.cameraview.controls.Hdr
import com.base.cameraview.controls.Mode
import com.base.cameraview.controls.PictureFormat
import com.base.cameraview.controls.VideoCodec
import com.base.cameraview.controls.WhiteBalance
import com.base.cameraview.engine.offset.Angles
import com.base.cameraview.engine.offset.Reference
import com.base.cameraview.engine.orchestrator.CameraState
import com.base.cameraview.frame.FrameManager
import com.base.cameraview.overlay.Overlay
import com.base.cameraview.picture.PictureRecorder
import com.base.cameraview.preview.CameraPreview
import com.base.cameraview.size.AspectRatio
import com.base.cameraview.size.Size
import com.base.cameraview.size.SizeSelector
import com.base.cameraview.size.SizeSelectors
import com.base.cameraview.video.VideoRecorder
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.io.File
import java.io.FileDescriptor
import kotlin.math.floor
import kotlin.math.min

/**
 * Abstract implementation of [CameraEngine] that helps in common tasks.
 */
abstract class CameraBaseEngine protected constructor(callback: Callback) : CameraEngine(callback) {
    private val mAngles = Angles()
    protected var mPreview: CameraPreview<*, *>? = null
    protected var mCameraOptions: CameraOptions? = null
    protected var mPictureRecorder: PictureRecorder? = null
    protected var mVideoRecorder: VideoRecorder? = null
    protected var mCaptureSize: Size? = null
    protected var mPreviewStreamSize: Size? = null
    protected var mFrameProcessingSize: Size? = null
    protected var mFrameProcessingFormat: Int = 0
    protected var mHasFrameProcessors: Boolean = false
    protected var mFlash: Flash? = null
    protected var mWhiteBalance: WhiteBalance? = null
    protected var mVideoCodec: VideoCodec? = null
    protected var mAudioCodec: AudioCodec? = null
    protected var mHdr: Hdr? = null
    protected var mPictureFormat: PictureFormat? = null
    protected var mLocation: Location? = null
    protected var mZoomValue: Float = 0f
    protected var mExposureCorrectionValue: Float = 0f
    protected var mPlaySounds: Boolean = false
    protected var mPictureMetering: Boolean = false
    protected var mPictureSnapshotMetering: Boolean = false
    protected var mPreviewFrameRate: Float = 0f

    // Ops used for testing.
    @VisibleForTesting(otherwise = VisibleForTesting.Companion.PROTECTED)
    var mZoomTask: Task<Void?> = Tasks.forResult<Void?>(null)

    @VisibleForTesting(otherwise = VisibleForTesting.Companion.PROTECTED)
    var mExposureCorrectionTask: Task<Void?> = Tasks.forResult<Void?>(null)

    @VisibleForTesting(otherwise = VisibleForTesting.Companion.PROTECTED)
    var mFlashTask: Task<Void?> = Tasks.forResult<Void?>(null)

    @VisibleForTesting(otherwise = VisibleForTesting.Companion.PROTECTED)
    var mWhiteBalanceTask: Task<Void?> = Tasks.forResult<Void?>(null)

    @VisibleForTesting(otherwise = VisibleForTesting.Companion.PROTECTED)
    var mHdrTask: Task<Void?> = Tasks.forResult<Void?>(null)

    @VisibleForTesting(otherwise = VisibleForTesting.Companion.PROTECTED)
    var mLocationTask: Task<Void?> = Tasks.forResult<Void?>(null)

    @VisibleForTesting(otherwise = VisibleForTesting.Companion.PROTECTED)
    var mPlaySoundsTask: Task<Void?> = Tasks.forResult<Void?>(null)

    @VisibleForTesting(otherwise = VisibleForTesting.Companion.PROTECTED)
    var mPreviewFrameRateTask: Task<Void?> = Tasks.forResult<Void?>(null)
    private var mPreviewFrameRateExact = false
    private var mFrameManager: FrameManager<*>? = null
    private var mPreviewStreamSizeSelector: SizeSelector? = null
    private var mPictureSizeSelector: SizeSelector? = null
    private var mVideoSizeSelector: SizeSelector? = null
    private var mFacing: Facing? = null
    private var mMode: Mode? = null
    private var mAudio: Audio? = null
    private var mVideoMaxSize: Long = 0
    private var mVideoMaxDuration = 0
    private var mVideoBitRate = 0
    private var mAudioBitRate = 0
    private var mAutoFocusResetDelayMillis: Long = 0
    private var mSnapshotMaxWidth = 0 // in REF_VIEW like SizeSelectors
    private var mSnapshotMaxHeight = 0 // in REF_VIEW like SizeSelectors
    private var mFrameProcessingMaxWidth = 0 // in REF_VIEW like SizeSelectors
    private var mFrameProcessingMaxHeight = 0 // in REF_VIEW like SizeSelectors
    private var mFrameProcessingPoolSize = 0
    private var mOverlay: Overlay? = null

    /**
     * Called at construction time to get a frame manager that can later be
     * accessed through [.getFrameManager].
     *
     * @param poolSize pool size
     * @return a frame manager
     */
    protected abstract fun instantiateFrameManager(poolSize: Int): FrameManager<*>

    override fun getAngles(): Angles {
        return mAngles
    }

    override fun getFrameManager(): FrameManager<*> {
        if (mFrameManager == null) {
            mFrameManager = instantiateFrameManager(mFrameProcessingPoolSize)
        }
        return mFrameManager!!
    }

    override fun getCameraOptions(): CameraOptions? {
        return mCameraOptions
    }

    override fun getPreview(): CameraPreview<*, *> {
        return mPreview!!
    }

    override fun setPreview(cameraPreview: CameraPreview<*, *>) {
        if (mPreview != null) mPreview!!.setSurfaceCallback(null)
        mPreview = cameraPreview
        mPreview!!.setSurfaceCallback(this)
    }

    override fun getOverlay(): Overlay? {
        return mOverlay
    }

    override fun setOverlay(overlay: Overlay?) {
        mOverlay = overlay
    }

    override fun getPreviewStreamSizeSelector(): SizeSelector? {
        return mPreviewStreamSizeSelector
    }

    override fun setPreviewStreamSizeSelector(selector: SizeSelector?) {
        mPreviewStreamSizeSelector = selector
    }

    override fun getPictureSizeSelector(): SizeSelector {
        return mPictureSizeSelector!!
    }

    override fun setPictureSizeSelector(selector: SizeSelector) {
        mPictureSizeSelector = selector
    }

    override fun getVideoSizeSelector(): SizeSelector {
        return mVideoSizeSelector!!
    }

    override fun setVideoSizeSelector(selector: SizeSelector) {
        mVideoSizeSelector = selector
    }

    override fun getVideoMaxSize(): Long {
        return mVideoMaxSize
    }

    override fun setVideoMaxSize(videoMaxSizeBytes: Long) {
        mVideoMaxSize = videoMaxSizeBytes
    }

    override fun getVideoMaxDuration(): Int {
        return mVideoMaxDuration
    }

    override fun setVideoMaxDuration(videoMaxDurationMillis: Int) {
        mVideoMaxDuration = videoMaxDurationMillis
    }

    override fun getVideoCodec(): VideoCodec {
        return mVideoCodec!!
    }

    override fun setVideoCodec(codec: VideoCodec) {
        mVideoCodec = codec
    }

    override fun getVideoBitRate(): Int {
        return mVideoBitRate
    }

    override fun setVideoBitRate(videoBitRate: Int) {
        mVideoBitRate = videoBitRate
    }

    override fun getAudioCodec(): AudioCodec {
        return mAudioCodec!!
    }

    override fun setAudioCodec(codec: AudioCodec) {
        mAudioCodec = codec
    }

    override fun getAudioBitRate(): Int {
        return mAudioBitRate
    }

    override fun setAudioBitRate(audioBitRate: Int) {
        mAudioBitRate = audioBitRate
    }

    override fun getSnapshotMaxWidth(): Int {
        return mSnapshotMaxWidth
    }

    override fun setSnapshotMaxWidth(maxWidth: Int) {
        mSnapshotMaxWidth = maxWidth
    }

    override fun getSnapshotMaxHeight(): Int {
        return mSnapshotMaxHeight
    }

    override fun setSnapshotMaxHeight(maxHeight: Int) {
        mSnapshotMaxHeight = maxHeight
    }

    override fun getFrameProcessingMaxWidth(): Int {
        return mFrameProcessingMaxWidth
    }

    override fun setFrameProcessingMaxWidth(maxWidth: Int) {
        mFrameProcessingMaxWidth = maxWidth
    }

    override fun getFrameProcessingMaxHeight(): Int {
        return mFrameProcessingMaxHeight
    }

    override fun setFrameProcessingMaxHeight(maxHeight: Int) {
        mFrameProcessingMaxHeight = maxHeight
    }

    override fun getFrameProcessingFormat(): Int {
        return mFrameProcessingFormat
    }

    override fun getFrameProcessingPoolSize(): Int {
        return mFrameProcessingPoolSize
    }

    override fun setFrameProcessingPoolSize(poolSize: Int) {
        mFrameProcessingPoolSize = poolSize
    }

    override fun getAutoFocusResetDelay(): Long {
        return mAutoFocusResetDelayMillis
    }

    override fun setAutoFocusResetDelay(delayMillis: Long) {
        mAutoFocusResetDelayMillis = delayMillis
    }

    /**
     * Helper function for subclasses.
     *
     * @return true if AF should be reset
     */
    protected fun shouldResetAutoFocus(): Boolean {
        return mAutoFocusResetDelayMillis > 0 && mAutoFocusResetDelayMillis != Long.Companion.MAX_VALUE
    }

    override fun getFacing(): Facing {
        return mFacing ?: Facing.defaultFacing()
    }

    /**
     * Sets a new facing value. This will restart the engine session (if there's any)
     * so that we can open the new facing camera.
     *
     * @param facing facing
     */
    override fun setFacing(facing: Facing) {

        val old = (mFacing ?: Facing.defaultFacing())
        if (facing != old) {
            mFacing = facing
            orchestrator.scheduleStateful(
                "facing", CameraState.ENGINE
            ) {
                if (collectCameraInfo(facing)) {
                    restart()
                } else {
                    mFacing = old
                }
            }
        }
    }

    override fun getAudio(): Audio {
        return mAudio ?: Audio.DEFAULT
    }

    /**
     * Sets a new audio value that will be used for video recordings.
     *
     * @param audio desired audio
     */
    override fun setAudio(audio: Audio) {
        if (mAudio != audio) {
            if (isTakingVideo) {
                LOG.w(
                    "Audio setting was changed while recording. " + "Changes will take place starting from next video"
                )
            }
            mAudio = audio
        }
    }

    override fun getMode(): Mode {
        return mMode!!
    }

    /**
     * Sets the desired mode (either picture or video).
     *
     * @param mode desired mode.
     */
    override fun setMode(mode: Mode) {
        if (mode != mMode) {
            mMode = mode
            orchestrator.scheduleStateful(
                "mode", CameraState.ENGINE
            ) {
                restart()
            }
        }
    }

    override fun getZoomValue(): Float {
        return mZoomValue
    }

    override fun getExposureCorrectionValue(): Float {
        return mExposureCorrectionValue
    }

    override fun getFlash(): Flash {
        return mFlash!!
    }

    override fun getWhiteBalance(): WhiteBalance {
        return mWhiteBalance!!
    }

    override fun getHdr(): Hdr {
        return mHdr!!
    }

    override fun getLocation(): Location? {
        return mLocation
    }

    override fun getPictureFormat(): PictureFormat {
        return mPictureFormat!!
    }

    override fun getPreviewFrameRateExact(): Boolean {
        return mPreviewFrameRateExact
    }

    override fun setPreviewFrameRateExact(previewFrameRateExact: Boolean) {
        mPreviewFrameRateExact = previewFrameRateExact
    }

    override fun getPreviewFrameRate(): Float {
        return mPreviewFrameRate
    }

    override fun hasFrameProcessors(): Boolean {
        return mHasFrameProcessors
    }

    override fun getPictureMetering(): Boolean {
        return mPictureMetering
    }

    override fun setPictureMetering(enable: Boolean) {
        mPictureMetering = enable
    }

    override fun getPictureSnapshotMetering(): Boolean {
        return mPictureSnapshotMetering
    }

    override fun setPictureSnapshotMetering(enable: Boolean) {
        mPictureSnapshotMetering = enable
    }

    //region Picture and video control
    override fun isTakingPicture(): Boolean {
        return mPictureRecorder != null
    }

    /* final */ override fun takePicture(stub: PictureResult.Stub) {
        // Save boolean before scheduling! See how Camera2Engine calls this with a temp value.
        val metering = mPictureMetering
        orchestrator.scheduleStateful(
            "take picture", CameraState.BIND
        ) {
            LOG.i("takePicture:", "running. isTakingPicture:", isTakingPicture)
            if (isTakingPicture) return@scheduleStateful
            check(mMode != Mode.VIDEO) { "Can't take hq pictures while in VIDEO mode" }
            stub.isSnapshot = false
            stub.location = mLocation
            stub.facing = mFacing
            stub.format = mPictureFormat
            onTakePicture(stub, metering)
        }
    }

    /**
     * The snapshot size is the [.getPreviewStreamSize], but cropped based on the
     * view/surface aspect ratio.
     *
     * @param stub a picture stub
     *//* final */ override fun takePictureSnapshot(stub: PictureResult.Stub) {
        // Save boolean before scheduling! See how Camera2Engine calls this with a temp value.
        val metering = mPictureSnapshotMetering
        orchestrator.scheduleStateful(
            "take picture snapshot", CameraState.BIND
        ) {
            LOG.i("takePictureSnapshot:", "running. isTakingPicture:", isTakingPicture)
            if (isTakingPicture) return@scheduleStateful
            stub.location = mLocation
            stub.isSnapshot = true
            stub.facing = mFacing
            stub.format = PictureFormat.JPEG
            // Leave the other parameters to subclasses.
            val ratio = AspectRatio.of(getPreviewSurfaceSize(Reference.OUTPUT)!!)
            onTakePictureSnapshot(stub, ratio, metering)
        }
    }

    override fun onPictureShutter(didPlaySound: Boolean) {
        callback.dispatchOnPictureShutter(!didPlaySound)
    }

    override fun onPictureResult(result: PictureResult.Stub?, error: Exception?) {
        mPictureRecorder = null
        if (result != null && result.data != null) {
            callback.dispatchOnPictureTaken(result)
        } else {
            LOG.e("onPictureResult", "result or data is null: something went wrong.", error)
            callback.dispatchError(
                CameraException(
                    error, CameraException.REASON_PICTURE_FAILED
                )
            )
        }
    }

    override fun isTakingVideo(): Boolean {
        return mVideoRecorder != null && mVideoRecorder!!.isRecording
    }

    override fun takeVideo(
        stub: VideoResult.Stub, file: File?, fileDescriptor: FileDescriptor?
    ) {
        orchestrator.scheduleStateful("take video", CameraState.BIND) {
            LOG.i("takeVideo:", "running. isTakingVideo:", isTakingVideo)
            if (isTakingVideo) return@scheduleStateful
            check(mMode != Mode.PICTURE) { "Can't record video while in PICTURE mode" }
            if (file != null) {
                stub.file = file
            } else if (fileDescriptor != null) {
                stub.fileDescriptor = fileDescriptor
            } else {
                throw IllegalStateException("file and fileDescriptor are both null.")
            }
            stub.isSnapshot = false
            stub.videoCodec = mVideoCodec
            stub.audioCodec = mAudioCodec
            stub.location = mLocation
            stub.facing = mFacing
            stub.audio = mAudio
            stub.maxSize = mVideoMaxSize
            stub.maxDuration = mVideoMaxDuration
            stub.videoBitRate = mVideoBitRate
            stub.audioBitRate = mAudioBitRate
            onTakeVideo(stub)
        }
    }

    /**
     * @param stub a video stub
     * @param file the output file
     */
    override fun takeVideoSnapshot(
        stub: VideoResult.Stub, file: File
    ) {
        orchestrator.scheduleStateful(
            "take video snapshot", CameraState.BIND
        ) {
            LOG.i("takeVideoSnapshot:", "running. isTakingVideo:", isTakingVideo)
            stub.file = file
            stub.isSnapshot = true
            stub.videoCodec = mVideoCodec
            stub.audioCodec = mAudioCodec
            stub.location = mLocation
            stub.facing = mFacing
            stub.videoBitRate = mVideoBitRate
            stub.audioBitRate = mAudioBitRate
            stub.audio = mAudio
            stub.maxSize = mVideoMaxSize
            stub.maxDuration = mVideoMaxDuration
            val ratio = AspectRatio.of(getPreviewSurfaceSize(Reference.OUTPUT)!!)
            onTakeVideoSnapshot(stub, ratio)
        }
    }

    override fun stopVideo() {
        orchestrator.schedule("stop video", true) {
            LOG.i("stopVideo", "running. isTakingVideo?", isTakingVideo)
            onStopVideo()
        }
    }

    @EngineThread
    protected fun onStopVideo() {
        if (mVideoRecorder != null) {
            mVideoRecorder!!.stop(false)
            // Do not null this, so we respond correctly to isTakingVideo(),
            // which checks for recorder presence and recorder.isRecording().
            // It will be pulled in onVideoResult.
        }
    }

    @CallSuper
    override fun onVideoResult(result: VideoResult.Stub?, exception: Exception?) {
        mVideoRecorder = null
        if (result != null) {
            callback.dispatchOnVideoTaken(result)
        } else {
            LOG.e("onVideoResult", "result is null: something went wrong.", exception)
            callback.dispatchError(
                CameraException(
                    exception, CameraException.REASON_VIDEO_FAILED
                )
            )
        }
    }

    override fun onVideoRecordingStart() {
        callback.dispatchOnVideoRecordingStart()
    }

    override fun onVideoRecordingEnd() {
        callback.dispatchOnVideoRecordingEnd()
    }

    @EngineThread
    protected abstract fun onTakePicture(stub: PictureResult.Stub, doMetering: Boolean)

    @EngineThread
    protected abstract fun onTakePictureSnapshot(
        stub: PictureResult.Stub, outputRatio: AspectRatio, doMetering: Boolean
    )

    @EngineThread
    protected abstract fun onTakeVideoSnapshot(
        stub: VideoResult.Stub, outputRatio: AspectRatio
    )

    @EngineThread
    protected abstract fun onTakeVideo(stub: VideoResult.Stub)

    //endregion
    //region Size / Surface
    override fun onSurfaceChanged() {
        LOG.i("onSurfaceChanged:", "Size is", getPreviewSurfaceSize(Reference.VIEW))
        orchestrator.scheduleStateful(
            "surface changed", CameraState.BIND
        ) {
            // Compute a new camera preview size and apply.
            val newSize = computePreviewStreamSize()
            if (newSize == mPreviewStreamSize) {
                LOG.i(
                    "onSurfaceChanged:", "The computed preview size is identical. No op."
                )
            } else {
                LOG.i(
                    "onSurfaceChanged:",
                    "Computed a new preview size. Calling onPreviewStreamSizeChanged()."
                )
                mPreviewStreamSize = newSize
                onPreviewStreamSizeChanged()
            }
        }
    }

    /**
     * The preview stream size has changed. At this point, some engine might want to
     * simply call [.restartPreview], others to [.restartBind].
     *
     *
     * It basically depends on the step at which the preview stream size is actually used.
     */
    @EngineThread
    protected abstract fun onPreviewStreamSizeChanged()

    override fun getPictureSize(reference: Reference): Size? {
        val size = mCaptureSize
        if (size == null || mMode == Mode.VIDEO) return null
        return if (angles.flip(Reference.SENSOR, reference)) size.flip() else size
    }

    override fun getVideoSize(reference: Reference): Size? {
        val size = mCaptureSize
        if (size == null || mMode == Mode.PICTURE) return null
        return if (angles.flip(Reference.SENSOR, reference)) size.flip() else size
    }

    override fun getPreviewStreamSize(reference: Reference): Size? {
        val size = mPreviewStreamSize
        if (size == null) return null
        return if (angles.flip(Reference.SENSOR, reference)) size.flip() else size
    }

    private fun getPreviewSurfaceSize(reference: Reference): Size? {
        val preview = mPreview
        if (preview == null) return null
        return if (angles.flip(Reference.VIEW, reference)) preview.surfaceSize.flip()
        else preview.surfaceSize
    }

    /**
     * Returns the snapshot size, but not cropped with the view dimensions, which
     * is what we will do before creating the snapshot. However, cropping is done at various
     * levels so we don't want to perform the op here.
     *
     *
     * The base snapshot size is based on PreviewStreamSize (later cropped with view ratio). Why?
     * One might be tempted to say that it's the SurfaceSize (which already matches the view ratio).
     *
     *
     * The camera sensor will capture preview frames with PreviewStreamSize and that's it. Then they
     * are hardware-scaled by the preview surface, but this does not affect the snapshot, as the
     * snapshot recorder simply creates another surface.
     *
     *
     * Done tests to ensure that this is true, by using
     * 1. small SurfaceSize and biggest() PreviewStreamSize: output is not low quality
     * 2. big SurfaceSize and smallest() PreviewStreamSize: output is low quality
     * In both cases the result.size here was set to the biggest of the two.
     *
     *
     * I could not find the same evidence for videos, but I would say that the same things should
     * apply, despite the capturing mechanism being different.
     *
     * @param reference the reference system
     * @return the uncropped snapshot size
     */
    override fun getUncroppedSnapshotSize(reference: Reference): Size? {
        val baseSize = getPreviewStreamSize(reference)
        if (baseSize == null) return null
        val flip = angles.flip(reference, Reference.VIEW)
        var maxWidth = if (flip) mSnapshotMaxHeight else mSnapshotMaxWidth
        var maxHeight = if (flip) mSnapshotMaxWidth else mSnapshotMaxHeight
        if (maxWidth <= 0) maxWidth = Int.Companion.MAX_VALUE
        if (maxHeight <= 0) maxHeight = Int.Companion.MAX_VALUE
        val baseRatio = AspectRatio.of(baseSize).toFloat()
        val maxValuesRatio = AspectRatio.of(maxWidth, maxHeight).toFloat()
        if (maxValuesRatio >= baseRatio) {
            // Height is the real constraint.
            val outHeight = min(baseSize.height, maxHeight)
            val outWidth = floor((outHeight.toFloat() * baseRatio).toDouble()).toInt()
            return Size(outWidth, outHeight)
        } else {
            // Width is the real constraint.
            val outWidth = min(baseSize.width, maxWidth)
            val outHeight = floor((outWidth.toFloat() / baseRatio).toDouble()).toInt()
            return Size(outWidth, outHeight)
        }
    }

    /**
     * This is called either on cameraView.start(), or when the underlying surface changes.
     * It is possible that in the first call the preview surface has not already computed its
     * dimensions.
     * But when it does, the [CameraPreview.SurfaceCallback] should be called,
     * and this should be refreshed.
     *
     * @return the capture size
     */
    protected fun computeCaptureSize(mode: Mode = (mMode ?: Mode.PICTURE)): Size {
        // We want to pass stuff into the REF_VIEW reference, not the sensor one.
        // This is already managed by CameraOptions, so we just flip again at the end.
        val flip = angles.flip(Reference.SENSOR, Reference.VIEW)
        var selector: SizeSelector?
        val sizes: MutableCollection<Size?>?
        if (mode == Mode.PICTURE) {
            selector = mPictureSizeSelector
            sizes = mCameraOptions!!.getSupportedPictureSizes()
        } else {
            selector = mVideoSizeSelector
            sizes = mCameraOptions!!.getSupportedVideoSizes()
        }
        selector = SizeSelectors.or(selector, SizeSelectors.biggest())
        val list: MutableList<Size?> = ArrayList(sizes)
        var result: Size = selector.select(list)[0]!!
        if (!list.contains(result)) {
            throw RuntimeException(
                "SizeSelectors must not return Sizes other than " + "those in the input list."
            )
        }
        LOG.i("computeCaptureSize:", "result:", result, "flip:", flip, "mode:", mode)
        if (flip) result = result.flip() // Go back to REF_SENSOR

        return result
    }

    @get:EngineThread
    protected abstract val previewStreamAvailableSizes: MutableList<Size>

    @EngineThread
    protected fun computePreviewStreamSize(): Size {
        val previewSizes = this.previewStreamAvailableSizes
        // These sizes come in REF_SENSOR. Since there is an external selector involved,
        // we must convert all of them to REF_VIEW, then flip back when returning.
        val flip = angles.flip(Reference.SENSOR, Reference.VIEW)
        val sizes: MutableList<Size?> = ArrayList(previewSizes.size)
        for (size in previewSizes) {
            sizes.add(if (flip) size.flip() else size)
        }

        // Create our own default selector, which will be used if the external
        // mPreviewStreamSizeSelector is null, or if it fails in finding a size.
        val targetMinSize = getPreviewSurfaceSize(Reference.VIEW)
        checkNotNull(targetMinSize) { "targetMinSize should not be null here." }
        var targetRatio = AspectRatio.of(mCaptureSize!!.width, mCaptureSize!!.height)
        if (flip) targetRatio = targetRatio.flip()
        LOG.i(
            "computePreviewStreamSize:",
            "targetRatio:",
            targetRatio,
            "targetMinSize:",
            targetMinSize
        )
        val matchRatio = SizeSelectors.and( // Match this aspect ratio and sort by biggest
            SizeSelectors.aspectRatio(targetRatio, 0f), SizeSelectors.biggest()
        )
        val matchSize = SizeSelectors.and( // Bigger than this size, and sort by smallest
            SizeSelectors.minHeight(targetMinSize.height),
            SizeSelectors.minWidth(targetMinSize.width),
            SizeSelectors.smallest()
        )
        val matchAll = SizeSelectors.or(
            SizeSelectors.and(matchRatio, matchSize),  // Try to respect both constraints.
            matchSize,  // If couldn't match aspect ratio, at least respect the size
            matchRatio,  // If couldn't respect size, at least match aspect ratio
            SizeSelectors.biggest() // If couldn't match any, take the biggest.
        )

        // Apply the external selector with this as a fallback,
        // and return a size in REF_SENSOR reference.
        val selector = if (mPreviewStreamSizeSelector != null) {
            SizeSelectors.or(mPreviewStreamSizeSelector, matchAll)
        } else {
            matchAll
        }
        var result: Size = selector.select(sizes)[0]!!
        if (!sizes.contains(result)) {
            throw RuntimeException(
                "SizeSelectors must not return Sizes other than " + "those in the input list."
            )
        }
        if (flip) result = result.flip()
        LOG.i("computePreviewStreamSize:", "result:", result, "flip:", flip)
        return result
    }

    @get:EngineThread
    protected abstract val frameProcessingAvailableSizes: MutableList<Size>

    @EngineThread
    protected fun computeFrameProcessingSize(): Size {
        val frameSizes = this.frameProcessingAvailableSizes
        // These sizes come in REF_SENSOR. Since there is an external selector involved,
        // we must convert all of them to REF_VIEW, then flip back when returning.
        val flip = angles.flip(Reference.SENSOR, Reference.VIEW)
        val sizes: MutableList<Size?> = ArrayList(frameSizes.size)
        for (size in frameSizes) {
            sizes.add(if (flip) size.flip() else size)
        }
        var targetRatio = AspectRatio.of(
            mPreviewStreamSize!!.width, mPreviewStreamSize!!.height
        )
        if (flip) targetRatio = targetRatio.flip()
        var maxWidth = mFrameProcessingMaxWidth
        var maxHeight = mFrameProcessingMaxHeight
        if (maxWidth <= 0 || maxWidth == Int.Companion.MAX_VALUE) maxWidth = 640
        if (maxHeight <= 0 || maxHeight == Int.Companion.MAX_VALUE) maxHeight = 640
        val targetMaxSize = Size(maxWidth, maxHeight)
        LOG.i(
            "computeFrameProcessingSize:",
            "targetRatio:",
            targetRatio,
            "targetMaxSize:",
            targetMaxSize
        )
        val matchRatio = SizeSelectors.aspectRatio(targetRatio, 0f)
        val matchSize = SizeSelectors.and(
            SizeSelectors.maxHeight(targetMaxSize.height),
            SizeSelectors.maxWidth(targetMaxSize.width),
            SizeSelectors.biggest()
        )
        val matchAll = SizeSelectors.or(
            SizeSelectors.and(matchRatio, matchSize),  // Try to respect both constraints.
            matchSize,  // If couldn't match aspect ratio, at least respect the size
            SizeSelectors.smallest() // If couldn't match any, take the smallest.
        )
        var result: Size = matchAll.select(sizes)[0]!!
        if (!sizes.contains(result)) {
            throw RuntimeException(
                "SizeSelectors must not return Sizes other than " + "those in the input list."
            )
        }
        if (flip) result = result.flip()
        LOG.i("computeFrameProcessingSize:", "result:", result, "flip:", flip)
        return result
    } //endregion

    companion object {
        const val ALLOWED_ZOOM_OPS: Int = 20
        const val ALLOWED_EV_OPS: Int = 20
    }
}
