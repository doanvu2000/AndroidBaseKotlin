package com.base.cameraview.engine;

import android.content.Context;
import android.graphics.PointF;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.base.cameraview.CameraException;
import com.base.cameraview.CameraLogger;
import com.base.cameraview.CameraOptions;
import com.base.cameraview.PictureResult;
import com.base.cameraview.VideoResult;
import com.base.cameraview.controls.Audio;
import com.base.cameraview.controls.AudioCodec;
import com.base.cameraview.controls.Facing;
import com.base.cameraview.controls.Flash;
import com.base.cameraview.controls.Hdr;
import com.base.cameraview.controls.Mode;
import com.base.cameraview.controls.PictureFormat;
import com.base.cameraview.controls.VideoCodec;
import com.base.cameraview.controls.WhiteBalance;
import com.base.cameraview.engine.offset.Angles;
import com.base.cameraview.engine.offset.Reference;
import com.base.cameraview.engine.orchestrator.CameraOrchestrator;
import com.base.cameraview.engine.orchestrator.CameraState;
import com.base.cameraview.engine.orchestrator.CameraStateOrchestrator;
import com.base.cameraview.frame.Frame;
import com.base.cameraview.frame.FrameManager;
import com.base.cameraview.gesture.Gesture;
import com.base.cameraview.internal.WorkerHandler;
import com.base.cameraview.metering.MeteringRegions;
import com.base.cameraview.overlay.Overlay;
import com.base.cameraview.picture.PictureRecorder;
import com.base.cameraview.preview.CameraPreview;
import com.base.cameraview.size.Size;
import com.base.cameraview.size.SizeSelector;
import com.base.cameraview.video.VideoRecorder;
import com.google.android.gms.tasks.SuccessContinuation;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.File;
import java.io.FileDescriptor;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * PROCESS
 * Setting up the Camera is usually a 4 steps process:
 * 2. Starting the camera. Done by us. See {@link #startEngine()}, {@link #onStartEngine()}.
 * 3. Binding the camera to the surface. Done by us. See {@link #startBind()},
 * {@link #onStartBind()}
 * 4. Streaming the camera preview. Done by us. See {@link #startPreview()},
 * {@link #onStartPreview()}
 * <p>
 * The first two steps can actually happen at the same time, anyway
 * the order is not guaranteed, we just get a callback from the Preview when 1 happens.
 * So at the end of both step 1 and 2, the engine should check if both have
 * been performed and trigger the steps 3 and 4.
 * <p>
 * STATE
 * We only expose generic {@link #start()} and {@link #stop(boolean)} calls to the outside.
 * The external users of this class are most likely interested in whether we have completed step 2
 * or not, since that tells us if we can act on the camera or not, rather than knowing about
 * steps 3 and 4.
 * <p>
 * So in the {@link CameraEngine} notation,
 * - {@link #start()}: ASYNC - starts the engine (S2). When possible, at a later time,
 * S3 and S4 are also performed.
 * - {@link #stop(boolean)}: ASYNC - stops everything: undoes S4, then S3, then S2.
 * - {@link #restart()}: ASYNC - completes a stop then a start.
 * - {@link #destroy(boolean)}: SYNC - performs a {@link #stop(boolean)} that will go on no matter
 * what, without throwing. Makes the engine unusable and clears
 * resources.
 * <p>
 * THREADING
 * Subclasses should always execute code on the thread given by {@link #mHandler}.
 * For convenience, all the setup and tear down methods are called on this engine thread:
 * {@link #onStartEngine()}, {@link #onStartBind()}, {@link #onStartPreview()} to setup and
 * {@link #onStopEngine()}, {@link #onStopBind()}, {@link #onStopPreview()} to tear down.
 * However, these methods are not forced to be synchronous and then can simply return a Google's
 * {@link Task}.
 * <p>
 * Other setters are executed on the callers thread so subclasses should make sure they post
 * to the engine handler before acting on themselves.
 * <p>
 * <p>
 * ERROR HANDLING
 * THe {@link #mHandler} thread has a special {@link Thread.UncaughtExceptionHandler} that handles
 * exceptions and dispatches error to the callback (instead of crashing the app).
 * This lets subclasses run code safely and directly throw {@link CameraException}s when needed.
 * <p>
 * For convenience, the two main method {@link #onStartEngine()} and {@link #onStopEngine()}
 * are already called on the engine thread, but they can still be asynchronous by returning a
 * Google's {@link Task}.
 */
public abstract class CameraEngine implements CameraPreview.SurfaceCallback, PictureRecorder.PictureResultListener, VideoRecorder.VideoResultListener {

    protected static final String TAG = CameraEngine.class.getSimpleName();
    protected static final CameraLogger LOG = CameraLogger.create(TAG);
    // If this is 2, this means we'll try to run destroy() twice.
    private static final int DESTROY_RETRIES = 2;
    private final Callback mCallback;
    @VisibleForTesting
    Handler mCrashHandler;
    private WorkerHandler mHandler;

    protected CameraEngine(@NonNull Callback callback) {
        mCallback = callback;
        mCrashHandler = new Handler(Looper.getMainLooper());
        recreateHandler(false);
    }

    @NonNull
    protected final Callback getCallback() {
        return mCallback;
    }

    /**
     * Handles exceptions coming from either runtime errors on the {@link #mHandler} code that is
     * not caught (using the {@link CrashExceptionHandler}), as might happen during standard
     * mHandler.post() operations that subclasses might do, OR for errors caught by tasks and
     * continuations that we launch here.
     * <p>
     * In the first case, the thread is about to be terminated. In the second case,
     * we can actually keep using it.
     *
     * @param throwable  the throwable
     * @param isUncaught true if coming from exception handler
     */
    private void handleException(@NonNull final Throwable throwable, final boolean isUncaught) {
        // 1. If this comes from the exception handler, the thread has crashed. Replace it.
        // Most actions are wrapped into Tasks so don't go here, but some callbacks do
        // (at least in Camera1, e.g. onError).
        if (isUncaught) {
            LOG.e("EXCEPTION:", "Handler thread is gone. Replacing.");
            recreateHandler(false);
        }

        // 2. Depending on the exception, we must destroy(false|true) to release resources, and
        // notify the outside, either with the callback or by crashing the app.
        LOG.e("EXCEPTION:", "Scheduling on the crash handler...");
        mCrashHandler.post(() -> {
            if (throwable instanceof CameraException) {
                CameraException exception = (CameraException) throwable;
                if (exception.isUnrecoverable()) {
                    LOG.e("EXCEPTION:", "Got CameraException. " + "Since it is unrecoverable, executing destroy(false).");
                    destroy(false);
                }
                LOG.e("EXCEPTION:", "Got CameraException. Dispatching to callback.");
                mCallback.dispatchError(exception);
            } else {
                LOG.e("EXCEPTION:", "Unexpected error! Executing destroy(true).");
                destroy(true);
                LOG.e("EXCEPTION:", "Unexpected error! Throwing.");
                //todo: don't throw exceptions in production
//                if (throwable instanceof RuntimeException) {
//                    throw (RuntimeException) throwable;
//                } else {
//                    throw new RuntimeException(throwable);
//                }
            }
        });
    }

    @NonNull
    protected final CameraStateOrchestrator getOrchestrator() {
        return mOrchestrator;
    }    private final CameraStateOrchestrator mOrchestrator = new CameraStateOrchestrator(new CameraOrchestrator.Callback() {
        @Override
        @NonNull
        public WorkerHandler getJobWorker(@NonNull String job) {
            return mHandler;
        }

        @Override
        public void handleJobException(@NonNull String job, @NonNull Exception exception) {
            handleException(exception, false);
        }
    });

    private void destroy(boolean unrecoverably, int depth) {
        LOG.i("DESTROY:", "state:", getState(), "thread:", Thread.currentThread(), "depth:", depth, "unrecoverably:", unrecoverably);
        if (unrecoverably) {
            // Prevent CameraEngine leaks. Don't set to null, or exceptions
            // inside the standard stop() method might crash the main thread.
            try {
                mHandler.getThread().setUncaughtExceptionHandler(new NoOpExceptionHandler());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // Cannot use Tasks.await() because we might be on the UI thread.
        try {
            final CountDownLatch latch;
            latch = new CountDownLatch(1);
            stop(true).addOnCompleteListener(mHandler.getExecutor(), task -> latch.countDown());
            boolean success = latch.await(6, TimeUnit.SECONDS);
            if (!success) {
                // This thread is likely stuck. The reason might be deadlock issues in the internal
                // camera implementation, at least in emulators: see Camera1Engine and Camera2Engine
                // onStopEngine() implementation and comments.
                LOG.e("DESTROY: Could not destroy synchronously after 6 seconds.", "Current thread:", Thread.currentThread(), "Handler thread:", mHandler.getThread());
                depth++;
                if (depth < DESTROY_RETRIES) {
                    recreateHandler(true);
                    LOG.e("DESTROY: Trying again on thread:", mHandler.getThread());
                    destroy(unrecoverably, depth);
                } else {
                    LOG.w("DESTROY: Giving up because DESTROY_RETRIES was reached.");
                }
            }
        } catch (Exception ignore) {
            ignore.printStackTrace();
        }
    }

    /**
     * Recreates the handler, to ensure we use a fresh one from now on.
     * If we suspect that handler is currently stuck, the orchestrator should be reset
     * because it hosts a chain of tasks and the last one will never complete.
     *
     * @param resetOrchestrator true to reset
     */
    private void recreateHandler(boolean resetOrchestrator) {
        if (mHandler != null) mHandler.destroy();
        mHandler = WorkerHandler.get("CameraViewEngine");
        mHandler.getThread().setUncaughtExceptionHandler(new CrashExceptionHandler());
        if (resetOrchestrator) mOrchestrator.reset();
    }

    @NonNull
    public final CameraState getState() {
        return mOrchestrator.getCurrentState();
    }

    //region Error handling

    @NonNull
    public final CameraState getTargetState() {
        return mOrchestrator.getTargetState();
    }

    public final boolean isChangingState() {
        return mOrchestrator.hasPendingStateChange();
    }

    /**
     * Calls {@link #stop(boolean)} and waits for it.
     * Not final due to mockito requirements.
     * <p>
     * If unrecoverably is true, this also releases resources and the engine will not be in a
     * functional state after. If forever is false, this really is just a synchronous stop.
     * <p>
     * NOTE: Should not be called on the orchestrator thread! This would cause deadlocks due to us
     * awaiting for {@link #stop(boolean)} to return.
     */
    public void destroy(boolean unrecoverably) {
        destroy(unrecoverably, 0);
    }

    @NonNull
    @EngineThread
    private Task<Void> startEngine() {
        return mOrchestrator.scheduleStateChange(CameraState.OFF, CameraState.ENGINE, true, () -> {
            if (!collectCameraInfo(getFacing())) {
                LOG.e("onStartEngine:", "No camera available for facing", getFacing());
                throw new CameraException(CameraException.REASON_NO_CAMERA);
            }
            return onStartEngine();
        }).onSuccessTask(new SuccessContinuation<CameraOptions, Void>() {
            @NonNull
            @Override
            public Task<Void> then(@Nullable CameraOptions cameraOptions) {
                // Put this on the outer task so we're sure it's called after getState() is changed.
                // This was breaking some tests on rare occasions.
                if (cameraOptions == null) throw new RuntimeException("Null options!");
                mCallback.dispatchOnCameraOpened(cameraOptions);
                return Tasks.forResult(null);
            }
        });
    }

    //endregion

    //region State management

    @SuppressWarnings("WeakerAccess")
    public void restart() {
        LOG.i("RESTART:", "scheduled. State:", getState());
        stop(false);
        start();
    }

    @NonNull
    public Task<Void> start() {
        LOG.i("START:", "scheduled. State:", getState());
        Task<Void> engine = startEngine();
        startBind();
        startPreview();
        return engine;
    }

    @NonNull
    public Task<Void> stop(final boolean swallowExceptions) {
        LOG.i("STOP:", "scheduled. State:", getState());
        stopPreview(swallowExceptions);
        stopBind(swallowExceptions);
        return stopEngine(swallowExceptions);
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    @NonNull
    protected Task<Void> restartBind() {
        LOG.i("RESTART BIND:", "scheduled. State:", getState());
        stopPreview(false);
        stopBind(false);
        startBind();
        return startPreview();
    }

    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    @NonNull
    protected Task<Void> restartPreview() {
        LOG.i("RESTART PREVIEW:", "scheduled. State:", getState());
        stopPreview(false);
        return startPreview();
    }

    @NonNull
    @EngineThread
    private Task<Void> stopEngine(boolean swallowExceptions) {
        return mOrchestrator.scheduleStateChange(CameraState.ENGINE, CameraState.OFF, !swallowExceptions, this::onStopEngine).addOnSuccessListener(aVoid -> {
            // Put this on the outer task so we're sure it's called after getState() is OFF.
            // This was breaking some tests on rare occasions.
            mCallback.dispatchOnCameraClosed();
        });
    }

    @NonNull
    @EngineThread
    private Task<Void> startBind() {
        return mOrchestrator.scheduleStateChange(CameraState.ENGINE, CameraState.BIND, true, () -> {
            if (getPreview() != null && getPreview().hasSurface()) {
                return onStartBind();
            } else {
                return Tasks.forCanceled();
            }
        });
    }

    /**
     * Camera is about to be opened. Implementors should look into available cameras
     * and see if anyone matches the given {@link Facing value}.
     * <p>
     * If so, implementors should set {@link Angles#setSensorOffset(Facing, int)}
     * and any other information (like camera ID) needed to start the engine.
     *
     * @param facing the facing value
     * @return true if we have one
     */
    @EngineThread
    protected abstract boolean collectCameraInfo(@NonNull Facing facing);

    /**
     * Starts the engine.
     *
     * @return a task
     */
    @NonNull
    @EngineThread
    protected abstract Task<CameraOptions> onStartEngine();

    /**
     * Stops the engine.
     * Stop events should generally not throw exceptions. We
     * want to release resources either way.
     *
     * @return a task
     */
    @NonNull
    @EngineThread
    protected abstract Task<Void> onStopEngine();

    //endregion

    //region Start & Stop the engine

    @SuppressWarnings("UnusedReturnValue")
    @NonNull
    @EngineThread
    private Task<Void> stopBind(boolean swallowExceptions) {
        return mOrchestrator.scheduleStateChange(CameraState.BIND, CameraState.ENGINE, !swallowExceptions, this::onStopBind);
    }

    @NonNull
    @EngineThread
    private Task<Void> startPreview() {
        return mOrchestrator.scheduleStateChange(CameraState.BIND, CameraState.PREVIEW, true, this::onStartPreview);
    }

    /**
     * Starts the binding process.
     *
     * @return a task
     */
    @NonNull
    @EngineThread
    protected abstract Task<Void> onStartBind();

    /**
     * Stops the binding process.
     * Stop events should generally not throw exceptions. We
     * want to release resources either way.
     *
     * @return a task
     */
    @NonNull
    @EngineThread
    protected abstract Task<Void> onStopBind();

    @SuppressWarnings("UnusedReturnValue")
    @NonNull
    @EngineThread
    private Task<Void> stopPreview(boolean swallowExceptions) {
        return mOrchestrator.scheduleStateChange(CameraState.PREVIEW, CameraState.BIND, !swallowExceptions, this::onStopPreview);
    }

    //endregion

    //region Start & Stop binding

    public abstract void setExposureCorrection(float EVvalue, @NonNull float[] bounds, @Nullable PointF[] points, boolean notify);

    /**
     * Starts the preview streaming.
     *
     * @return a task
     */
    @NonNull
    @EngineThread
    protected abstract Task<Void> onStartPreview();

    /**
     * Stops the preview streaming.
     * Stop events should generally not throw exceptions. We
     * want to release resources either way.
     *
     * @return a task
     */
    @NonNull
    @EngineThread
    protected abstract Task<Void> onStopPreview();

    /**
     * The surface is now available, which means that step 1 has completed.
     * If we have also completed step 2, go on with binding and streaming.
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    public final void onSurfaceAvailable() {
        LOG.i("onSurfaceAvailable:", "Size is", getPreview().getSurfaceSize());
        startBind();
        startPreview();
    }

    //endregion

    //region Start & Stop preview

    @Override
    public final void onSurfaceDestroyed() {
        LOG.i("onSurfaceDestroyed");
        stopPreview(false);
        stopBind(false);
    }

    @NonNull
    public abstract Angles getAngles();

    @NonNull
    public abstract FrameManager getFrameManager();

    @Nullable
    public abstract CameraOptions getCameraOptions();

    //endregion

    //region Surface callbacks

    @Nullable
    public abstract Size getPictureSize(@NonNull Reference reference);

    @Nullable
    public abstract Size getVideoSize(@NonNull Reference reference);

    //endregion

    //region Abstract getters

    @Nullable
    public abstract Size getPreviewStreamSize(@NonNull Reference reference);

    @Nullable
    public abstract Size getUncroppedSnapshotSize(@NonNull Reference reference);

    @Nullable
    public abstract CameraPreview getPreview();

    public abstract void setPreview(@NonNull CameraPreview cameraPreview);

    @Nullable
    public abstract Overlay getOverlay();

    public abstract void setOverlay(@Nullable Overlay overlay);

    @Nullable
    public abstract SizeSelector getPreviewStreamSizeSelector();

    //endregion

    //region Abstract APIs

    public abstract void setPreviewStreamSizeSelector(@Nullable SizeSelector selector);

    @NonNull
    public abstract SizeSelector getPictureSizeSelector();

    public abstract void setPictureSizeSelector(@NonNull SizeSelector selector);

    @NonNull
    public abstract SizeSelector getVideoSizeSelector();

    public abstract void setVideoSizeSelector(@NonNull SizeSelector selector);

    public abstract long getVideoMaxSize();

    public abstract void setVideoMaxSize(long videoMaxSizeBytes);

    public abstract int getVideoMaxDuration();

    public abstract void setVideoMaxDuration(int videoMaxDurationMillis);

    @NonNull
    public abstract VideoCodec getVideoCodec();

    public abstract void setVideoCodec(@NonNull VideoCodec codec);

    public abstract int getVideoBitRate();

    public abstract void setVideoBitRate(int videoBitRate);

    public abstract int getAudioBitRate();

    public abstract void setAudioBitRate(int audioBitRate);

    @NonNull
    public abstract AudioCodec getAudioCodec();

    public abstract void setAudioCodec(@NonNull AudioCodec codec);

    public abstract int getSnapshotMaxWidth();

    public abstract void setSnapshotMaxWidth(int maxWidth);

    public abstract int getSnapshotMaxHeight();

    public abstract void setSnapshotMaxHeight(int maxHeight);

    public abstract int getFrameProcessingMaxWidth();

    public abstract void setFrameProcessingMaxWidth(int maxWidth);

    public abstract int getFrameProcessingMaxHeight();

    public abstract void setFrameProcessingMaxHeight(int maxHeight);

    public abstract int getFrameProcessingFormat();

    public abstract void setFrameProcessingFormat(int format);

    public abstract int getFrameProcessingPoolSize();

    public abstract void setFrameProcessingPoolSize(int poolSize);

    public abstract long getAutoFocusResetDelay();

    public abstract void setAutoFocusResetDelay(long delayMillis);

    @NonNull
    public abstract Facing getFacing();

    public abstract void setFacing(final @NonNull Facing facing);

    @NonNull
    public abstract Audio getAudio();

    public abstract void setAudio(@NonNull Audio audio);

    @NonNull
    public abstract Mode getMode();

    public abstract void setMode(@NonNull Mode mode);

    public abstract void setZoom(float zoom, @Nullable PointF[] points, boolean notify);

    public abstract float getZoomValue();

    public abstract void startAutoFocus(@Nullable Gesture gesture, @NonNull MeteringRegions regions, @NonNull PointF legacyPoint);

    public abstract float getExposureCorrectionValue();

    @NonNull
    public abstract Flash getFlash();

    public abstract void setFlash(@NonNull Flash flash);

    @NonNull
    public abstract WhiteBalance getWhiteBalance();

    public abstract void setWhiteBalance(@NonNull WhiteBalance whiteBalance);

    @NonNull
    public abstract Hdr getHdr();

    public abstract void setHdr(@NonNull Hdr hdr);

    @Nullable
    public abstract Location getLocation();

    public abstract void setLocation(@Nullable Location location);

    @NonNull
    public abstract PictureFormat getPictureFormat();

    public abstract void setPictureFormat(@NonNull PictureFormat pictureFormat);

    public abstract boolean getPreviewFrameRateExact();

    public abstract void setPreviewFrameRateExact(boolean previewFrameRateExact);

    public abstract float getPreviewFrameRate();

    public abstract void setPreviewFrameRate(float previewFrameRate);

    public abstract void setHasFrameProcessors(boolean hasFrameProcessors);

    public abstract boolean hasFrameProcessors();

    public abstract boolean getPictureMetering();

    public abstract void setPictureMetering(boolean enable);

    public abstract boolean getPictureSnapshotMetering();

    public abstract void setPictureSnapshotMetering(boolean enable);

    public abstract void takeVideo(@NonNull VideoResult.Stub stub, @Nullable File file, @Nullable FileDescriptor fileDescriptor);

    public abstract void setPlaySounds(boolean playSounds);

    public abstract boolean isTakingPicture();

    public abstract void takePicture(@NonNull PictureResult.Stub stub);

    public abstract void takePictureSnapshot(final @NonNull PictureResult.Stub stub);

    public abstract boolean isTakingVideo();

    public abstract void takeVideoSnapshot(@NonNull VideoResult.Stub stub, @NonNull File file);

    public abstract void stopVideo();

    public interface Callback {
        @NonNull
        Context getContext();

        void dispatchOnCameraOpened(@NonNull CameraOptions options);

        void dispatchOnCameraClosed();

        void onCameraPreviewStreamSizeChanged();

        void dispatchOnPictureShutter(boolean shouldPlaySound);

        void dispatchOnVideoTaken(@NonNull VideoResult.Stub stub);

        void dispatchOnPictureTaken(@NonNull PictureResult.Stub stub);

        void dispatchOnFocusStart(@Nullable Gesture trigger, @NonNull PointF where);

        void dispatchOnFocusEnd(@Nullable Gesture trigger, boolean success, @NonNull PointF where);

        void dispatchOnZoomChanged(final float newValue, @Nullable final PointF[] fingers);

        void dispatchOnExposureCorrectionChanged(float newValue, @NonNull float[] bounds, @Nullable PointF[] fingers);

        void dispatchFrame(@NonNull Frame frame);

        void dispatchError(CameraException exception);

        void dispatchOnVideoRecordingStart();

        void dispatchOnVideoRecordingEnd();
    }

    /**
     * A static exception handler used during destruction to avoid leaks,
     * since the default handler is not static and the thread might survive the engine.
     */
    private static class NoOpExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
            LOG.w("EXCEPTION:", "In the NoOpExceptionHandler, probably while destroying.", "Thread:", thread, "Error:", throwable);
        }
    }

    /**
     * The base exception handler, which inspects the exception and
     * decides what to do.
     */
    private class CrashExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
            handleException(throwable, true);
        }
    }
    //endregion
}
