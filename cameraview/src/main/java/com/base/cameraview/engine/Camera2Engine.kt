package com.base.cameraview.engine

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.location.Location
import android.media.Image
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import android.util.Pair
import android.util.Range
import android.util.Rational
import android.view.Surface
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
import com.base.cameraview.engine.action.Action
import com.base.cameraview.engine.action.ActionHolder
import com.base.cameraview.engine.action.Actions.sequence
import com.base.cameraview.engine.action.Actions.timeout
import com.base.cameraview.engine.action.BaseAction
import com.base.cameraview.engine.action.CompletionCallback
import com.base.cameraview.engine.action.LogAction
import com.base.cameraview.engine.mappers.Camera2Mapper.Companion.get
import com.base.cameraview.engine.meter.MeterAction
import com.base.cameraview.engine.meter.MeterResetAction
import com.base.cameraview.engine.offset.Axis
import com.base.cameraview.engine.offset.Reference
import com.base.cameraview.engine.options.Camera2Options
import com.base.cameraview.engine.orchestrator.CameraState
import com.base.cameraview.frame.FrameManager
import com.base.cameraview.frame.ImageFrameManager
import com.base.cameraview.gesture.Gesture
import com.base.cameraview.internal.CropHelper
import com.base.cameraview.internal.FpsRangeValidator
import com.base.cameraview.metering.MeteringRegions
import com.base.cameraview.picture.Full2PictureRecorder
import com.base.cameraview.picture.Snapshot2PictureRecorder
import com.base.cameraview.preview.RendererCameraPreview
import com.base.cameraview.size.AspectRatio
import com.base.cameraview.size.Size
import com.base.cameraview.video.Full2VideoRecorder
import com.base.cameraview.video.Full2VideoRecorder.PrepareException
import com.base.cameraview.video.SnapshotVideoRecorder
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import java.util.Arrays
import java.util.concurrent.Callable
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.ExecutionException
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class Camera2Engine(callback: Callback) : CameraBaseEngine(callback), OnImageAvailableListener,
    ActionHolder {
    private val mManager: CameraManager =
        getCallback().getContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val mMapper = get()
    private val mPictureCaptureStopsPreview = false // can be configurable at some point

    // Actions
    // Use COW to properly synchronize the list. We'll iterate much more than mutate
    private val mActions: MutableList<Action> = CopyOnWriteArrayList()
    private var mCameraId: String? = null
    private var mCamera: CameraDevice? = null
    private var mCameraCharacteristics: CameraCharacteristics? = null
    private var mSession: CameraCaptureSession? = null
    private var mRepeatingRequestBuilder: CaptureRequest.Builder? = null
    private var mLastRepeatingResult: TotalCaptureResult? = null
    private val mRepeatingRequestCallback: CaptureCallback = object : CaptureCallback() {
        override fun onCaptureStarted(
            session: CameraCaptureSession,
            request: CaptureRequest,
            timestamp: Long,
            frameNumber: Long
        ) {
            for (action in mActions) {
                action.onCaptureStarted(this@Camera2Engine, request)
            }
        }

        override fun onCaptureProgressed(
            session: CameraCaptureSession, request: CaptureRequest, partialResult: CaptureResult
        ) {
            for (action in mActions) {
                action.onCaptureProgressed(this@Camera2Engine, request, partialResult)
            }
        }

        override fun onCaptureCompleted(
            session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult
        ) {
            mLastRepeatingResult = result
            for (action in mActions) {
                action.onCaptureCompleted(this@Camera2Engine, request, result)
            }
        }
    }

    // Frame processing
    private var mFrameProcessingReader: ImageReader? =
        null // need this or the reader surface is collected
    private var mFrameProcessingSurface: Surface? = null

    // Preview
    private var mPreviewStreamSurface: Surface? = null

    // Video recording
    // When takeVideo is called, we restart the session.
    private var mFullVideoPendingStub: VideoResult.Stub? = null

    // Picture capturing
    private var mPictureReader: ImageReader? = null
    private var mMeterAction: MeterAction? = null

    //region Utilities
    init {
        LogAction().start(this)
    }

    @VisibleForTesting
    fun <T> readCharacteristic(key: CameraCharacteristics.Key<T?>, fallback: T): T {
        return readCharacteristic(mCameraCharacteristics!!, key, fallback)
    }

    private fun <T> readCharacteristic(
        characteristics: CameraCharacteristics, key: CameraCharacteristics.Key<T?>, fallback: T
    ): T {
        val value = characteristics.get<T?>(key)
        return value ?: fallback
    }

    private fun createCameraException(exception: CameraAccessException): CameraException {
        val reason: Int = when (exception.reason) {
            CameraAccessException.CAMERA_DISABLED, CameraAccessException.CAMERA_IN_USE, CameraAccessException.MAX_CAMERAS_IN_USE -> {
                CameraException.REASON_FAILED_TO_CONNECT
            }

            CameraAccessException.CAMERA_ERROR, CameraAccessException.CAMERA_DISCONNECTED -> {
                CameraException.REASON_DISCONNECTED
            }

            else -> {
                CameraException.REASON_UNKNOWN
            }
        }
        return CameraException(exception, reason)
    }

    private fun createCameraException(stateCallbackError: Int): CameraException {
        val reason: Int = when (stateCallbackError) {
            CameraDevice.StateCallback.ERROR_CAMERA_DISABLED, CameraDevice.StateCallback.ERROR_CAMERA_DEVICE, CameraDevice.StateCallback.ERROR_CAMERA_SERVICE, CameraDevice.StateCallback.ERROR_CAMERA_IN_USE, CameraDevice.StateCallback.ERROR_MAX_CAMERAS_IN_USE -> {
                CameraException.REASON_FAILED_TO_CONNECT
            }

            else -> {
                CameraException.REASON_UNKNOWN
            }
        }
        return CameraException(reason)
    }

    /**
     * When creating a new builder, we want to
     * - set it to [.mRepeatingRequestBuilder], the current one
     * - add a tag for the template just in case
     * - apply all the current parameters
     */
    @Throws(CameraAccessException::class)
    private fun createRepeatingRequestBuilder(template: Int): CaptureRequest.Builder {
        val oldBuilder = mRepeatingRequestBuilder
        mRepeatingRequestBuilder = mCamera!!.createCaptureRequest(template)
        mRepeatingRequestBuilder!!.setTag(template)
        applyAllParameters(mRepeatingRequestBuilder!!, oldBuilder)
        return mRepeatingRequestBuilder!!
    }

    /**
     * Sets up the repeating request builder with default surfaces and extra ones
     * if needed (like a video recording surface).
     */
    private fun addRepeatingRequestBuilderSurfaces(vararg extraSurfaces: Surface) {
        mRepeatingRequestBuilder!!.addTarget(mPreviewStreamSurface!!)
        if (mFrameProcessingSurface != null) {
            mRepeatingRequestBuilder!!.addTarget(mFrameProcessingSurface!!)
        }
        for (extraSurface in extraSurfaces) {
            requireNotNull(extraSurface) { "Should not add a null surface." }
            mRepeatingRequestBuilder!!.addTarget(extraSurface)
        }
    }

    /**
     * Removes default surfaces from the repeating request builder.
     */
    private fun removeRepeatingRequestBuilderSurfaces() {
        mRepeatingRequestBuilder!!.removeTarget(mPreviewStreamSurface!!)
        if (mFrameProcessingSurface != null) {
            mRepeatingRequestBuilder!!.removeTarget(mFrameProcessingSurface!!)
        }
    }

    private val repeatingRequestDefaultTemplate: Int
        /**
         * Can be changed to select something different than [CameraDevice.TEMPLATE_PREVIEW]
         * for the default repeating request.
         *
         * @return the default template for preview
         */
        get() = CameraDevice.TEMPLATE_PREVIEW

    /**
     * Applies the repeating request builder to the preview, assuming we actually have a preview
     * running. Can be called after changing parameters to the builder.
     *
     *
     * To apply a new builder (for example switch between TEMPLATE_PREVIEW and TEMPLATE_RECORD)
     * it should be set before calling this method, for example by calling
     * [.createRepeatingRequestBuilder].
     */
    @EngineThread
    private fun applyRepeatingRequestBuilder() {
        applyRepeatingRequestBuilder(true, CameraException.REASON_DISCONNECTED)
    }

    @EngineThread
    private fun applyRepeatingRequestBuilder(checkStarted: Boolean, errorReason: Int) {
        if ((state == CameraState.PREVIEW && !isChangingState) || !checkStarted) {
            try {
                mSession!!.setRepeatingRequest(
                    mRepeatingRequestBuilder!!.build(), mRepeatingRequestCallback, null
                )
            } catch (e: CameraAccessException) {
                throw CameraException(e, errorReason)
            } catch (e: IllegalStateException) {
                // mSession is invalid - has been closed. This is extremely worrying because
                // it means that the session state and getPreviewState() are not synced.
                // This probably signals an error in the setup/teardown synchronization.
                LOG.e(
                    "applyRepeatingRequestBuilder: session is invalid!",
                    e,
                    "checkStarted:",
                    checkStarted,
                    "currentThread:",
                    Thread.currentThread().name,
                    "state:",
                    state,
                    "targetState:",
                    targetState
                )
                //                throw new CameraException(CameraException.REASON_DISCONNECTED);
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    //endregion
    //region Protected APIs
    @EngineThread
    override fun getPreviewStreamAvailableSizes(): MutableList<Size?> {
        try {
            val characteristics = mManager.getCameraCharacteristics(mCameraId!!)
            val streamMap =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            if (streamMap == null) {
                throw RuntimeException("StreamConfigurationMap is null. Should not happen.")
            }
            // This works because our previews return either a SurfaceTexture or a SurfaceHolder,
            // which are accepted class types by the getOutputSizes method.
            val sizes = streamMap.getOutputSizes(mPreview.getOutputClass())
            val candidates: MutableList<Size?> = ArrayList(sizes.size)
            for (size in sizes) {
                val add = Size(size.width, size.height)
                if (!candidates.contains(add)) candidates.add(add)
            }
            return candidates
        } catch (e: CameraAccessException) {
            throw createCameraException(e)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ArrayList()
    }

    @EngineThread
    override fun getFrameProcessingAvailableSizes(): MutableList<Size?> {
        try {
            val characteristics = mManager.getCameraCharacteristics(mCameraId!!)
            val streamMap =
                characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            if (streamMap == null) {
                throw RuntimeException("StreamConfigurationMap is null. Should not happen.")
            }
            val sizes = streamMap.getOutputSizes(mFrameProcessingFormat)
            val candidates: MutableList<Size?> = ArrayList(sizes.size)
            for (size in sizes) {
                val add = Size(size.width, size.height)
                if (!candidates.contains(add)) candidates.add(add)
            }
            return candidates
        } catch (e: CameraAccessException) {
            throw createCameraException(e)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ArrayList()
    }

    @EngineThread
    override fun onPreviewStreamSizeChanged() {
        LOG.i("onPreviewStreamSizeChanged:", "Calling restartBind().")
        restartBind()
    }

    @EngineThread
    override fun collectCameraInfo(facing: Facing): Boolean {
        val internalFacing = mMapper.mapFacing(facing)
        val cameraIds: Array<String>?
        try {
            cameraIds = mManager.cameraIdList
        } catch (e: CameraAccessException) {
            // This should never happen, I don't see how it could crash here.
            // However, let's launch an unrecoverable exception.
            throw createCameraException(e)
        }
        LOG.i(
            "collectCameraInfo",
            "Facing:",
            facing,
            "Internal:",
            internalFacing,
            "Cameras:",
            cameraIds.size
        )
        for (cameraId in cameraIds) {
            try {
                val characteristics = mManager.getCameraCharacteristics(cameraId)
                if (internalFacing == readCharacteristic<Int?>(
                        characteristics, CameraCharacteristics.LENS_FACING, -99
                    )
                ) {
                    mCameraId = cameraId
                    val sensorOffset = readCharacteristic<Int?>(
                        characteristics, CameraCharacteristics.SENSOR_ORIENTATION, 0
                    )!!
                    angles.setSensorOffset(facing, sensorOffset)
                    return true
                }
            } catch (ignore: CameraAccessException) {
                // This specific camera has been disconnected.
                // Keep searching in other cameraIds.
                ignore.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    //endregion
    //region Start
    @EngineThread
    @SuppressLint("MissingPermission")
    override fun onStartEngine(): Task<CameraOptions?> {
        val task = TaskCompletionSource<CameraOptions?>()
        try {
            // We have a valid camera for this Facing. Go on.
            mManager.openCamera(mCameraId!!, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    mCamera = camera

                    // Set parameters that might have been set before the camera was opened.
                    try {
                        LOG.i("onStartEngine:", "Opened camera device.")
                        mCameraCharacteristics = mManager.getCameraCharacteristics(mCameraId!!)
                        val flip = angles.flip(Reference.SENSOR, Reference.VIEW)
                        val format = when (mPictureFormat) {
                            PictureFormat.JPEG -> ImageFormat.JPEG
                            PictureFormat.DNG -> ImageFormat.RAW_SENSOR
                        }
                        mCameraOptions = Camera2Options(mManager, mCameraId!!, flip, format)
                        createRepeatingRequestBuilder(this@Camera2Engine.repeatingRequestDefaultTemplate)
                    } catch (e: CameraAccessException) {
                        task.trySetException(createCameraException(e))
                        return
                    } catch (e: Exception) {
                        e.printStackTrace()
                        return
                    }
                    task.trySetResult(mCameraOptions)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    // Not sure if this is called INSTEAD of onOpened() or can be called after
                    // as well. Cover both cases with an unrecoverable exception so that the
                    // engine is properly destroyed.
                    val exception = CameraException(CameraException.REASON_DISCONNECTED)
                    if (!task.getTask().isComplete) {
                        task.trySetException(exception)
                    } else {
                        LOG.i("CameraDevice.StateCallback reported disconnection.")
                        throw exception
                    }
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    if (!task.getTask().isComplete) {
                        task.trySetException(createCameraException(error))
                    } else {
                        // This happened while the engine is running. Throw unrecoverable exception
                        // so that engine is properly destroyed.
                        LOG.e("CameraDevice.StateCallback reported an error:", error)
                        throw CameraException(CameraException.REASON_DISCONNECTED)
                    }
                }
            }, null)
        } catch (e: CameraAccessException) {
            throw createCameraException(e)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return task.getTask()
    }

    @EngineThread
    override fun onStartBind(): Task<Void?> {
        LOG.i("onStartBind:", "Started")
        val task = TaskCompletionSource<Void?>()

        // Compute sizes.
        // TODO preview stream should never be bigger than 1920x1080 as per
        //  CameraDevice.createCaptureSession. This should probably be applied
        //  before all the other external selectors, to treat it as a hard limit.
        //  OR: pass an int into these functions to be able to take smaller dims
        //  when session configuration fails
        //  OR: both.
        mCaptureSize = computeCaptureSize()
        mPreviewStreamSize = computePreviewStreamSize()

        // Deal with surfaces.
        // In Camera2, instead of applying the size to the camera params object,
        // we must resize our own surfaces and configure them before opening the session.
        val outputSurfaces: MutableList<Surface?> = ArrayList()

        // 1. PREVIEW
        // Create a preview surface with the correct size.
        val outputClass = mPreview.getOutputClass()
        val output = mPreview.getOutput()
        when (outputClass) {
            SurfaceHolder::class.java -> {
                try {
                    // This must be called from the UI thread...
                    LOG.i("onStartBind:", "Waiting on UI thread...")
                    @Suppress("DEPRECATION", "UNCHECKED_CAST") Tasks.await(
                        Tasks.call<Void?>(
                            Callable {
                                (output as SurfaceHolder).setFixedSize(
                                    mPreviewStreamSize.width, mPreviewStreamSize.height
                                )
                                null
                            } as Callable<Void?>))
                } catch (e: ExecutionException) {
                    throw CameraException(e, CameraException.REASON_FAILED_TO_CONNECT)
                } catch (e: InterruptedException) {
                    throw CameraException(e, CameraException.REASON_FAILED_TO_CONNECT)
                } catch (e: Exception) {
                    throw CameraException(e, CameraException.REASON_UNKNOWN)
                }
                mPreviewStreamSurface = (output as SurfaceHolder).surface
            }

            SurfaceTexture::class.java -> {
                try {
                    (output as SurfaceTexture).setDefaultBufferSize(
                        mPreviewStreamSize.width, mPreviewStreamSize.height
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                mPreviewStreamSurface = Surface(output as SurfaceTexture)
            }

            else -> {
                throw RuntimeException("Unknown CameraPreview output class.")
            }
        }
        outputSurfaces.add(mPreviewStreamSurface)

        // 2. VIDEO RECORDING
        if (mode == Mode.VIDEO) {
            if (mFullVideoPendingStub != null) {
                val recorder = Full2VideoRecorder(this, mCameraId!!)
                try {
                    outputSurfaces.add(recorder.createInputSurface(mFullVideoPendingStub!!))
                } catch (e: PrepareException) {
                    throw CameraException(e, CameraException.REASON_FAILED_TO_CONNECT)
                } catch (e: Exception) {
                    throw CameraException(e, CameraException.REASON_UNKNOWN)
                }
                mVideoRecorder = recorder
            }
        }

        // 3. PICTURE RECORDING
        // Format is supported, or it would have thrown in Camera2Options constructor.
        if (mode == Mode.PICTURE) {
            val format = when (mPictureFormat) {
                PictureFormat.JPEG -> ImageFormat.JPEG
                PictureFormat.DNG -> ImageFormat.RAW_SENSOR
            }
            mPictureReader = ImageReader.newInstance(
                mCaptureSize.width, mCaptureSize.height, format, 2
            )
            outputSurfaces.add(mPictureReader!!.surface)
        }

        // 4. FRAME PROCESSING
        if (hasFrameProcessors()) {
            mFrameProcessingSize = computeFrameProcessingSize()
            // Hard to write down why, but in Camera2 we need a number of Frames that's one less
            // than the number of Images. If we let all Images be part of Frames, thus letting all
            // Images be used by processor at any given moment, the Camera2 output breaks.
            // In fact, if there are no Images available, the sensor BLOCKS until it finds one,
            // which is a big issue because processor times become a bottleneck for the preview.
            // This is a design flaw in the ImageReader / sensor implementation, as they should
            // simply DROP frames written to the surface if there are no Images available.
            // Since this is not how things work, we ensure that one Image is always available here.
            mFrameProcessingReader = ImageReader.newInstance(
                mFrameProcessingSize.width,
                mFrameProcessingSize.height,
                mFrameProcessingFormat,
                frameProcessingPoolSize + 1
            )
            mFrameProcessingReader!!.setOnImageAvailableListener(this, null)
            mFrameProcessingSurface = mFrameProcessingReader!!.surface
            outputSurfaces.add(mFrameProcessingSurface)
        } else {
            mFrameProcessingReader = null
            mFrameProcessingSize = null
            mFrameProcessingSurface = null
        }

        try {
            // null handler means using the current looper which is totally ok.
            @Suppress("DEPRECATION") mCamera!!.createCaptureSession(
                outputSurfaces, object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        mSession = session
                        LOG.i("onStartBind:", "Completed")
                        task.trySetResult(null)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        val message = LOG.e("onConfigureFailed! Session", session)
                        val cause: Throwable = RuntimeException(message)
                        if (!task.getTask().isComplete) {
                            task.trySetException(
                                CameraException(
                                    cause, CameraException.REASON_FAILED_TO_START_PREVIEW
                                )
                            )
                        } else {
                            // Like onStartEngine.onError
                            throw CameraException(CameraException.REASON_DISCONNECTED)
                        }
                    }

                    override fun onReady(session: CameraCaptureSession) {
                        super.onReady(session)
                        LOG.i("CameraCaptureSession.StateCallback reported onReady.")
                    }
                }, null
            )
        } catch (e: CameraAccessException) {
            throw createCameraException(e)
        } catch (e: Exception) {
            throw CameraException(e, CameraException.REASON_UNKNOWN)
        }
        return task.getTask()
    }

    @EngineThread
    override fun onStartPreview(): Task<Void?> {
        LOG.i("onStartPreview:", "Dispatching onCameraPreviewStreamSizeChanged.")
        callback.onCameraPreviewStreamSizeChanged()

        val previewSizeForView = getPreviewStreamSize(Reference.VIEW)
        checkNotNull(previewSizeForView) { "previewStreamSize should not be null at this point." }
        mPreview.setStreamSize(previewSizeForView.width, previewSizeForView.height)
        mPreview.setDrawRotation(angles.offset(Reference.BASE, Reference.VIEW, Axis.ABSOLUTE))
        if (hasFrameProcessors()) {
            getFrameManager().setUp(mFrameProcessingFormat, mFrameProcessingSize, angles)
        }

        LOG.i("onStartPreview:", "Starting preview.")
        addRepeatingRequestBuilderSurfaces()
        applyRepeatingRequestBuilder(false, CameraException.REASON_FAILED_TO_START_PREVIEW)
        LOG.i("onStartPreview:", "Started preview.")

        // Start delayed video if needed.
        if (mFullVideoPendingStub != null) {
            // Do not call takeVideo/onTakeVideo. It will reset some stub parameters that
            // the recorder sets. Also we are posting so that doTakeVideo sees a started preview.
            val stub = mFullVideoPendingStub
            mFullVideoPendingStub = null
            orchestrator.scheduleStateful("do take video", CameraState.PREVIEW) {
                doTakeVideo(stub!!)
            }
        }

        // Wait for the first frame.
        val task = TaskCompletionSource<Void?>()
        object : BaseAction() {
            override fun onCaptureCompleted(
                holder: ActionHolder, request: CaptureRequest, result: TotalCaptureResult
            ) {
                super.onCaptureCompleted(holder, request, result)
                state = Action.Companion.STATE_COMPLETED
                task.trySetResult(null)
            }
        }.start(this)
        return task.getTask()
    }

    //endregion
    //region Stop
    @EngineThread
    override fun onStopPreview(): Task<Void?> {
        LOG.i("onStopPreview:", "Started.")
        if (mVideoRecorder != null) {
            // This should synchronously call onVideoResult that will reset the repeating builder
            // to the PREVIEW template. This is very important.
            mVideoRecorder.stop(true)
            mVideoRecorder = null
        }
        mPictureRecorder = null
        if (hasFrameProcessors()) {
            getFrameManager().release()
        }
        // Removing the part below for now. It hangs on emulators and can take a lot of time
        // in real devices, for benefits that I'm not 100% sure about.
        removeRepeatingRequestBuilderSurfaces()
        mLastRepeatingResult = null
        LOG.i("onStopPreview:", "Returning.")
        return Tasks.forResult<Void?>(null)
    }

    @EngineThread
    override fun onStopBind(): Task<Void?> {
        LOG.i("onStopBind:", "About to clean up.")
        mFrameProcessingSurface = null
        mPreviewStreamSurface = null
        mPreviewStreamSize = null
        mCaptureSize = null
        mFrameProcessingSize = null
        if (mFrameProcessingReader != null) {
            // WARNING: This call synchronously releases all Images and their underlying
            // properties. This can cause issues if the Image is being used.
            mFrameProcessingReader!!.close()
            mFrameProcessingReader = null
        }
        if (mPictureReader != null) {
            mPictureReader!!.close()
            mPictureReader = null
        }
        mSession!!.close()
        mSession = null
        LOG.i("onStopBind:", "Returning.")
        return Tasks.forResult<Void?>(null)
    }

    @EngineThread
    override fun onStopEngine(): Task<Void?> {
        try {
            LOG.i("onStopEngine:", "Clean up.", "Releasing camera.")
            // Just like Camera1Engine, this call can hang (at least on emulators) and if
            // we don't find a way around the lock, it leaves the camera in a bad state.
            //
            // 12:33:28.152  2888  5470 I CameraEngine: onStopEngine: Clean up. Releasing camera.[0m
            // 12:33:29.476  1384  1555 E audio_hw_generic: pcm_write failed cannot write stream data: I/O error[0m
            // 12:33:33.206  1512  3616 E Camera3-Device: Camera 0: waitUntilDrainedLocked: Error waiting for HAL to drain: Connection timed out (-110)[0m
            // 12:33:33.242  1512  3616 E CameraDeviceClient: detachDevice: waitUntilDrained failed with code 0xffffff92[0m
            // 12:33:33.243  1512  3616 E Camera3-Device: Camera 0: disconnect: Shutting down in an error state[0m
            //
            // I believe there is a thread deadlock due to this call internally waiting to
            // dispatch some callback to us (pending captures, ...), but the callback thread
            // is blocked here. We try to workaround this in CameraEngine.destroy().
            mCamera!!.close()
            LOG.i("onStopEngine:", "Clean up.", "Released camera.")
        } catch (e: Exception) {
            LOG.w("onStopEngine:", "Clean up.", "Exception while releasing camera.", e)
        }
        mCamera = null

        // After engine is stopping, the repeating request builder will be null,
        // so the ActionHolder.getBuilder() contract would be broken. Same for characteristics.
        // This can cause crashes if some ongoing Action queries the holder. So we abort them.
        LOG.i("onStopEngine:", "Aborting actions.")
        for (action in mActions) {
            action.abort(this)
        }

        mCameraCharacteristics = null
        mCameraOptions = null
        mVideoRecorder = null
        mRepeatingRequestBuilder = null
        LOG.w("onStopEngine:", "Returning.")
        return Tasks.forResult<Void?>(null)
    }

    //endregion
    //region Pictures
    @EngineThread
    override fun onTakePictureSnapshot(
        stub: PictureResult.Stub, outputRatio: AspectRatio, doMetering: Boolean
    ) {
        if (doMetering) {
            LOG.i("onTakePictureSnapshot:", "doMetering is true. Delaying.")
            val action: Action = timeout(METER_TIMEOUT_SHORT, createMeterAction(null))
            action.addCallback(object : CompletionCallback() {
                override fun onActionCompleted(action: Action) {
                    // This is called on any thread, so be careful.
                    pictureSnapshotMetering = false
                    takePictureSnapshot(stub)
                    pictureSnapshotMetering = true
                }
            })
            action.start(this)
        } else {
            LOG.i("onTakePictureSnapshot:", "doMetering is false. Performing.")
            if (mPreview !is RendererCameraPreview) {
                throw RuntimeException("takePictureSnapshot with Camera2 is only " + "supported with Preview.GL_SURFACE")
            }
            // stub.size is not the real size: it will be cropped to the given ratio
            // stub.rotation will be set to 0 - we rotate the texture instead.
            stub.size = getUncroppedSnapshotSize(Reference.OUTPUT)
            stub.rotation = angles.offset(Reference.VIEW, Reference.OUTPUT, Axis.ABSOLUTE)
            mPictureRecorder = Snapshot2PictureRecorder(
                stub, this, (mPreview as RendererCameraPreview?)!!, outputRatio
            )
            mPictureRecorder.take()
        }
    }

    @EngineThread
    override fun onTakePicture(stub: PictureResult.Stub, doMetering: Boolean) {
        if (doMetering) {
            LOG.i("onTakePicture:", "doMetering is true. Delaying.")
            val action: Action = timeout(METER_TIMEOUT_SHORT, createMeterAction(null))
            action.addCallback(object : CompletionCallback() {
                override fun onActionCompleted(action: Action) {
                    // This is called on any thread, so be careful.
                    pictureMetering = false
                    takePicture(stub)
                    pictureMetering = true
                }
            })
            action.start(this)
        } else {
            LOG.i("onTakePicture:", "doMetering is false. Performing.")
            stub.rotation =
                angles.offset(Reference.SENSOR, Reference.OUTPUT, Axis.RELATIVE_TO_SENSOR)
            stub.size = getPictureSize(Reference.OUTPUT)
            try {
                if (mPictureCaptureStopsPreview) {
                    // These two are present in official samples and are probably meant to
                    // speed things up? But from my tests, they actually make everything slower.
                    // So this is disabled by default with a boolean flag. Maybe in the future
                    // we can make this configurable as some people might want to stop the preview
                    // while picture is being taken even if it increases the latency.
                    mSession!!.stopRepeating()
                    mSession!!.abortCaptures()
                }
                val builder = mCamera!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                applyAllParameters(builder, mRepeatingRequestBuilder)
                mPictureRecorder = Full2PictureRecorder(stub, this, builder, mPictureReader!!)
                mPictureRecorder.take()
            } catch (e: CameraAccessException) {
                throw createCameraException(e)
            } catch (e: Exception) {
                throw CameraException(e, CameraException.REASON_UNKNOWN)
            }
        }
    }

    override fun onPictureResult(result: PictureResult.Stub?, error: Exception?) {
        val fullPicture = mPictureRecorder is Full2PictureRecorder
        super.onPictureResult(result, error)
        if (fullPicture && mPictureCaptureStopsPreview) {
            applyRepeatingRequestBuilder()
        }

        // Some picture recorders might lock metering, and we usually run a metering sequence
        // before running the recorders. So, run an unlock/reset sequence if needed.
        val unlock = (fullPicture && pictureMetering) || (!fullPicture && pictureSnapshotMetering)
        if (unlock) {
            orchestrator.scheduleStateful(
                "reset metering after picture", CameraState.PREVIEW
            ) {
                unlockAndResetMetering()
            }
        }
    }

    //endregion
    //region Videos
    @EngineThread
    override fun onTakeVideo(stub: VideoResult.Stub) {
        LOG.i("onTakeVideo", "called.")
        stub.rotation =
            angles.offset(Reference.SENSOR, Reference.OUTPUT, Axis.RELATIVE_TO_SENSOR)
        stub.size = if (angles.flip(
                Reference.SENSOR, Reference.OUTPUT
            )
        ) mCaptureSize.flip() else mCaptureSize
        // We must restart the session at each time.
        // Save the pending data and restart the session.
        LOG.w("onTakeVideo", "calling restartBind.")
        mFullVideoPendingStub = stub
        restartBind()
    }

    private fun doTakeVideo(stub: VideoResult.Stub) {
        check(mVideoRecorder is Full2VideoRecorder) {
            "doTakeVideo called, but video recorder is not a Full2VideoRecorder! $mVideoRecorder"
        }

        try {
            createRepeatingRequestBuilder(CameraDevice.TEMPLATE_RECORD)
            addRepeatingRequestBuilderSurfaces((mVideoRecorder as Full2VideoRecorder).inputSurface!!)
            applyRepeatingRequestBuilder(true, CameraException.REASON_DISCONNECTED)
            mVideoRecorder.start(stub)
        } catch (e: CameraAccessException) {
            onVideoResult(null, e)
            throw createCameraException(e)
        } catch (e: CameraException) {
            onVideoResult(null, e)
            throw e
        } catch (e: Exception) {
            onVideoResult(null, e)
            throw CameraException(e, CameraException.REASON_UNKNOWN)
        }
    }

    @EngineThread
    override fun onTakeVideoSnapshot(stub: VideoResult.Stub, outputRatio: AspectRatio) {
        check(mPreview is RendererCameraPreview) { "Video snapshots are only supported with GL_SURFACE." }
        var outputSize = getUncroppedSnapshotSize(Reference.OUTPUT)
        checkNotNull(outputSize) { "outputSize should not be null." }
        val outputCrop = CropHelper.computeCrop(outputSize, outputRatio)
        outputSize = Size(outputCrop.width(), outputCrop.height())
        stub.size = outputSize
        stub.rotation = angles.offset(Reference.VIEW, Reference.OUTPUT, Axis.ABSOLUTE)
        stub.videoFrameRate = mPreviewFrameRate.roundToInt()
        LOG.i("onTakeVideoSnapshot", "rotation:", stub.rotation, "size:", stub.size)
        mVideoRecorder = SnapshotVideoRecorder(this, mPreview as RendererCameraPreview, overlay)
        try {
            mVideoRecorder.start(stub)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * When video ends we must stop the recorder and remove the recorder surface from
     * camera outputs. This is done in onVideoResult. However, on some devices, order matters.
     * If we stop the recorder and AFTER send camera frames to it, the camera will try to fill
     * the recorder "abandoned" Surface and on some devices with a poor internal implementation
     * (HW_LEVEL_LEGACY) this crashes. So if the conditions are met, we restore here. Issue #549.
     */
    override fun onVideoRecordingEnd() {
        super.onVideoRecordingEnd()
        // SnapshotRecorder will invoke this on its own thread which is risky, but if it was a
        // snapshot, this function does nothing so it's safe.
        val needsIssue549Workaround =
            (mVideoRecorder is Full2VideoRecorder) && (readCharacteristic<Int?>(
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL, -1
            ) == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY)
        if (needsIssue549Workaround) {
            LOG.w("Applying the Issue549 workaround.", Thread.currentThread())
            maybeRestorePreviewTemplateAfterVideo()
            LOG.w("Applied the Issue549 workaround. Sleeping...")
            try {
                Thread.sleep(600)
            } catch (ignore: InterruptedException) {
                ignore.printStackTrace()
            }
            LOG.w("Applied the Issue549 workaround. Slept!")
        }
    }

    override fun onVideoResult(result: VideoResult.Stub?, exception: Exception?) {
        super.onVideoResult(result, exception)
        // SnapshotRecorder will invoke this on its own thread, so let's post in our own thread
        // and check camera state before trying to restore the preview. Engine might have been
        // torn down in the engine thread while this was still being called.
        orchestrator.scheduleStateful(
            "restore preview template", CameraState.BIND
        ) {
            maybeRestorePreviewTemplateAfterVideo()
        }
    }

    /**
     * Video recorders might change the camera template to [CameraDevice.TEMPLATE_RECORD].
     * After the video is taken, we should restore the template preview, which also means that
     * we'll remove any extra surface target that was added by the video recorder.
     *
     *
     * This method avoids doing this twice by checking the request tag, as set by
     * the [.createRepeatingRequestBuilder] method.
     */
    @EngineThread
    private fun maybeRestorePreviewTemplateAfterVideo() {
        val captureRequest = mRepeatingRequestBuilder!!.build()
        if (captureRequest.tag != null) {
            val template = captureRequest.tag as Int
            if (template != this.repeatingRequestDefaultTemplate) {
                try {
                    createRepeatingRequestBuilder(this.repeatingRequestDefaultTemplate)
                    addRepeatingRequestBuilderSurfaces()
                    applyRepeatingRequestBuilder()
                } catch (e: CameraAccessException) {
                    throw createCameraException(e)
                } catch (e: Exception) {
                    throw CameraException(e, CameraException.REASON_UNKNOWN)
                }
            }
        }
    }

    //endregion
    //region Parameters
    private fun applyAllParameters(
        builder: CaptureRequest.Builder, oldBuilder: CaptureRequest.Builder?
    ) {
        LOG.i("applyAllParameters:", "called for tag", builder.build().tag)
        builder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
        applyDefaultFocus(builder)
        applyFlash(builder, Flash.OFF)
        applyLocation(builder, null)
        applyWhiteBalance(builder, WhiteBalance.AUTO)
        applyHdr(builder, Hdr.OFF)
        applyZoom(builder, 0f)
        applyExposureCorrection(builder, 0f)
        applyPreviewFrameRate(builder, 0f)

        if (oldBuilder != null) {
            // We might be in a metering operation, or the old builder might have some special
            // metering parameters. Copy these special keys over to the new builder.
            // These are the keys changed by metering.Parameters, or by us in applyFocusForMetering.
            builder.set(
                CaptureRequest.CONTROL_AF_REGIONS,
                oldBuilder.get(CaptureRequest.CONTROL_AF_REGIONS)
            )
            builder.set(
                CaptureRequest.CONTROL_AE_REGIONS,
                oldBuilder.get(CaptureRequest.CONTROL_AE_REGIONS)
            )
            builder.set(
                CaptureRequest.CONTROL_AWB_REGIONS,
                oldBuilder.get(CaptureRequest.CONTROL_AWB_REGIONS)
            )
            builder.set(
                CaptureRequest.CONTROL_AF_MODE, oldBuilder.get(CaptureRequest.CONTROL_AF_MODE)
            )
            // Do NOT copy exposure or focus triggers!
        }
    }

    private fun applyDefaultFocus(builder: CaptureRequest.Builder) {
        val modesArray = readCharacteristic<IntArray?>(
            CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES, intArrayOf()
        )!!
        val modes: MutableList<Int?> = ArrayList()
        for (mode in modesArray) {
            modes.add(mode)
        }
        if (mode == Mode.VIDEO && modes.contains(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO)) {
            builder.set(
                CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO
            )
            return
        }

        if (modes.contains(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)) {
            builder.set(
                CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            return
        }

        if (modes.contains(CaptureRequest.CONTROL_AF_MODE_AUTO)) {
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
            return
        }

        if (modes.contains(CaptureRequest.CONTROL_AF_MODE_OFF)) {
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_OFF)
            builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, 0f)
            return
        }
    }

    /**
     * All focus modes support the AF trigger, except OFF and ED-OF.
     * However, unlike the preview, we'd prefer AUTO to any CONTINUOUS value.
     * An AUTO value means that focus is locked unless we run the focus trigger,
     * which is what metering does.
     *
     * @param builder builder
     */
    private fun applyFocusForMetering(builder: CaptureRequest.Builder) {
        val modesArray = readCharacteristic<IntArray?>(
            CameraCharacteristics.CONTROL_AF_AVAILABLE_MODES, intArrayOf()
        )!!
        val modes: MutableList<Int?> = ArrayList()
        for (mode in modesArray) {
            modes.add(mode)
        }
        if (modes.contains(CaptureRequest.CONTROL_AF_MODE_AUTO)) {
            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_AUTO)
            return
        }
        if (mode == Mode.VIDEO && modes.contains(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO)) {
            builder.set(
                CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO
            )
            return
        }

        if (modes.contains(CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)) {
            builder.set(
                CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE
            )
            return
        }
    }

    override fun setFlash(flash: Flash) {
        val old = mFlash
        mFlash = flash
        mFlashTask = orchestrator.scheduleStateful(
            "flash ($flash)", CameraState.ENGINE
        ) {
            val shouldApply = applyFlash(mRepeatingRequestBuilder!!, old)
            val needsWorkaround = state == CameraState.PREVIEW
            if (needsWorkaround) {
                // Runtime changes to the flash value are not correctly handled by the
                // driver. See https://stackoverflow.com/q/53003383/4288782 for example.
                // For this reason, we go back to OFF, capture once, then go to the new one.
                mFlash = Flash.OFF
                applyFlash(mRepeatingRequestBuilder!!, old)
                try {
                    mSession!!.capture(mRepeatingRequestBuilder!!.build(), null, null)
                } catch (e: CameraAccessException) {
                    throw createCameraException(e)
                } catch (e: Exception) {
                    throw CameraException(e, CameraException.REASON_UNKNOWN)
                }
                mFlash = flash
                applyFlash(mRepeatingRequestBuilder!!, old)
                applyRepeatingRequestBuilder()
            } else if (shouldApply) {
                applyRepeatingRequestBuilder()
            }
        }
    }

    /**
     * This sets the CONTROL_AE_MODE to either:
     * - [CaptureRequest.CONTROL_AE_MODE_ON]
     * - [CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH]
     * - [CaptureRequest.CONTROL_AE_MODE_ON_ALWAYS_FLASH]
     *
     *
     * The API offers a high level control through [CaptureRequest.CONTROL_AE_MODE],
     * which is what the mapper looks at. It will trigger (if specified) flash only for
     * still captures which is exactly what we want.
     *
     *
     * However, we set CONTROL_AE_MODE to ON/OFF (depending
     * on which is available) with both [Flash.OFF] and [Flash.TORCH].
     *
     *
     * When CONTROL_AE_MODE is ON or OFF, the low level control, called
     * [CaptureRequest.FLASH_MODE], becomes effective, and that's where we can actually
     * distinguish between a turned off flash and a torch flash.
     */
    private fun applyFlash(builder: CaptureRequest.Builder, oldFlash: Flash): Boolean {
        if (mCameraOptions.supports(mFlash)) {
            val availableAeModesArray = readCharacteristic<IntArray?>(
                CameraCharacteristics.CONTROL_AE_AVAILABLE_MODES, intArrayOf()
            )!!
            val availableAeModes: MutableList<Int?> = ArrayList()
            for (mode in availableAeModesArray) {
                availableAeModes.add(mode)
            }
            val pairs: MutableList<Pair<Int?, Int?>> = mMapper.mapFlash(mFlash)
            for (pair in pairs) {
                if (availableAeModes.contains(pair.first)) {
                    LOG.i("applyFlash: setting CONTROL_AE_MODE to", pair.first)
                    LOG.i("applyFlash: setting FLASH_MODE to", pair.second)
                    builder.set(CaptureRequest.CONTROL_AE_MODE, pair.first)
                    builder.set(CaptureRequest.FLASH_MODE, pair.second)
                    return true
                }
            }
        }
        mFlash = oldFlash
        return false
    }

    override fun setLocation(location: Location?) {
        val old = mLocation
        mLocation = location
        mLocationTask =
            orchestrator.scheduleStateful("location", CameraState.ENGINE) {
                if (applyLocation(
                        mRepeatingRequestBuilder!!, old
                    )
                ) {
                    applyRepeatingRequestBuilder()
                }
            }
    }

    private fun applyLocation(
        builder: CaptureRequest.Builder, @Suppress("unused") oldLocation: Location?
    ): Boolean {
        if (mLocation != null) {
            builder.set(CaptureRequest.JPEG_GPS_LOCATION, mLocation)
        }
        return true
    }

    override fun setWhiteBalance(whiteBalance: WhiteBalance) {
        val old = mWhiteBalance
        mWhiteBalance = whiteBalance
        mWhiteBalanceTask = orchestrator.scheduleStateful(
            "white balance ($whiteBalance)", CameraState.ENGINE
        ) {
            if (applyWhiteBalance(
                    mRepeatingRequestBuilder!!, old
                )
            ) {
                applyRepeatingRequestBuilder()
            }
        }
    }

    private fun applyWhiteBalance(
        builder: CaptureRequest.Builder, oldWhiteBalance: WhiteBalance
    ): Boolean {
        if (mCameraOptions.supports(mWhiteBalance)) {
            val whiteBalance = mMapper.mapWhiteBalance(mWhiteBalance)
            builder.set(CaptureRequest.CONTROL_AWB_MODE, whiteBalance)
            return true
        }
        mWhiteBalance = oldWhiteBalance
        return false
    }

    override fun setHdr(hdr: Hdr) {
        val old = mHdr
        mHdr = hdr
        mHdrTask =
            orchestrator.scheduleStateful("hdr ($hdr)", CameraState.ENGINE) {
                if (applyHdr(
                        mRepeatingRequestBuilder!!, old
                    )
                ) {
                    applyRepeatingRequestBuilder()
                }
            }
    }

    private fun applyHdr(builder: CaptureRequest.Builder, oldHdr: Hdr): Boolean {
        if (mCameraOptions.supports(mHdr)) {
            val hdr = mMapper.mapHdr(mHdr)
            builder.set(CaptureRequest.CONTROL_SCENE_MODE, hdr)
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
        mZoomTask = orchestrator.scheduleStateful("zoom", CameraState.ENGINE) {
            if (applyZoom(
                    mRepeatingRequestBuilder!!, old
                )
            ) {
                applyRepeatingRequestBuilder()
                if (notify) {
                    callback.dispatchOnZoomChanged(zoom, points)
                }
            }
        }
    }

    private fun applyZoom(builder: CaptureRequest.Builder, oldZoom: Float): Boolean {
        if (mCameraOptions.isZoomSupported) {
            val maxZoom = readCharacteristic<Float?>(
                CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM, 1f
            )!!
            // converting 0.0f-1.0f zoom scale to the actual camera digital zoom scale
            // (which will be for example, 1.0-10.0)
            val calculatedZoom = (mZoomValue * (maxZoom - 1.0f)) + 1.0f
            val newRect = getZoomRect(calculatedZoom, maxZoom)
            builder.set(CaptureRequest.SCALER_CROP_REGION, newRect)
            return true
        }
        mZoomValue = oldZoom
        return false
    }

    private fun getZoomRect(zoomLevel: Float, maxDigitalZoom: Float): Rect {
        val activeRect =
            readCharacteristic(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE, Rect())
        val minW = (activeRect.width() / maxDigitalZoom).toInt()
        val minH = (activeRect.height() / maxDigitalZoom).toInt()
        val difW = activeRect.width() - minW
        val difH = activeRect.height() - minH

        // When zoom is 1, we want to return new Rect(0, 0, width, height).
        // When zoom is maxZoom, we want to return a centered rect with minW and minH
        val cropW = (difW * (zoomLevel - 1) / (maxDigitalZoom - 1) / 2f).toInt()
        val cropH = (difH * (zoomLevel - 1) / (maxDigitalZoom - 1) / 2f).toInt()
        return Rect(cropW, cropH, activeRect.width() - cropW, activeRect.height() - cropH)
    }

    override fun setExposureCorrection(
        evValue: Float, bounds: FloatArray, points: Array<PointF?>?, notify: Boolean
    ) {
        val old = mExposureCorrectionValue
        mExposureCorrectionValue = evValue
        // EV requests can be high frequency (e.g. linked to touch events), let's trim the oldest.
        orchestrator.trim("exposure correction", ALLOWED_EV_OPS)
        mExposureCorrectionTask =
            orchestrator.scheduleStateful("exposure correction", CameraState.ENGINE) {
                if (applyExposureCorrection(
                        mRepeatingRequestBuilder!!, old
                    )
                ) {
                    applyRepeatingRequestBuilder()
                    if (notify) {
                        callback.dispatchOnExposureCorrectionChanged(evValue, bounds, points)
                    }
                }
            }
    }

    private fun applyExposureCorrection(
        builder: CaptureRequest.Builder, oldEValue: Float
    ): Boolean {
        if (mCameraOptions.isExposureCorrectionSupported) {
            val exposureCorrectionStep = readCharacteristic(
                CameraCharacteristics.CONTROL_AE_COMPENSATION_STEP, Rational(1, 1)
            )
            val exposureCorrectionSteps =
                (mExposureCorrectionValue * exposureCorrectionStep.toFloat()).roundToInt()
            builder.set(
                CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, exposureCorrectionSteps
            )
            return true
        }
        mExposureCorrectionValue = oldEValue
        return false
    }

    override fun setPlaySounds(playSounds: Boolean) {
        mPlaySounds = playSounds
        mPlaySoundsTask = Tasks.forResult(null)
    }

    override fun setPreviewFrameRate(previewFrameRate: Float) {
        val oldPreviewFrameRate = mPreviewFrameRate
        mPreviewFrameRate = previewFrameRate
        mPreviewFrameRateTask = orchestrator.scheduleStateful(
            "preview fps ($previewFrameRate)", CameraState.ENGINE
        ) {
            if (applyPreviewFrameRate(
                    mRepeatingRequestBuilder!!, oldPreviewFrameRate
                )
            ) {
                applyRepeatingRequestBuilder()
            }
        }
    }

    private fun applyPreviewFrameRate(
        builder: CaptureRequest.Builder, oldPreviewFrameRate: Float
    ): Boolean {
        val fpsRanges = readCharacteristic(
            CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES, arrayOf()
        )
        sortFrameRateRanges(fpsRanges)
        if (mPreviewFrameRate == 0f) {
            // 0F is a special value. Fallback to a reasonable default.
            for (fpsRange in filterFrameRateRanges(fpsRanges)) {
                if (fpsRange.contains(30) || fpsRange.contains(24)) {
                    builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRange)
                    return true
                }
            }
        } else {
            // If out of boundaries, adjust it.
            mPreviewFrameRate = min(mPreviewFrameRate, mCameraOptions.getPreviewFrameRateMaxValue())
            mPreviewFrameRate = max(mPreviewFrameRate, mCameraOptions.getPreviewFrameRateMinValue())
            for (fpsRange in filterFrameRateRanges(fpsRanges)) {
                if (fpsRange.contains(mPreviewFrameRate.roundToInt())) {
                    builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRange)
                    return true
                }
            }
        }
        mPreviewFrameRate = oldPreviewFrameRate
        return false
    }

    private fun sortFrameRateRanges(fpsRanges: Array<Range<Int>?>) {
        val ascending = previewFrameRateExact && mPreviewFrameRate != 0f
        Arrays.sort<Range<Int>?>(
            fpsRanges, Comparator { range1: Range<Int>?, range2: Range<Int>? ->
                if (ascending) {
                    return@Comparator (range1!!.getUpper()!! - range1.getLower()!!) - (range2!!.getUpper()!! - range2.getLower()!!)
                } else {
                    return@Comparator (range2!!.getUpper()!! - range2.getLower()!!) - (range1!!.getUpper()!! - range1.getLower()!!)
                }
            })
    }

    private fun filterFrameRateRanges(fpsRanges: Array<Range<Int>>): MutableList<Range<Int>> {
        val results: MutableList<Range<Int>> = ArrayList()
        val min = mCameraOptions.getPreviewFrameRateMinValue().roundToInt()
        val max = mCameraOptions.getPreviewFrameRateMaxValue().roundToInt()
        for (fpsRange in fpsRanges) {
            if (!fpsRange.contains(min) && !fpsRange.contains(max)) continue
            if (!FpsRangeValidator.validate(fpsRange)) continue
            results.add(fpsRange)
        }
        return results
    }

    override fun setPictureFormat(pictureFormat: PictureFormat) {
        if (pictureFormat != mPictureFormat) {
            mPictureFormat = pictureFormat
            orchestrator.scheduleStateful(
                "picture format ($pictureFormat)", CameraState.ENGINE
            ) {
                restart()
            }
        }
    }

    //endregion
    //region Frame Processing
    override fun instantiateFrameManager(poolSize: Int): FrameManager<*> {
        return ImageFrameManager(poolSize)
    }

    @EngineThread
    override fun onImageAvailable(reader: ImageReader) {
        LOG.v("onImageAvailable:", "trying to acquire Image.")
        var image: Image? = null
        try {
            image = reader.acquireLatestImage()
        } catch (ignore: Exception) {
            ignore.printStackTrace()
        }
        if (image == null) {
            LOG.w("onImageAvailable:", "failed to acquire Image!")
        } else if (state == CameraState.PREVIEW && !isChangingState) {
            // After preview, the frame manager is correctly set up
            val frame = getFrameManager().getFrame(image, System.currentTimeMillis())
            if (frame != null) {
                LOG.v("onImageAvailable:", "Image acquired, dispatching.")
                callback.dispatchFrame(frame)
            } else {
                LOG.i("onImageAvailable:", "Image acquired, but no free frames. DROPPING.")
            }
        } else {
            LOG.i("onImageAvailable:", "Image acquired in wrong state. Closing it now.")
            image.close()
        }
    }

    override fun setHasFrameProcessors(hasFrameProcessors: Boolean) {
        // Frame processing is set up partially when binding and partially when starting
        // the preview. If the value is changed between the two, the preview step can crash.
        orchestrator.schedule(
            "has frame processors ($hasFrameProcessors)", true
        ) {
            if (state.isAtLeast(CameraState.BIND) && isChangingState) {
                // Extremely rare case in which this was called in between startBind and
                // startPreview. This can cause issues. Try later.
                setHasFrameProcessors(hasFrameProcessors)
                return@schedule
            }
            // Apply and restart.
            mHasFrameProcessors = hasFrameProcessors
            if (state.isAtLeast(CameraState.BIND)) {
                restartBind()
            }
        }
    }

    override fun setFrameProcessingFormat(format: Int) {
        // This is called during initialization. Set our default first.
        if (mFrameProcessingFormat == 0) mFrameProcessingFormat = FRAME_PROCESSING_FORMAT
        // Frame processing format is used both when binding and when starting the preview.
        // If the value is changed between the two, the preview step can crash.
        orchestrator.schedule("frame processing format ($format)", true) {
            if (state.isAtLeast(CameraState.BIND) && isChangingState) {
                // Extremely rare case in which this was called in between startBind and
                // startPreview. This can cause issues. Try later.
                setFrameProcessingFormat(format)
                return@schedule
            }
            mFrameProcessingFormat = if (format > 0) format else FRAME_PROCESSING_FORMAT
            if (state.isAtLeast(CameraState.BIND)) {
                restartBind()
            }
        }
    }

    //endregion
    //region 3A Metering
    override fun startAutoFocus(gesture: Gesture?, regions: MeteringRegions, legacyPoint: PointF) {
        // This will only work when we have a preview, since it launches the preview
        // in the end. Even without this it would need the bind state at least,
        // since we need the preview size.
        orchestrator.scheduleStateful(
            "autofocus ($gesture)", CameraState.PREVIEW
        ) {
            // The camera options API still has the auto focus API but it really
            // refers to "3A metering to a specific point". Since we have a point, check.
            if (!mCameraOptions.isAutoFocusSupported) return@scheduleStateful

            // Create the meter and start.
            callback.dispatchOnFocusStart(gesture, legacyPoint)
            val action = createMeterAction(regions)
            val wrapper: Action = timeout(METER_TIMEOUT, action)
            wrapper.start(this@Camera2Engine)
            wrapper.addCallback(object : CompletionCallback() {
                override fun onActionCompleted(a: Action) {
                    callback.dispatchOnFocusEnd(gesture, action.isSuccessful, legacyPoint)
                    orchestrator.remove("reset metering")
                    if (shouldResetAutoFocus()) {
                        orchestrator.scheduleStatefulDelayed(
                            "reset metering", CameraState.PREVIEW, autoFocusResetDelay
                        ) {
                            unlockAndResetMetering()
                        }
                    }
                }
            })
        }
    }

    private fun createMeterAction(regions: MeteringRegions?): MeterAction {
        // Before creating any new meter action, abort the old one.
        if (mMeterAction != null) mMeterAction!!.abort(this)
        // The meter will check the current configuration to see if AF/AE/AWB should run.
        // - AE should be on CONTROL_AE_MODE_ON*    (this depends on setFlash())
        // - AWB should be on CONTROL_AWB_MODE_AUTO (this depends on setWhiteBalance())
        // - AF should be on CONTROL_AF_MODE_AUTO or others
        // The last one is under our control because the library has no focus API.
        // So let's set a good af mode here. This operation is reverted during onMeteringReset().
        applyFocusForMetering(mRepeatingRequestBuilder!!)
        mMeterAction = MeterAction(this@Camera2Engine, regions, regions == null)
        return mMeterAction!!
    }

    @EngineThread
    private fun unlockAndResetMetering() {
        // Needs the PREVIEW state!
        sequence(object : BaseAction() {
            override fun onStart(holder: ActionHolder) {
                super.onStart(holder)
                applyDefaultFocus(holder.getBuilder(this))
                holder.getBuilder(this).set(CaptureRequest.CONTROL_AE_LOCK, false)
                holder.getBuilder(this).set(CaptureRequest.CONTROL_AWB_LOCK, false)
                holder.applyBuilder(this)
                state = Action.Companion.STATE_COMPLETED
                // TODO should wait results?
            }
        }, MeterResetAction()).start(this@Camera2Engine)
    }

    //endregion
    //region Actions
    override fun addAction(action: Action) {
        if (!mActions.contains(action)) {
            mActions.add(action)
        }
    }

    override fun removeAction(action: Action) {
        mActions.remove(action)
    }

    override fun getCharacteristics(action: Action): CameraCharacteristics {
        return mCameraCharacteristics!!
    }

    override fun getLastResult(action: Action): TotalCaptureResult? {
        return mLastRepeatingResult
    }

    override fun getBuilder(action: Action): CaptureRequest.Builder {
        return mRepeatingRequestBuilder!!
    }

    @EngineThread
    override fun applyBuilder(source: Action) {
        // NOTE: Should never be called on a non-engine thread!
        // Non-engine threads are not protected by the uncaught exception handler
        // and can make the process crash.
        applyRepeatingRequestBuilder()
    }

    @Throws(CameraAccessException::class)
    override fun applyBuilder(source: Action, builder: CaptureRequest.Builder) {
        // Risky - would be better to ensure that thread is the engine one.
        if (state == CameraState.PREVIEW && !isChangingState) {
            mSession!!.capture(builder.build(), mRepeatingRequestCallback, null)
        }
    } //endregion

    companion object {
        @VisibleForTesting
        const val METER_TIMEOUT: Long = 5000
        private const val FRAME_PROCESSING_FORMAT = ImageFormat.YUV_420_888
        private const val METER_TIMEOUT_SHORT: Long = 2500
    }
}