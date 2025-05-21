package com.base.cameraview.video

import android.hardware.camera2.CaptureRequest
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.util.Log
import android.view.Surface
import com.base.cameraview.VideoResult
import com.base.cameraview.engine.Camera2Engine
import com.base.cameraview.engine.action.Action
import com.base.cameraview.engine.action.ActionHolder
import com.base.cameraview.engine.action.BaseAction
import com.base.cameraview.engine.action.CompletionCallback
import com.base.cameraview.internal.CamcorderProfiles.get

/**
 * A [VideoRecorder] that uses [MediaRecorder] APIs
 * for the Camera2 engine.
 */
class Full2VideoRecorder(engine: Camera2Engine, cameraId: String) : FullVideoRecorder(engine) {
    private val mCameraId: String = cameraId
    private val mHolder: ActionHolder = engine
    var inputSurface: Surface? = null
        private set

    override fun onStart() {
        // Do not start now. Instead, wait for the first frame.
        // Check that the request is the correct one, using the request tag.
        // The engine might have been changing the request to add our surface lately,
        // and we don't want to start on an old frame.
        val action: Action = object : BaseAction() {
            override fun onCaptureStarted(holder: ActionHolder, request: CaptureRequest) {
                super.onCaptureStarted(holder, request)
                val tag = holder.getBuilder(this).build().tag
                val currentTag = request.tag
                if (if (tag == null) currentTag == null else (tag == currentTag)) {
                    state = Action.Companion.STATE_COMPLETED
                }
            }
        }
        try {
            action.addCallback(object : CompletionCallback() {
                override fun onActionCompleted(action: Action) {
                    super@Full2VideoRecorder.onStart()
                }
            })
            action.start(mHolder)
        } catch (e: Exception) {
            Log.e("VideoRecorder", "onStart: error when onStart Fill2VideoRecorder.java")
            e.printStackTrace()
        }
    }

    override fun applyVideoSource(
        stub: VideoResult.Stub,
        mediaRecorder: MediaRecorder
    ) {
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
    }

    override fun getCamcorderProfile(stub: VideoResult.Stub): CamcorderProfile {
        // This was an option: get the surface from outside this class, using
        // MediaCodec.createPersistentInputSurface(). But it doesn't really help since the
        // Camera2 engine refuses a surface that has not been configured, so even with that trick
        // we would have to attach the surface to this recorder before creating the CameraSession.
        // mediaRecorder.setInputSurface(mInputSurface);
        val size = if (stub.rotation % 180 != 0) stub.size.flip() else stub.size
        return get(mCameraId, size)
    }

    /**
     * This method should be called just once.
     *
     * @param stub the video stub
     * @return a surface
     * @throws PrepareException if prepare went wrong
     */
    @Throws(PrepareException::class)
    fun createInputSurface(stub: VideoResult.Stub): Surface {
        if (!prepareMediaRecorder(stub)) {
            throw PrepareException(mError)
        }
        this.inputSurface = mMediaRecorder?.surface
        return this.inputSurface!!
    }

    inner class PrepareException(cause: Throwable?) : Exception(cause)
}
