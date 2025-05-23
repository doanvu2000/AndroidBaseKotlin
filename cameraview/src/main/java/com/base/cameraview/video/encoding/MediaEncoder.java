package com.base.cameraview.video.encoding;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaFormat;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.base.cameraview.CameraLogger;
import com.base.cameraview.internal.WorkerHandler;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class MediaEncoder {

    private final static String TAG = MediaEncoder.class.getSimpleName();
    private final static CameraLogger LOG = CameraLogger.create(TAG);

    // Did some test to see which value would maximize our performance in the current setup
    // (infinite audio pool). Measured the time it would take to write a 30 seconds video.
    // Based on this, we'll go with TIMEOUT=0 for now.
    // INPUT_TIMEOUT_US 10000: 46 seconds
    // INPUT_TIMEOUT_US 1000: 37 seconds
    // INPUT_TIMEOUT_US 100: 33 seconds
    // INPUT_TIMEOUT_US 0: 32 seconds
    private final static int INPUT_TIMEOUT_US = 0;

    // 0 also seems to be the best, although it does not change so much.
    // Can't go too high or this is a bottleneck for the audio encoder.
    private final static int OUTPUT_TIMEOUT_US = 0;

    private final static int STATE_NONE = 0;
    private final static int STATE_PREPARING = 1;
    private final static int STATE_PREPARED = 2;
    private final static int STATE_STARTING = 3;
    private final static int STATE_STARTED = 4;
    // max timestamp was reached. we will keep draining but have asked the engine to stop us.
    // this step can be skipped in case stop() is called from outside before a limit is reached.
    private final static int STATE_LIMIT_REACHED = 5;
    private final static int STATE_STOPPING = 6;
    private final static int STATE_STOPPED = 7;
    private final String mName;
    private final Map<String, AtomicInteger> mPendingEvents = new HashMap<>();
    @SuppressWarnings("WeakerAccess")
    protected MediaCodec mMediaCodec;

    @SuppressWarnings("WeakerAccess")
    protected WorkerHandler mWorker;
    private int mState = STATE_NONE;
    private MediaEncoderEngine.Controller mController;
    private int mTrackIndex;
    private OutputBufferPool mOutputBufferPool;
    private MediaCodec.BufferInfo mBufferInfo;
    private MediaCodecBuffers mBuffers;
    private long mMaxLengthUs;
    private boolean mMaxLengthReached;

    private long mStartTimeMillis = 0; // In System.currentTimeMillis()
    private long mFirstTimeUs = Long.MIN_VALUE; // In unknown reference
    private long mLastTimeUs = 0;

    private long mDebugSetStateTimestamp = Long.MIN_VALUE;

    /**
     * Needs a readable name for the thread and for logging.
     *
     * @param name a name
     */
    @SuppressWarnings("WeakerAccess")
    protected MediaEncoder(@NonNull String name) {
        mName = name;
    }

    private void setState(int newState) {
        if (mDebugSetStateTimestamp == Long.MIN_VALUE) {
            mDebugSetStateTimestamp = System.currentTimeMillis();
        }
        long millis = System.currentTimeMillis() - mDebugSetStateTimestamp;
        mDebugSetStateTimestamp = System.currentTimeMillis();

        String newStateName = switch (newState) {
            case STATE_NONE -> "NONE";
            case STATE_PREPARING -> "PREPARING";
            case STATE_PREPARED -> "PREPARED";
            case STATE_STARTING -> "STARTING";
            case STATE_STARTED -> "STARTED";
            case STATE_LIMIT_REACHED -> "LIMIT_REACHED";
            case STATE_STOPPING -> "STOPPING";
            case STATE_STOPPED -> "STOPPED";
            default -> null;
        };
        LOG.w(mName, "setState:", newStateName, "millisSinceLastState:", millis);
        mState = newState;
    }

    /**
     * This encoder was attached to the engine. Keep the controller
     * and run the internal thread.
     * <p>
     * NOTE: it's important to call {@link WorkerHandler#post(Runnable)} instead of run()!
     * The internal actions can cause a stop, and due to how {@link WorkerHandler#run(Runnable)}
     * works, we might have {@link #onStop()} or {@link #onStopped()} to be executed before
     * the previous step has completed.
     */
    final void prepare(@NonNull final MediaEncoderEngine.Controller controller, final long maxLengthUs) {
        if (mState >= STATE_PREPARING) {
            LOG.e(mName, "Wrong state while preparing. Aborting.", mState);
            return;
        }
        mController = controller;
        mBufferInfo = new MediaCodec.BufferInfo();
        mMaxLengthUs = maxLengthUs;
        mWorker = WorkerHandler.get(mName);
        mWorker.getThread().setPriority(Thread.MAX_PRIORITY);
        LOG.i(mName, "Prepare was called. Posting.");
        mWorker.post(() -> {
            LOG.i(mName, "Prepare was called. Executing.");
            setState(STATE_PREPARING);
            onPrepare(controller, maxLengthUs);
            setState(STATE_PREPARED);
        });
    }

    /**
     * Start recording. This might be a lightweight operation
     * in case the encoder needs to wait for a certain event
     * like a "frame available".
     * <p>
     * The {@link #STATE_STARTED} state will be set when draining for the
     * first time (not when onStart ends).
     * <p>
     * NOTE: it's important to call {@link WorkerHandler#post(Runnable)} instead of run()!
     */
    final void start() {
        LOG.w(mName, "Start was called. Posting.");
        mWorker.post(() -> {
            if (mState < STATE_PREPARED || mState >= STATE_STARTING) {
                LOG.e(mName, "Wrong state while starting. Aborting.", mState);
                return;
            }
            setState(STATE_STARTING);
            LOG.w(mName, "Start was called. Executing.");
            try {
                onStart();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * The caller notifying of a certain event occurring.
     * Should analyze the string and see if the event is important.
     * <p>
     * NOTE: it's important to call {@link WorkerHandler#post(Runnable)} instead of run()!
     *
     * @param event what happened
     * @param data  object
     */
    @SuppressWarnings("ConstantConditions")
    final void notify(final @NonNull String event, final @Nullable Object data) {
        if (!mPendingEvents.containsKey(event)) mPendingEvents.put(event, new AtomicInteger(0));
        final AtomicInteger pendingEvents = mPendingEvents.get(event);
        pendingEvents.incrementAndGet();
        LOG.v(mName, "Notify was called. Posting. pendingEvents:", pendingEvents.intValue());
        mWorker.post(() -> {
            LOG.v(mName, "Notify was called. Executing. pendingEvents:", pendingEvents.intValue());
            onEvent(event, data);
            pendingEvents.decrementAndGet();
        });
    }

    /**
     * Stop recording. This involves signaling the end of stream and draining
     * all output left.
     * <p>
     * The {@link #STATE_STOPPED} state will be set when draining for the
     * last time (not when onStart ends).
     * <p>
     * NOTE: it's important to call {@link WorkerHandler#post(Runnable)} instead of run()!
     */
    final void stop() {
        if (mState >= STATE_STOPPING) {
            LOG.e(mName, "Wrong state while stopping. Aborting.", mState);
            return;
        }
        setState(STATE_STOPPING);
        LOG.w(mName, "Stop was called. Posting.");
        mWorker.post(() -> {
            LOG.w(mName, "Stop was called. Executing.");
            try {
                onStop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Called to prepare this encoder before starting.
     * Any initialization should be done here as it does not interfere with the original
     * thread (that, generally, is the rendering thread).
     * <p>
     * At this point subclasses MUST create the {@link #mMediaCodec} object.
     *
     * @param controller  the muxer controller
     * @param maxLengthUs the maxLength in microseconds
     */
    @EncoderThread
    protected abstract void onPrepare(@NonNull MediaEncoderEngine.Controller controller, long maxLengthUs);

    /**
     * Start recording. This might be a lightweight operation
     * in case the encoder needs to wait for a certain event
     * like a "frame available".
     */
    @EncoderThread
    protected abstract void onStart();

    /**
     * The caller notifying of a certain event occurring.
     * Should analyze the string and see if the event is important.
     *
     * @param event what happened
     * @param data  object
     */
    @EncoderThread
    protected void onEvent(@NonNull String event, @Nullable Object data) {
    }

    /**
     * Stop recording. This involves signaling the end of stream and draining
     * all output left.
     */
    @EncoderThread
    protected abstract void onStop();

    /**
     * Called by {@link #drainOutput(boolean)} when we get an EOS signal (not necessarily in the
     * parameters, might also be through an input buffer flag).
     * <p>
     * This is a good moment to release all resources, although the muxer might still
     * be alive (we wait for the other Encoder, see MediaEncoderEngine.Controller).
     */
    @CallSuper
    protected void onStopped() {
        LOG.w(mName, "is being released. Notifying controller and releasing codecs.");
        // TODO should we call notifyStopped after this method ends?
        mController.notifyStopped(mTrackIndex);
        mMediaCodec.stop();
        mMediaCodec.release();
        mMediaCodec = null;
        mOutputBufferPool.clear();
        mOutputBufferPool = null;
        mBuffers = null;
        setState(STATE_STOPPED);
        mWorker.destroy();
    }

    /**
     * Returns a new input buffer and index, waiting at most {@link #INPUT_TIMEOUT_US} if none
     * is available. Callers should check the boolean result - true if the buffer was filled.
     *
     * @param holder the input buffer holder
     * @return true if acquired
     */
    @SuppressWarnings("WeakerAccess")
    protected boolean tryAcquireInputBuffer(@NonNull InputBuffer holder) {
        if (mBuffers == null) {
            mBuffers = new MediaCodecBuffers(mMediaCodec);
        }
        int inputBufferIndex = mMediaCodec.dequeueInputBuffer(INPUT_TIMEOUT_US);
        if (inputBufferIndex < 0) {
            return false;
        } else {
            holder.index = inputBufferIndex;
            holder.data = mBuffers.getInputBuffer(inputBufferIndex);
            return true;
        }
    }

    /**
     * Returns a new input buffer and index, waiting indefinitely if none is available.
     * The buffer should be written into, then be passed to {@link #encodeInputBuffer(InputBuffer)}.
     *
     * @param holder the input buffer holder
     */
    @SuppressWarnings({"StatementWithEmptyBody", "WeakerAccess"})
    protected void acquireInputBuffer(@NonNull InputBuffer holder) {
        while (!tryAcquireInputBuffer(holder)) {
        }
    }

    /**
     * Encode data into the {@link #mMediaCodec}.
     *
     * @param buffer the input buffer
     */
    @SuppressWarnings("WeakerAccess")
    protected void encodeInputBuffer(InputBuffer buffer) {
        LOG.v(mName, "ENCODING - Buffer:", buffer.index, "Bytes:", buffer.length, "Presentation:", buffer.timestamp);
        try {
            if (buffer.isEndOfStream) { // send EOS
                mMediaCodec.queueInputBuffer(buffer.index, 0, 0, buffer.timestamp, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            } else {
                mMediaCodec.queueInputBuffer(buffer.index, 0, buffer.length, buffer.timestamp, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Extracts all pending data that was written and encoded into {@link #mMediaCodec},
     * and forwards it to the muxer.
     * <p>
     * If drainAll is not set, this returns after TIMEOUT_USEC if there is no more data to drain.
     * If drainAll is set, we wait until we see EOS on the output.
     * Calling this with drainAll set should be done once, right before stopping the muxer.
     *
     * @param drainAll whether to drain all
     */
    @SuppressLint("LogNotTimber")
    @SuppressWarnings("WeakerAccess")
    protected final void drainOutput(boolean drainAll) {
        LOG.i(mName, "DRAINING - EOS:", drainAll);
        if (mMediaCodec == null) {
            LOG.e("drain() was called before prepare() or after releasing.");
            return;
        }
        if (mBuffers == null) {
            mBuffers = new MediaCodecBuffers(mMediaCodec);
        }
        try {
            while (true) {
                int encoderStatus = mMediaCodec.dequeueOutputBuffer(mBufferInfo, OUTPUT_TIMEOUT_US);
                LOG.i(mName, "DRAINING - Got status:", encoderStatus);
                if (encoderStatus == MediaCodec.INFO_TRY_AGAIN_LATER) {
                    // no output available yet
                    if (!drainAll) break; // out of while

                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                    // not expected for an encoder
                    mBuffers.onOutputBuffersChanged();

                } else if (encoderStatus == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                    // should happen before receiving buffers, and should only happen once
                    if (!mController.isStarted()) {
                        MediaFormat newFormat = mMediaCodec.getOutputFormat();
                        mTrackIndex = mController.notifyStarted(newFormat);
                        setState(STATE_STARTED);
                        mOutputBufferPool = new OutputBufferPool(mTrackIndex);
                    } else {
                        // throw new RuntimeException("MediaFormat changed twice.");
                        // Seen this happen in API31. TODO handle differently?
                    }
                } else if (encoderStatus < 0) {
                    LOG.e("Unexpected result from dequeueOutputBuffer: " + encoderStatus);
                    // let's ignore it
                } else {
                    ByteBuffer encodedData = mBuffers.getOutputBuffer(encoderStatus);

                    // Codec config means that config data was pulled out and fed to the muxer
                    // when we got the INFO_OUTPUT_FORMAT_CHANGED status. Ignore it.
                    boolean isCodecConfig = (mBufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0;
                    if (!isCodecConfig && mController.isStarted() && mBufferInfo.size != 0) {

                        // adjust the ByteBuffer values to match BufferInfo (not needed?)
                        encodedData.position(mBufferInfo.offset);
                        encodedData.limit(mBufferInfo.offset + mBufferInfo.size);

                        // Store mStartTimeUs and mLastTimeUs, useful to detect the max length
                        // reached and stop recording when needed.
                        if (mFirstTimeUs == Long.MIN_VALUE) {
                            mFirstTimeUs = mBufferInfo.presentationTimeUs;
                            LOG.w(mName, "DRAINING - Got the first presentation time:", mFirstTimeUs);
                        }
                        mLastTimeUs = mBufferInfo.presentationTimeUs;

                        // Adjust the presentation times. Subclasses can pass a presentation time in any
                        // reference system - possibly some that has no real meaning, and frequently,
                        // presentation times from different encoders have a different time-base.
                        // To address this, encoders are required to call notifyFirstFrameMillis
                        // so we can adjust here - moving to 1970 reference.
                        // Extra benefit: we never pass a pts equal to 0, which some encoders refuse.
                        mBufferInfo.presentationTimeUs = (mStartTimeMillis * 1000) + mLastTimeUs - mFirstTimeUs;

                        // Write.
                        LOG.v(mName, "DRAINING - About to write(). Adjusted presentation:", mBufferInfo.presentationTimeUs);
                        OutputBuffer buffer = mOutputBufferPool.get();
                        //noinspection ConstantConditions
                        buffer.info = mBufferInfo;
                        buffer.trackIndex = mTrackIndex;
                        buffer.data = encodedData;
                        onWriteOutput(mOutputBufferPool, buffer);
                    }
                    mMediaCodec.releaseOutputBuffer(encoderStatus, false);

                    // Check for the maxLength constraint (with appropriate conditions)
                    // Not needed if drainAll because we already were asked to stop
                    if (!drainAll && !mMaxLengthReached && mFirstTimeUs != Long.MIN_VALUE && mLastTimeUs - mFirstTimeUs > mMaxLengthUs) {
                        LOG.w(mName, "DRAINING - Reached maxLength! mLastTimeUs:", mLastTimeUs, "mStartTimeUs:", mFirstTimeUs, "mDeltaUs:", mLastTimeUs - mFirstTimeUs, "mMaxLengthUs:", mMaxLengthUs);
                        onMaxLengthReached();
                        break;
                    }

                    // Check for the EOS flag so we can call onStopped.
                    if ((mBufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        LOG.w(mName, "DRAINING - Got EOS. Releasing the codec.");
                        onStopped();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @CallSuper
    protected void onWriteOutput(@NonNull OutputBufferPool pool, @NonNull OutputBuffer buffer) {
        mController.write(pool, buffer);
    }

    protected abstract int getEncodedBitRate();

    /**
     * Returns the max length setting, in microseconds, which can be used
     * to compute the current state and eventually call {@link #notifyMaxLengthReached()}.
     * This is not a requirement for subclasses - we do this check anyway when draining,
     * but doing so might be better.
     *
     * @return the max length setting
     */
    @SuppressWarnings("WeakerAccess")
    protected long getMaxLengthUs() {
        return mMaxLengthUs;
    }

    /**
     * Called by subclasses to notify that the max length was reached.
     * We will move to {@link #STATE_LIMIT_REACHED} and request a stop.
     */
    @SuppressWarnings("WeakerAccess")
    protected void notifyMaxLengthReached() {
        onMaxLengthReached();
    }

    @SuppressWarnings("WeakerAccess")
    protected boolean hasReachedMaxLength() {
        return mMaxLengthReached;
    }

    /**
     * Called by us (during {@link #drainOutput(boolean)}) or by subclasses
     * (through {@link #notifyMaxLengthReached()}) to notify that we reached the
     * max length allowed. We will move to {@link #STATE_LIMIT_REACHED} and request a stop.
     */
    private void onMaxLengthReached() {
        if (mMaxLengthReached) {
            LOG.w(mName, "onMaxLengthReached: Called twice.");
        } else {
            mMaxLengthReached = true;
            if (mState >= STATE_LIMIT_REACHED) {
                LOG.w(mName, "onMaxLengthReached: Reached in wrong state. Aborting.", mState);
            } else {
                LOG.w(mName, "onMaxLengthReached: Requesting a stop.");
                setState(STATE_LIMIT_REACHED);
                mController.requestStop(mTrackIndex);
            }
        }
    }

    /**
     * Should be called by subclasses to pass the milliseconds of the first frame - as soon
     * as this information is available. The milliseconds should be in the
     * {@link System#currentTimeMillis()} reference system, so we can coordinate between different
     * encoders.
     *
     * @param firstFrameMillis the milliseconds of the first frame presentation
     */
    @SuppressWarnings("WeakerAccess")
    protected final void notifyFirstFrameMillis(long firstFrameMillis) {
        mStartTimeMillis = firstFrameMillis;
    }

    /**
     * Returns the number of events (see {@link #onEvent(String, Object)}) that were scheduled
     * but still not passed to that function. Could be used to drop some of them if this
     * number is too high.
     *
     * @param event the event type
     * @return the pending events number
     */
    @SuppressWarnings({"SameParameterValue", "ConstantConditions", "WeakerAccess"})
    protected final int getPendingEvents(@NonNull String event) {
        return mPendingEvents.get(event).intValue();
    }
}
