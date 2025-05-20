package com.base.cameraview.engine.action

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import androidx.annotation.CallSuper

/**
 * The base implementation of [Action] that should always be subclassed,
 * instead of implementing the root interface itself.
 *
 *
 * It holds a list of callbacks and dispatches events to them, plus it cares about
 * its own lifecycle:
 * - when [.start] is called, we add ourselves to the holder list
 * - when [.STATE_COMPLETED] is reached, we remove ourselves from the holder list
 *
 *
 * This is very important in all cases.
 */
abstract class BaseAction : Action {
    private val callbacks: MutableList<ActionCallback> = ArrayList()
    override var state = 0
        set(newState) {
            if (newState != field) {
                field = newState
                for (callback in callbacks) {
                    callback.onActionStateChanged(this, field)
                }
                if (field == Action.Companion.STATE_COMPLETED) {
                    holder?.let {
                        holder?.removeAction(this)
                        onCompleted(holder!!)
                    }
                }
            }
        }
    private var holder: ActionHolder? = null
    private var needsOnStart = false

    override fun start(holder: ActionHolder) {
        this.holder = holder
        holder.addAction(this)
        if (holder.getLastResult(this) != null) {
            onStart(holder)
        } else {
            needsOnStart = true
        }
    }

    override fun abort(holder: ActionHolder) {
        holder.removeAction(this)
        if (!this.isCompleted) {
            onAbort(holder)
            state = Action.Companion.STATE_COMPLETED
        }
        needsOnStart = false
    }

    /**
     * Action was started and will soon receive events from the
     * holder stream.
     *
     * @param holder holder
     */
    @CallSuper
    open fun onStart(holder: ActionHolder) {
        // Repeating holder assignment here (already in start()) because we NEED it in start()
        // but some special actions will not call start() at all for their children.
        this.holder = holder
        // Overrideable
    }

    /**
     * Action was aborted and will not receive events from the
     * holder stream anymore. It will soon be marked as completed.
     *
     * @param holder holder
     */
    @Suppress("unused")
    open fun onAbort(holder: ActionHolder) {
        // Overrideable
    }

    @CallSuper
    override fun onCaptureStarted(holder: ActionHolder, request: CaptureRequest) {
        if (needsOnStart) {
            onStart(holder)
            needsOnStart = false
        }
    }

    override fun onCaptureProgressed(
        holder: ActionHolder,
        request: CaptureRequest,
        result: CaptureResult
    ) {
        // Overrideable
    }

    override fun onCaptureCompleted(
        holder: ActionHolder,
        request: CaptureRequest,
        result: TotalCaptureResult
    ) {
        // Overrideable
    }

    val isCompleted: Boolean
        /**
         * Whether this action has reached the completed state.
         *
         * @return true if completed
         */
        get() = state == Action.Companion.STATE_COMPLETED

    /**
     * Called when this action has completed (possibly aborted).
     *
     * @param holder holder
     */
    protected open fun onCompleted(holder: ActionHolder) {
        // Overrideable
    }

    /**
     * Returns the holder.
     *
     * @return the holder
     */
    protected fun getHolder(): ActionHolder {
        return holder!!
    }


    /**
     * Reads a characteristic with a fallback.
     *
     * @param key      key
     * @param fallback fallback
     * @param <T>      key type
     * @return value or fallback
    </T> */
    protected fun <T> readCharacteristic(
        key: CameraCharacteristics.Key<T?>,
        fallback: T
    ): T {
        val value = holder!!.getCharacteristics(this).get<T?>(key)
        return value ?: fallback
    }

    override fun addCallback(callback: ActionCallback) {
        if (!callbacks.contains(callback)) {
            callbacks.add(callback)
            callback.onActionStateChanged(this, state)
        }
    }

    override fun removeCallback(callback: ActionCallback) {
        callbacks.remove(callback)
    }

    open fun changeState(newState: Int) {
        state = newState
    }
}
