package com.base.cameraview.engine

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.graphics.PointF
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.hardware.Camera.ErrorCallback
import android.hardware.Camera.PreviewCallback
import android.location.Location
import android.view.SurfaceHolder
import androidx.annotation.VisibleForTesting
import com.base.cameraview.CameraException
import com.base.cameraview.CameraOptions
import com.base.cameraview.PictureResult
import com.base.cameraview.VideoResult
import com.base.cameraview.controls.Facing
import com.base.cameraview.controls.Flash
import com.base.cameraview.controls.Hdr
import com.base.cameraview.controls.Mode
import com.base.cameraview.controls.PictureFormat
import com.base.cameraview.controls.WhiteBalance
import com.base.cameraview.engine.mappers.Camera1Mapper.Companion.get
import com.base.cameraview.engine.metering.Camera1MeteringTransform
import com.base.cameraview.engine.offset.Axis
import com.base.cameraview.engine.offset.Reference
import com.base.cameraview.engine.options.Camera1Options
import com.base.cameraview.engine.orchestrator.CameraState
import com.base.cameraview.frame.ByteBufferFrameManager
import com.base.cameraview.frame.ByteBufferFrameManager.BufferCallback
import com.base.cameraview.frame.FrameManager
import com.base.cameraview.gesture.Gesture
import com.base.cameraview.internal.CropHelper
import com.base.cameraview.metering.MeteringRegions
import com.base.cameraview.metering.MeteringTransform
import com.base.cameraview.picture.Full1PictureRecorder
import com.base.cameraview.picture.Snapshot1PictureRecorder
import com.base.cameraview.picture.SnapshotGlPictureRecorder
import com.base.cameraview.preview.RendererCameraPreview
import com.base.cameraview.size.AspectRatio
import com.base.cameraview.size.Size
import com.base.cameraview.video.Full1VideoRecorder
import com.base.cameraview.video.SnapshotVideoRecorder
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import java.io.IOException
import java.util.Collections
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class Camera1Engine(
    callback: Callback
) : CameraBaseEngine(callback), PreviewCallback, ErrorCallback, BufferCallback {
    private val mMapper = get()

    @VisibleForTesting
    var mCameraId: Int = 0
    private var mCamera: Camera? = null

    //region Utilities
    override fun onError(error: Int, camera: Camera?) {
        val message = LOG.e("Internal Camera1 error.", error)
        val runtime: Exception = RuntimeException(message)
        val reason: Int = when (error) {
            Camera.CAMERA_ERROR_SERVER_DIED, Camera.CAMERA_ERROR_EVICTED -> CameraException.REASON_DISCONNECTED

            Camera.CAMERA_ERROR_UNKNOWN -> CameraException.REASON_DISCONNECTED
            else -> CameraException.REASON_UNKNOWN
        }
        throw CameraException(runtime, reason)
    }

    //endregion
    //region Protected APIs
    override val previewStreamAvailableSizes: MutableList<Size>
        get() = getPreviewStreamAvailableSizesEngine1()

    @EngineThread
    fun getPreviewStreamAvailableSizesEngine1(): MutableList<Size> {
        val sizes: MutableList<Camera.Size>
        try {
            sizes = mCamera!!.parameters.supportedPreviewSizes
        } catch (e: Exception) {
            LOG.e(
                "getPreviewStreamAvailableSizes:",
                "Failed to compute preview size. Camera params is empty"
            )
            throw CameraException(e, CameraException.REASON_FAILED_TO_START_PREVIEW)
        }
        val result: MutableList<Size> = ArrayList(sizes.size)
        for (size in sizes) {
            val add = Size(size.width, size.height)
            if (!result.contains(add)) result.add(add)
        }
        LOG.i("getPreviewStreamAvailableSizes:", result)
        return result
    }

    override val frameProcessingAvailableSizes: MutableList<Size>
        @EngineThread get() = if (mPreviewStreamSize == null) {
            mutableListOf()
        } else {
            mutableListOf(mPreviewStreamSize) as MutableList<Size>
        }


    @EngineThread
    override fun onPreviewStreamSizeChanged() {
        restartPreview()
    }

    @EngineThread
    override fun collectCameraInfo(facing: Facing): Boolean {
        val internalFacing = mMapper.mapFacing(facing)
        LOG.i(
            "collectCameraInfo",
            "Facing:",
            facing,
            "Internal:",
            internalFacing,
            "Cameras:",
            Camera.getNumberOfCameras()
        )
        val cameraInfo = CameraInfo()
        var i = 0
        val count = Camera.getNumberOfCameras()
        while (i < count) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == internalFacing) {
                angles.setSensorOffset(facing, cameraInfo.orientation)
                mCameraId = i
                return true
            }
            i++
        }
        return false
    }

    //endregion
    //region Start
    @EngineThread
    override fun onStartEngine(): Task<CameraOptions?> {
        try {
            mCamera = Camera.open(mCameraId)
        } catch (e: Exception) {
            LOG.e("onStartEngine:", "Failed to connect. Maybe in use by another app?")
            throw CameraException(e, CameraException.REASON_FAILED_TO_CONNECT)
        }
        if (mCamera == null) {
            LOG.e(
                "onStartEngine:",
                "Failed to connect. Camera is null, maybe in use by another app or already released?"
            )
            throw CameraException(CameraException.REASON_FAILED_TO_CONNECT)
        }
        mCamera!!.setErrorCallback(this)

        // Set parameters that might have been set before the camera was opened.
        LOG.i("onStartEngine:", "Applying default parameters.")
        try {
            val params = mCamera!!.parameters
            mCameraOptions = Camera1Options(
                params, mCameraId, angles.flip(Reference.SENSOR, Reference.VIEW)
            )
            applyAllParameters(params)
            mCamera!!.parameters = params
        } catch (e: Exception) {
            LOG.e("onStartEngine:", "Failed to connect. Problem with camera params")
            throw CameraException(e, CameraException.REASON_FAILED_TO_CONNECT)
        }
        try {
            mCamera!!.setDisplayOrientation(
                angles.offset(
                    Reference.SENSOR, Reference.VIEW, Axis.ABSOLUTE
                )
            ) // <- not allowed during preview
        } catch (e: Exception) {
            LOG.e(
                "onStartEngine:",
                "Failed to connect. Can't set display orientation, maybe preview already exists?"
            )
            throw CameraException(CameraException.REASON_FAILED_TO_CONNECT)
        }
        LOG.i("onStartEngine:", "Ended")
        return Tasks.forResult<CameraOptions?>(mCameraOptions)
    }

    @EngineThread
    override fun onStartBind(): Task<Void?> {
        LOG.i("onStartBind:", "Started")
        check(mPreview == null) { "Preview is null" }
        try {
            if (mPreview?.getOutputClass() == SurfaceHolder::class.java) {
                mCamera!!.setPreviewDisplay(mPreview!!.getOutput() as SurfaceHolder)
            } else if (mPreview!!.getOutputClass() == SurfaceTexture::class.java) {
                mCamera!!.setPreviewTexture(mPreview!!.getOutput() as SurfaceTexture)
            } else {
                throw RuntimeException("Unknown CameraPreview output class.")
            }
        } catch (e: IOException) {
            LOG.e("onStartBind:", "Failed to bind.", e)
            throw CameraException(e, CameraException.REASON_FAILED_TO_START_PREVIEW)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        mCaptureSize = computeCaptureSize()
        mPreviewStreamSize = computePreviewStreamSize()
        LOG.i("onStartBind:", "Returning")
        return Tasks.forResult<Void?>(null)
    }

    @EngineThread
    override fun onStartPreview(): Task<Void?> {
        LOG.i("onStartPreview", "Dispatching onCameraPreviewStreamSizeChanged.")
        callback.onCameraPreviewStreamSizeChanged()

        val previewSize = getPreviewStreamSize(Reference.VIEW)
        checkNotNull(previewSize) { "previewStreamSize should not be null at this point." }
        mPreview?.setStreamSize(previewSize.width, previewSize.height)
        mPreview?.setDrawRotation(0)

        val params: Camera.Parameters
        try {
            params = mCamera!!.parameters
        } catch (e: Exception) {
            LOG.e(
                "onStartPreview:",
                "Failed to get params from camera. Maybe low level problem with camera or camera has already released?"
            )
            throw CameraException(e, CameraException.REASON_FAILED_TO_START_PREVIEW)
        }
        // NV21 should be the default, but let's make sure, since YuvImage will only support this
        // and a few others
        params.previewFormat = ImageFormat.NV21
        // setPreviewSize is not allowed during preview
        params.setPreviewSize(mPreviewStreamSize!!.width, mPreviewStreamSize!!.height)
        if (mode == Mode.PICTURE) {
            // setPictureSize is allowed during preview
            params.setPictureSize(mCaptureSize!!.width, mCaptureSize!!.height)
        } else {
            // mCaptureSize in this case is a video size. The available video sizes are not
            // necessarily a subset of the picture sizes, so we can't use the mCaptureSize value:
            // it might crash. However, the setPictureSize() passed here is useless : we don't allow
            // HQ pictures in video mode.
            // While this might be lifted in the future, for now, just use a picture capture size.
            val pictureSize = computeCaptureSize(Mode.PICTURE)
            params.setPictureSize(pictureSize.width, pictureSize.height)
        }
        try {
            mCamera!!.parameters = params
        } catch (e: Exception) {
            LOG.e(
                "onStartPreview:",
                "Failed to set params for camera. Maybe incorrect parameter put in params?"
            )
            throw CameraException(e, CameraException.REASON_FAILED_TO_START_PREVIEW)
        }

        mCamera!!.setPreviewCallbackWithBuffer(null) // Release anything left
        mCamera!!.setPreviewCallbackWithBuffer(this) // Add ourselves
        mPreviewStreamSize?.let {
            frameManager.setUp(PREVIEW_FORMAT, mPreviewStreamSize!!, angles)
        }

        LOG.i("onStartPreview", "Starting preview with startPreview().")
        try {
            mCamera!!.startPreview()
        } catch (e: Exception) {
            LOG.e("onStartPreview", "Failed to start preview.", e)
            throw CameraException(e, CameraException.REASON_FAILED_TO_START_PREVIEW)
        }
        LOG.i("onStartPreview", "Started preview.")
        return Tasks.forResult<Void?>(null)
    }

    //endregion
    //region Stop
    @EngineThread
    override fun onStopPreview(): Task<Void?> {
        LOG.i("onStopPreview:", "Started.")
        mVideoRecorder?.stop(true)
        mVideoRecorder = null
        mPictureRecorder = null
        frameManager.release()
        LOG.i("onStopPreview:", "Releasing preview buffers.")
        mCamera!!.setPreviewCallbackWithBuffer(null) // Release anything left
        try {
            LOG.i("onStopPreview:", "Stopping preview.")
            mCamera!!.stopPreview()
            LOG.i("onStopPreview:", "Stopped preview.")
        } catch (e: Exception) {
            LOG.e("stopPreview", "Could not stop preview", e)
        }
        return Tasks.forResult<Void?>(null)
    }

    @EngineThread
    override fun onStopBind(): Task<Void?> {
        mPreviewStreamSize = null
        mCaptureSize = null
        try {
            if (mPreview?.getOutputClass() == SurfaceHolder::class.java) {
                mCamera!!.setPreviewDisplay(null)
            } else if (mPreview?.getOutputClass() == SurfaceTexture::class.java) {
                mCamera!!.setPreviewTexture(null)
            } else {
                throw RuntimeException("Unknown CameraPreview output class.")
            }
        } catch (e: IOException) {
            // NOTE: when this happens, the next onStopEngine() call hangs on camera.release(),
            // Not sure for how long. This causes the destroy() flow to fail the timeout.
            LOG.e("onStopBind", "Could not release surface", e)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return Tasks.forResult<Void?>(null)
    }

    @EngineThread
    override fun onStopEngine(): Task<Void?> {
        LOG.i("onStopEngine:", "About to clean up.")
        orchestrator.remove(JOB_FOCUS_RESET)
        orchestrator.remove(JOB_FOCUS_END)
        if (mCamera != null) {
            try {
                LOG.i("onStopEngine:", "Clean up.", "Releasing camera.")
                // Just like Camera2Engine, this call can hang (at least on emulators) and if
                // we don't find a way around the lock, it leaves the camera in a bad state.
                // This is anticipated by the exception in onStopBind() (see above).
                //
                // 12:29:32.163 E Camera3-Device: Camera 0: clearStreamingRequest: Device has encountered a serious error[0m
                // 12:29:32.163 E Camera2-StreamingProcessor: stopStream: Camera 0: Can't clear stream request: Function not implemented (-38)[0m
                // 12:29:32.163 E Camera2Client: stopPreviewL: Camera 0: Can't stop streaming: Function not implemented (-38)[0m
                // 12:29:32.273 E Camera2-StreamingProcessor: deletePreviewStream: Unable to delete old preview stream: Device or resource busy (-16)[0m
                // 12:29:32.274 E Camera2-CallbackProcessor: deleteStream: Unable to delete callback stream: Device or resource busy (-16)[0m
                // 12:29:32.274 E Camera3-Device: Camera 0: disconnect: Shutting down in an error state[0m
                //
                // I believe there is a thread deadlock due to this call internally waiting to
                // dispatch some callback to us (pending captures, ...), but the callback thread
                // is blocked here. We try to workaround this in CameraEngine.destroy().
                mCamera!!.release()
                LOG.i("onStopEngine:", "Clean up.", "Released camera.")
            } catch (e: Exception) {
                LOG.w("onStopEngine:", "Clean up.", "Exception while releasing camera.", e)
            }
            mCamera = null
            mCameraOptions = null
        }
        mVideoRecorder = null
        mCameraOptions = null
        mCamera = null
        LOG.w("onStopEngine:", "Clean up.", "Returning.")
        return Tasks.forResult<Void?>(null)
    }

    //endregion
    //region Pictures
    @EngineThread
    override fun onTakePicture(stub: PictureResult.Stub, doMetering: Boolean) {
        LOG.i("onTakePicture:", "executing.")
        stub.rotation = angles.offset(
            Reference.SENSOR, Reference.OUTPUT, Axis.RELATIVE_TO_SENSOR
        )
        stub.size = getPictureSize(Reference.OUTPUT)
        mPictureRecorder = Full1PictureRecorder(stub, this, mCamera!!)
        mPictureRecorder?.take()
        LOG.i("onTakePicture:", "executed.")
    }

    @EngineThread
    override fun onTakePictureSnapshot(
        stub: PictureResult.Stub, outputRatio: AspectRatio, doMetering: Boolean
    ) {
        LOG.i("onTakePictureSnapshot:", "executing.")
        // Not the real size: it will be cropped to match the view ratio
        stub.size = getUncroppedSnapshotSize(Reference.OUTPUT)
        if (mPreview is RendererCameraPreview) {
            stub.rotation = angles.offset(Reference.VIEW, Reference.OUTPUT, Axis.ABSOLUTE)
            mPictureRecorder = SnapshotGlPictureRecorder(
                stub, this, (mPreview as RendererCameraPreview?)!!, outputRatio, overlay
            )
        } else {
            stub.rotation =
                angles.offset(Reference.SENSOR, Reference.OUTPUT, Axis.RELATIVE_TO_SENSOR)
            mPictureRecorder = Snapshot1PictureRecorder(stub, this, mCamera!!, outputRatio)
        }
        mPictureRecorder?.take()
        LOG.i("onTakePictureSnapshot:", "executed.")
    }

    //endregion
    //region Videos
    @EngineThread
    override fun onTakeVideo(stub: VideoResult.Stub) {
        stub.rotation = angles.offset(
            Reference.SENSOR, Reference.OUTPUT, Axis.RELATIVE_TO_SENSOR
        )
        stub.size = if (angles.flip(Reference.SENSOR, Reference.OUTPUT)) mCaptureSize?.flip()
        else mCaptureSize
        // Unlock the camera and start recording.
        try {
            mCamera!!.unlock()
        } catch (e: Exception) {
            // If this failed, we are unlikely able to record the video.
            // Dispatch an error.
            onVideoResult(null, e)
            return
        }
        mVideoRecorder = Full1VideoRecorder(this@Camera1Engine, mCamera!!, mCameraId)
        mVideoRecorder?.start(stub)
    }

    @SuppressLint("NewApi")
    @EngineThread
    override fun onTakeVideoSnapshot(
        stub: VideoResult.Stub, outputRatio: AspectRatio
    ) {
        check(mPreview is RendererCameraPreview) { "Video snapshots are only supported with GL_SURFACE." }
        val glPreview = mPreview as RendererCameraPreview
        var outputSize = getUncroppedSnapshotSize(Reference.OUTPUT)
        checkNotNull(outputSize) { "outputSize should not be null." }
        val outputCrop = CropHelper.computeCrop(outputSize, outputRatio)
        outputSize = Size(outputCrop.width(), outputCrop.height())
        stub.size = outputSize
        // Vertical:               0   (270-0-0)
        // Left (unlocked):        0   (270-90-270)
        // Right (unlocked):       0   (270-270-90)
        // Upside down (unlocked): 0   (270-180-180)
        // Left (locked):          270 (270-0-270)
        // Right (locked):         90  (270-0-90)
        // Upside down (locked):   180 (270-0-180)
        // The correct formula seems to be deviceOrientation+displayOffset,
        // which means offset(Reference.VIEW, Reference.OUTPUT, Axis.ABSOLUTE).
        stub.rotation = angles.offset(Reference.VIEW, Reference.OUTPUT, Axis.ABSOLUTE)
        stub.videoFrameRate = mPreviewFrameRate.roundToInt()
        LOG.i("onTakeVideoSnapshot", "rotation:", stub.rotation, "size:", stub.size)

        // Start.
        mVideoRecorder = SnapshotVideoRecorder(this@Camera1Engine, glPreview, overlay)
        try {
            mVideoRecorder?.start(stub)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onVideoResult(result: VideoResult.Stub?, exception: Exception?) {
        super.onVideoResult(result, exception)
        if (result == null) {
            // Something went wrong, lock the camera again.
            mCamera!!.lock()
        }
    }

    //endregion
    //region Parameters
    private fun applyAllParameters(params: Camera.Parameters) {
        params.setRecordingHint(mode == Mode.VIDEO)
        applyDefaultFocus(params)
        applyFlash(params, Flash.OFF)
        applyLocation(params, null)
        applyWhiteBalance(params, WhiteBalance.AUTO)
        applyHdr(params, Hdr.OFF)
        applyZoom(params, 0f)
        applyExposureCorrection(params, 0f)
        applyPlaySounds(mPlaySounds)
        applyPreviewFrameRate(params, 0f)
    }

    private fun applyDefaultFocus(params: Camera.Parameters) {
        val modes = params.supportedFocusModes

        if (mode == Mode.VIDEO && modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
            return
        }

        if (modes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            params.focusMode = Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
            return
        }

        if (modes.contains(Camera.Parameters.FOCUS_MODE_INFINITY)) {
            params.focusMode = Camera.Parameters.FOCUS_MODE_INFINITY
            return
        }

        if (modes.contains(Camera.Parameters.FOCUS_MODE_FIXED)) {
            params.focusMode = Camera.Parameters.FOCUS_MODE_FIXED
            return
        }
    }

    override fun setFlash(flash: Flash) {
        val old = mFlash
        mFlash = flash
        mFlashTask = orchestrator.scheduleStateful(
            "flash ($flash)", CameraState.ENGINE
        ) {
            val params = mCamera!!.parameters
            if (applyFlash(params, old!!)) mCamera!!.parameters = params
        }
    }

    private fun applyFlash(params: Camera.Parameters, oldFlash: Flash): Boolean {
        if (mFlash == null) return false
        if (mCameraOptions?.supports(mFlash!!) == true) {
            params.flashMode = mMapper.mapFlash(mFlash!!)
            return true
        }
        mFlash = oldFlash
        return false
    }

    override fun setLocation(location: Location?) {
        val oldLocation = mLocation
        mLocation = location
        mLocationTask = orchestrator.scheduleStateful(
            "location", CameraState.ENGINE
        ) {
            val params = mCamera!!.parameters
            if (applyLocation(params, oldLocation)) mCamera!!.parameters = params
        }
    }

    private fun applyLocation(
        params: Camera.Parameters, @Suppress("unused") oldLocation: Location?
    ): Boolean {
        if (mLocation != null) {
            params.setGpsLatitude(mLocation!!.latitude)
            params.setGpsLongitude(mLocation!!.longitude)
            params.setGpsAltitude(mLocation!!.altitude)
            params.setGpsTimestamp(mLocation!!.time)
            params.setGpsProcessingMethod(mLocation!!.provider)
        }
        return true
    }

    override fun setWhiteBalance(whiteBalance: WhiteBalance) {
        val old = mWhiteBalance
        mWhiteBalance = whiteBalance
        mWhiteBalanceTask = orchestrator.scheduleStateful(
            "white balance ($whiteBalance)", CameraState.ENGINE
        ) {
            val params = mCamera!!.parameters
            old?.let {
                if (applyWhiteBalance(params, old)) mCamera!!.parameters = params
            }
        }
    }

    private fun applyWhiteBalance(
        params: Camera.Parameters, oldWhiteBalance: WhiteBalance
    ): Boolean {

        if (mWhiteBalance == null) {
            return false
        }

        if (mCameraOptions?.supports(mWhiteBalance!!) == true) {
            // If this lock key is present, the engine can throw when applying the
            // parameters, not sure why. Since we never lock it, this should be
            // harmless for the rest of the engine.
            params.whiteBalance = mMapper.mapWhiteBalance(mWhiteBalance!!)
            params.remove("auto-whitebalance-lock")
            return true
        }
        mWhiteBalance = oldWhiteBalance
        return false
    }

    override fun setHdr(hdr: Hdr) {
        val old = mHdr
        mHdr = hdr
        mHdrTask = orchestrator.scheduleStateful(
            "hdr ($hdr)", CameraState.ENGINE
        ) {
            val params = mCamera!!.parameters
            old?.let {
                if (applyHdr(params, old)) mCamera!!.parameters = params
            }
        }
    }

    private fun applyHdr(params: Camera.Parameters, oldHdr: Hdr): Boolean {
        if (mHdr == null) {
            return false
        }
        if (mCameraOptions?.supports(mHdr!!) == true) {
            params.sceneMode = mMapper.mapHdr(mHdr!!)
            return true
        }
        mHdr = oldHdr
        return false
    }

    override fun setZoom(zoom: Float, points: Array<PointF?>?, notify: Boolean) {
        val old = mZoomValue
        mZoomValue = zoom
        // Zoom requests can be high frequency (e.g. linked to touch events), let's trim the oldest.
        orchestrator.trim("zoom", ALLOWED_ZOOM_OPS)
        mZoomTask = orchestrator.scheduleStateful(
            "zoom", CameraState.ENGINE
        ) {
            val params = mCamera!!.parameters
            if (applyZoom(params, old)) {
                mCamera!!.parameters = params
                if (notify) {
                    callback.dispatchOnZoomChanged(mZoomValue, points)
                }
            }
        }
    }

    private fun applyZoom(params: Camera.Parameters, oldZoom: Float): Boolean {
        if (mCameraOptions?.isZoomSupported == true) {
            val max = params.maxZoom.toFloat()
            params.zoom = (mZoomValue * max).toInt()
            mCamera!!.parameters = params
            return true
        }
        mZoomValue = oldZoom
        return false
    }

    override fun setExposureCorrection(
        EVvalue: Float, bounds: FloatArray, points: Array<PointF?>?, notify: Boolean
    ) {
        val old = mExposureCorrectionValue
        mExposureCorrectionValue = EVvalue
        // EV requests can be high frequency (e.g. linked to touch events), let's trim the oldest.
        orchestrator.trim("exposure correction", ALLOWED_EV_OPS)
        mExposureCorrectionTask = orchestrator.scheduleStateful(
            "exposure correction", CameraState.ENGINE
        ) {
            val params = mCamera!!.parameters
            if (applyExposureCorrection(params, old)) {
                mCamera!!.parameters = params
                if (notify) {
                    callback.dispatchOnExposureCorrectionChanged(
                        mExposureCorrectionValue, bounds, points
                    )
                }
            }
        }
    }

    private fun applyExposureCorrection(
        params: Camera.Parameters, oldExposureCorrection: Float
    ): Boolean {
        if (mCameraOptions == null) {
            return false
        }
        if (mCameraOptions!!.isExposureCorrectionSupported) {
            // Just make sure we're inside boundaries.
            val max = mCameraOptions!!.getExposureCorrectionMaxValue()
            val min = mCameraOptions!!.getExposureCorrectionMinValue()
            mExposureCorrectionValue = mExposureCorrectionValue.coerceIn(min, max)
            // Apply.
            val indexValue = (mExposureCorrectionValue / params.exposureCompensationStep).toInt()
            params.exposureCompensation = indexValue
            return true
        }
        mExposureCorrectionValue = oldExposureCorrection
        return false
    }

    override fun setPlaySounds(playSounds: Boolean) {
        val old = mPlaySounds
        mPlaySounds = playSounds
        mPlaySoundsTask = orchestrator.scheduleStateful(
            "play sounds ($playSounds)", CameraState.ENGINE
        ) { applyPlaySounds(old) }
    }

    private fun applyPlaySounds(oldPlaySound: Boolean): Boolean {
        val info = CameraInfo()
        Camera.getCameraInfo(mCameraId, info)
        if (info.canDisableShutterSound) {
            return try {
                // this method is documented to throw on some occasions. #377
                mCamera!!.enableShutterSound(mPlaySounds)
            } catch (exception: RuntimeException) {
                false
            }
        }
        if (mPlaySounds) {
            return true
        }
        mPlaySounds = oldPlaySound
        return false
    }

    override fun setPreviewFrameRate(previewFrameRate: Float) {
        val old = previewFrameRate
        mPreviewFrameRate = previewFrameRate
        mPreviewFrameRateTask = orchestrator.scheduleStateful(
            "preview fps ($previewFrameRate)", CameraState.ENGINE
        ) {
            val params = mCamera!!.parameters
            if (applyPreviewFrameRate(params, old)) mCamera!!.parameters = params
        }
    }

    private fun applyPreviewFrameRate(
        params: Camera.Parameters, oldPreviewFrameRate: Float
    ): Boolean {
        val fpsRanges = params.supportedPreviewFpsRange
        sortRanges(fpsRanges)
        if (mPreviewFrameRate == 0f) {
            // 0F is a special value. Fallback to a reasonable default.
            for (fpsRange in fpsRanges) {
                val lower = fpsRange[0].toFloat() / 1000f
                val upper = fpsRange[1].toFloat() / 1000f
                if ((lower <= 30f && 30f <= upper) || (lower <= 24f && 24f <= upper)) {
                    params.setPreviewFpsRange(fpsRange[0], fpsRange[1])
                    return true
                }
            }
        } else {
            // If out of boundaries, adjust it.
            if (mCameraOptions == null) {
                return false
            }
            mPreviewFrameRate = min(
                mPreviewFrameRate, mCameraOptions!!.getPreviewFrameRateMaxValue()
            )
            mPreviewFrameRate = max(
                mPreviewFrameRate, mCameraOptions!!.getPreviewFrameRateMinValue()
            )
            for (fpsRange in fpsRanges) {
                val lower = fpsRange[0].toFloat() / 1000f
                val upper = fpsRange[1].toFloat() / 1000f
                val rate = mPreviewFrameRate.roundToInt().toFloat()
                if (lower <= rate && rate <= upper) {
                    params.setPreviewFpsRange(fpsRange[0], fpsRange[1])
                    return true
                }
            }
        }
        mPreviewFrameRate = oldPreviewFrameRate
        return false
    }

    private fun sortRanges(fpsRanges: MutableList<IntArray>) {
        if (previewFrameRateExact && mPreviewFrameRate != 0f) { // sort by range width in ascending order
            Collections.sort(
                fpsRanges,
                Comparator<IntArray> { range1, range2 -> (range1[1] - range1[0]) - (range2[1] - range2[0]) })
        } else { // sort by range width in descending order
            Collections.sort(
                fpsRanges,
                Comparator<IntArray> { range1, range2 -> (range2[1] - range2[0]) - (range1[1] - range1[0]) })
        }
    }

    override fun setPictureFormat(pictureFormat: PictureFormat) {
        if (pictureFormat != PictureFormat.JPEG) {
            throw UnsupportedOperationException("Unsupported picture format: $pictureFormat")
        }
        mPictureFormat = pictureFormat
    }

    //endregion
    //region Frame Processing
    override fun instantiateFrameManager(poolSize: Int): FrameManager<*> {
        return ByteBufferFrameManager(poolSize, this)
    }

    override fun getFrameManager(): ByteBufferFrameManager {
        return super.getFrameManager() as ByteBufferFrameManager
    }

    override fun setHasFrameProcessors(hasFrameProcessors: Boolean) {
        // we don't care, FP is always on
        mHasFrameProcessors = hasFrameProcessors
    }

    override fun setFrameProcessingFormat(format: Int) {
        // Ignore input: we only support NV21.
        mFrameProcessingFormat = ImageFormat.NV21
    }

    override fun onBufferAvailable(buffer: ByteArray) {
        if (state.isAtLeast(CameraState.ENGINE) && targetState.isAtLeast(CameraState.ENGINE)) {
            mCamera!!.addCallbackBuffer(buffer)
        }
    }

    override fun onPreviewFrame(data: ByteArray?, camera: Camera?) {
        if (data == null) {
            // Seen this happen in logs.
            return
        }
        val frame = frameManager.getFrame(data, System.currentTimeMillis())
        if (frame != null) {
            callback.dispatchFrame(frame)
        }
    }

    //endregion
    //region Auto Focus
    override fun startAutoFocus(
        gesture: Gesture?, regions: MeteringRegions, legacyPoint: PointF
    ) {
        orchestrator.scheduleStateful("auto focus", CameraState.BIND, object : Runnable {
            override fun run() {
                if (mCameraOptions == null) {
                    return
                }
                if (!mCameraOptions!!.isAutoFocusSupported) return
                val transform: MeteringTransform<Camera.Area?> = Camera1MeteringTransform(
                    angles, preview.surfaceSize
                )
                val transformed = regions.transform(transform)

                val params = mCamera!!.parameters
                val maxAF = params.maxNumFocusAreas
                val maxAE = params.maxNumMeteringAreas
                if (maxAF > 0) params.focusAreas = transformed.get<Camera.Area?>(maxAF, transform)
                if (maxAE > 0) params.meteringAreas = transformed.get<Camera.Area?>(
                    maxAE, transform
                )
                params.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
                try {
                    mCamera!!.parameters = params
                } catch (re: RuntimeException) {
                    LOG.e("startAutoFocus:", "Failed to set camera parameters")
                    throw CameraException(re, CameraException.REASON_UNKNOWN)
                }
                callback.dispatchOnFocusStart(gesture, legacyPoint)

                // The auto focus callback is not guaranteed to be called, but we really want it
                // to be. So we remove the old runnable if still present and post a new one.
                orchestrator.remove(JOB_FOCUS_END)
                orchestrator.scheduleDelayed(
                    JOB_FOCUS_END, true, AUTOFOCUS_END_DELAY_MILLIS.toLong()
                ) { callback.dispatchOnFocusEnd(gesture, false, legacyPoint) }

                // Wrapping autoFocus in a try catch to handle some device specific exceptions,
                // see See https://github.com/natario1/CameraView/issues/181.
                try {
                    mCamera!!.autoFocus { success, camera ->
                        orchestrator.remove(JOB_FOCUS_END)
                        orchestrator.remove(JOB_FOCUS_RESET)
                        callback.dispatchOnFocusEnd(gesture, success, legacyPoint)
                        if (shouldResetAutoFocus()) {
                            orchestrator.scheduleStatefulDelayed(
                                JOB_FOCUS_RESET, CameraState.ENGINE, autoFocusResetDelay
                            ) {
                                mCamera!!.cancelAutoFocus()
                                val params = mCamera!!.parameters
                                val maxAF = params.maxNumFocusAreas
                                val maxAE = params.maxNumMeteringAreas
                                if (maxAF > 0) params.focusAreas = null
                                if (maxAE > 0) params.meteringAreas = null
                                applyDefaultFocus(params) // Revert to internal focus.
                                mCamera!!.parameters = params
                            }
                        }
                    }
                } catch (e: RuntimeException) {
                    LOG.e("startAutoFocus:", "Error calling autoFocus", e)
                    // Let the mFocusEndRunnable do its job. (could remove it and quickly dispatch
                    // onFocusEnd here, but let's make it simpler).
                }
            }
        })
    } //endregion

    companion object {
        @VisibleForTesting
        const val AUTOFOCUS_END_DELAY_MILLIS: Int = 2500
        private const val JOB_FOCUS_RESET = "focus reset"
        private const val JOB_FOCUS_END = "focus end"
        private const val PREVIEW_FORMAT = ImageFormat.NV21
    }
}

