package com.base.cameraview.engine.action

import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import com.base.cameraview.CameraLogger
import com.base.cameraview.engine.CameraEngine

class LogAction : BaseAction() {
    private var lastLog: String? = null

    override fun onCaptureCompleted(
        holder: ActionHolder,
        request: CaptureRequest,
        result: TotalCaptureResult
    ) {
        super.onCaptureCompleted(holder, request, result)
        val aeMode = result.get(CaptureResult.CONTROL_AE_MODE)
        val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
        val afState = result.get(CaptureResult.CONTROL_AF_STATE)
        val aeLock = result.get(CaptureResult.CONTROL_AE_LOCK)
        val aeTriggerState = result.get(CaptureResult.CONTROL_AE_PRECAPTURE_TRIGGER)
        val afTriggerState = result.get(CaptureResult.CONTROL_AF_TRIGGER)
        val log = "aeMode: " + aeMode + " aeLock: " + aeLock +
                " aeState: " + aeState + " aeTriggerState: " + aeTriggerState +
                " afState: " + afState + " afTriggerState: " + afTriggerState
        if (log != lastLog) {
            lastLog = log
            LOG.i(log)
        }
    }

    override fun onCompleted(holder: ActionHolder) {
        super.onCompleted(holder)
        state = 0 // set another state.
        start(holder) // restart.
    }

    companion object {
        private val LOG
                : CameraLogger = CameraLogger.create(CameraEngine::class.java.simpleName)
    }
}
