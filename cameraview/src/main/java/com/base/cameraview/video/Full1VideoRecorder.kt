package com.base.cameraview.video

import android.hardware.Camera
import android.media.CamcorderProfile
import android.media.MediaRecorder
import com.base.cameraview.VideoResult
import com.base.cameraview.engine.Camera1Engine
import com.base.cameraview.internal.CamcorderProfiles.get

/**
 * A [VideoRecorder] that uses [MediaRecorder] APIs
 * for the Camera1 engine.
 */
class Full1VideoRecorder(
    private val mEngine: Camera1Engine,
    private val mCamera: Camera, private val mCameraId: Int
) : FullVideoRecorder(
    mEngine
) {
    override fun applyVideoSource(
        stub: VideoResult.Stub,
        mediaRecorder: MediaRecorder
    ) {
        mediaRecorder.setCamera(mCamera)
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA)
    }

    override fun getCamcorderProfile(stub: VideoResult.Stub): CamcorderProfile {
        // Get a profile of quality compatible with the chosen size.
        val size = if (stub.rotation % 180 != 0) stub.size.flip() else stub.size
        return get(mCameraId, size)
    }

    override fun onDispatchResult() {
        // Restore frame processing.
        mCamera.setPreviewCallbackWithBuffer(mEngine)
        super.onDispatchResult()
    }
}
