package com.base.cameraview.frame;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.base.cameraview.engine.offset.Angles;
import com.base.cameraview.size.Size;

import java.util.concurrent.LinkedBlockingQueue;

public class ByteBufferFrameManager extends FrameManager<byte[]> {

    /**
     * In this mode, we have a {@link #mBufferCallback} and dispatch
     * new buffers to the callback.
     */
    private final static int BUFFER_MODE_DISPATCH = 0;
    /**
     * In this mode, we have a {@link #mBufferQueue} where we store
     * buffers and only dispatch when requested.
     */
    private final static int BUFFER_MODE_ENQUEUE = 1;
    private final int mBufferMode;
    private LinkedBlockingQueue<byte[]> mBufferQueue;
    private BufferCallback mBufferCallback;

    /**
     * Construct a new frame manager.
     *
     * @param poolSize the size of the backing pool.
     * @param callback a callback
     */
    public ByteBufferFrameManager(int poolSize, @Nullable BufferCallback callback) {
        super(poolSize, byte[].class);
        if (callback != null) {
            mBufferCallback = callback;
            mBufferMode = BUFFER_MODE_DISPATCH;
        } else {
            mBufferQueue = new LinkedBlockingQueue<>(poolSize);
            mBufferMode = BUFFER_MODE_ENQUEUE;
        }
    }

    @Override
    public void setUp(int format, @NonNull Size size, @NonNull Angles angles) {
        super.setUp(format, size, angles);
        int bytes = getFrameBytes();
        for (int i = 0; i < getPoolSize(); i++) {
            if (mBufferMode == BUFFER_MODE_DISPATCH) {
                mBufferCallback.onBufferAvailable(new byte[bytes]);
            } else {
                mBufferQueue.offer(new byte[bytes]);
            }
        }
    }

    /**
     * Returns a new byte buffer than can be filled.
     * This can only be called in {@link #BUFFER_MODE_ENQUEUE} mode! Where the frame
     * manager also holds a queue of the byte buffers.
     * <p>
     * If not null, the buffer returned by this method can be filled and used to get
     * a new frame through {@link FrameManager#getFrame(Object, long)}.
     *
     * @return a buffer, or null
     */
    @Nullable
    public byte[] getBuffer() {
        if (mBufferMode != BUFFER_MODE_ENQUEUE) {
            throw new IllegalStateException("Can't call getBuffer() " +
                    "when not in BUFFER_MODE_ENQUEUE.");
        }
        return mBufferQueue.poll();
    }

    /**
     * Can be called if the buffer obtained by {@link #getBuffer()}
     * was not used to construct a frame, so it can be put back into the queue.
     *
     * @param buffer a buffer
     */
    public void onBufferUnused(@NonNull byte[] buffer) {
        if (mBufferMode != BUFFER_MODE_ENQUEUE) {
            throw new IllegalStateException("Can't call onBufferUnused() " +
                    "when not in BUFFER_MODE_ENQUEUE.");
        }

        if (isSetUp()) {
            mBufferQueue.offer(buffer);
        } else {
            LOG.w("onBufferUnused: buffer was returned but we're not set up anymore.");
        }
    }

    @Override
    protected void onFrameDataReleased(@NonNull byte[] data, boolean recycled) {
        if (recycled && data.length == getFrameBytes()) {
            if (mBufferMode == BUFFER_MODE_DISPATCH) {
                mBufferCallback.onBufferAvailable(data);
            } else {
                mBufferQueue.offer(data);
            }
        }
    }

    @NonNull
    @Override
    protected byte[] onCloneFrameData(@NonNull byte[] data) {
        byte[] clone = new byte[data.length];
        System.arraycopy(data, 0, clone, 0, data.length);
        return clone;
    }

    /**
     * Releases all frames controlled by this manager and
     * clears the pool.
     * In BUFFER_MODE_ENQUEUE, releases also all the buffers.
     */
    @Override
    public void release() {
        super.release();
        if (mBufferMode == BUFFER_MODE_ENQUEUE) {
            mBufferQueue.clear();
        }
    }

    /**
     * Receives callbacks on buffer availability
     * (when a Frame is released, we reuse its buffer).
     */
    public interface BufferCallback {
        void onBufferAvailable(@NonNull byte[] buffer);
    }
}
