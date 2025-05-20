package com.base.cameraview.engine.action

import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult

/**
 * The Action class encapsulates logic for completing an action in a Camera2 environment.
 * In this case, we are often interested in constantly receiving the [CaptureResult]
 * and [CaptureRequest] callbacks, as well as applying changes to a
 * [CaptureRequest.Builder] and having them applied to the sensor.
 *
 *
 * The Action class receives the given callbacks and can operate over the engine
 *
 *
 * Each Action operates on a given state in a given moment. This base class offers the
 * [.STATE_COMPLETED] state which is common to all actions.
 *
 *
 * See [BaseAction] for a base implementation.
 */
interface Action {
    /**
     * Returns the current state.
     *
     * @return the state
     */
    val state: Int

    /**
     * Starts this action.
     *
     * @param holder the holder
     */
    fun start(holder: ActionHolder)

    /**
     * Aborts this action.
     *
     * @param holder the holder
     */
    fun abort(holder: ActionHolder)

    /**
     * Adds an [ActionCallback] to receive state
     * change events.
     *
     * @param callback a callback
     */
    fun addCallback(callback: ActionCallback)

    /**
     * Removes a previously added callback.
     *
     * @param callback a callback
     */
    fun removeCallback(callback: ActionCallback)

    /**
     * Called from [android.hardware.camera2.CameraCaptureSession.CaptureCallback.onCaptureStarted].
     *
     * @param holder  the holder
     * @param request the request
     */
    fun onCaptureStarted(holder: ActionHolder, request: CaptureRequest)

    /**
     * Called from [android.hardware.camera2.CameraCaptureSession.CaptureCallback.onCaptureProgressed].
     *
     * @param holder  the holder
     * @param request the request
     * @param result  the result
     */
    fun onCaptureProgressed(
        holder: ActionHolder,
        request: CaptureRequest,
        result: CaptureResult
    )

    /**
     * Called from [android.hardware.camera2.CameraCaptureSession.CaptureCallback.onCaptureCompleted].
     *
     * @param holder  the holder
     * @param request the request
     * @param result  the result
     */
    fun onCaptureCompleted(
        holder: ActionHolder,
        request: CaptureRequest,
        result: TotalCaptureResult
    )

    companion object {
        const val STATE_COMPLETED: Int = Int.Companion.MAX_VALUE
    }
}
