package com.base.cameraview.engine.action

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult

/**
 * The holder of [Action].
 *
 *
 * This class should keep a list or set of currently running actions, and offers
 * to them the base Camera2 objects that are needed to apply changes.
 *
 *
 * This class, or an holder of it, should also forward the capture callbacks
 * our implementation.
 */
interface ActionHolder {
    /**
     * Adds a new action
     *
     * @param action action
     */
    fun addAction(action: Action)

    /**
     * Removes a previously added action
     *
     * @param action action
     */
    fun removeAction(action: Action)

    /**
     * Returns the [CameraCharacteristics] of the current
     * camera device.
     *
     * @param action action
     * @return characteristics
     */
    fun getCharacteristics(action: Action): CameraCharacteristics

    /**
     * Returns the latest [TotalCaptureResult]. Can be used
     * by actions to start querying the state before receiving their
     * first frame.
     *
     * @param action action
     * @return last result
     */
    fun getLastResult(action: Action): TotalCaptureResult?

    /**
     * Returns the current [CaptureRequest.Builder] so that
     * actions can apply changes to it and later submit them.
     *
     * @param action action
     * @return the builder
     */
    fun getBuilder(action: Action): CaptureRequest.Builder

    /**
     * Applies the current builder (as per [.getBuilder])
     * as a repeating request on the preview.
     *
     * @param source action
     */
    fun applyBuilder(source: Action)

    /**
     * Applies the given builder as a single capture request.
     * Callers can catch the exception and choose what to do.
     *
     * @param source  action
     * @param builder builder
     * @throws CameraAccessException camera exception
     */
    @Throws(CameraAccessException::class)
    fun applyBuilder(source: Action, builder: CaptureRequest.Builder)
}
