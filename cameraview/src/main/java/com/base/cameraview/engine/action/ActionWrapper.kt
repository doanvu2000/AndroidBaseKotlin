package com.base.cameraview.engine.action

import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult

/**
 * This can be used to add functionality around a base action.
 */
abstract class ActionWrapper : BaseAction() {
    /**
     * Should return the wrapped action.
     *
     * @return the wrapped action
     */
    abstract val action: BaseAction

    override fun onStart(holder: ActionHolder) {
        super.onStart(holder)
        this.action.addCallback(object : ActionCallback {
            override fun onActionStateChanged(action: Action, state: Int) {
                changeState(state)
                if (state == Action.Companion.STATE_COMPLETED) {
                    action.removeCallback(this)
                }
            }
        })
        this.action.onStart(holder)
    }

    override fun onAbort(holder: ActionHolder) {
        super.onAbort(holder)
        this.action.onAbort(holder)
    }

    override fun onCaptureStarted(holder: ActionHolder, request: CaptureRequest) {
        super.onCaptureStarted(holder, request)
        this.action.onCaptureStarted(holder, request)
    }

    override fun onCaptureProgressed(
        holder: ActionHolder, request: CaptureRequest, result: CaptureResult
    ) {
        super.onCaptureProgressed(holder, request, result)
        this.action.onCaptureProgressed(holder, request, result)
    }

    override fun onCaptureCompleted(
        holder: ActionHolder, request: CaptureRequest, result: TotalCaptureResult
    ) {
        super.onCaptureCompleted(holder, request, result)
        this.action.onCaptureCompleted(holder, request, result)
    }
}
