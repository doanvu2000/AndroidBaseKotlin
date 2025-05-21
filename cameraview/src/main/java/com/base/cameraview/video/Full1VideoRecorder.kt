package com.base.cameraview.video;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;

import androidx.annotation.NonNull;

import com.base.cameraview.VideoResult;
import com.base.cameraview.engine.Camera1Engine;
import com.base.cameraview.internal.CamcorderProfiles;
import com.base.cameraview.size.Size;

/**
 * A {@link VideoRecorder} that uses {@link MediaRecorder} APIs
 * for the Camera1 engine.
 */
public class Full1VideoRecorder extends FullVideoRecorder {

    private final Camera1Engine mEngine;
    private final Camera mCamera;
    private final int mCameraId;

    public Full1VideoRecorder(@NonNull Camera1Engine engine,
                              @NonNull Camera camera, int cameraId) {
        super(engine);
        mCamera = camera;
        mEngine = engine;
        mCameraId = cameraId;
    }

    @Override
    protected void applyVideoSource(@NonNull VideoResult.Stub stub,
                                    @NonNull MediaRecorder mediaRecorder) {
        mediaRecorder.setCamera(mCamera);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
    }

    @NonNull
    @Override
    protected CamcorderProfile getCamcorderProfile(@NonNull VideoResult.Stub stub) {
        // Get a profile of quality compatible with the chosen size.
        Size size = stub.rotation % 180 != 0 ? stub.size.flip() : stub.size;
        return CamcorderProfiles.get(mCameraId, size);
    }

    @Override
    protected void onDispatchResult() {
        // Restore frame processing.
        mCamera.setPreviewCallbackWithBuffer(mEngine);
        super.onDispatchResult();
    }
}
