package com.base.cameraview.engine.meter

import android.hardware.camera2.params.MeteringRectangle
import com.base.cameraview.CameraLogger
import com.base.cameraview.engine.action.Action
import com.base.cameraview.engine.action.ActionHolder
import com.base.cameraview.engine.action.BaseAction

abstract class BaseMeter protected constructor(
    private val areas: MutableList<MeteringRectangle?>,
    private val skipIfPossible: Boolean
) : BaseAction() {
    var isSuccessful: Boolean = false
        protected set

    override fun onStart(holder: ActionHolder) {
        super.onStart(holder)
        val isSkipped = skipIfPossible && checkShouldSkip(holder)
        val isSupported = checkIsSupported(holder)
        if (isSupported && !isSkipped) {
            LOG.i("onStart:", "supported and not skipped. Dispatching onStarted.")
            onStarted(holder, areas)
        } else {
            LOG.i("onStart:", "not supported or skipped. Dispatching COMPLETED state.")
            this.isSuccessful = true
            state = Action.Companion.STATE_COMPLETED
        }
    }

    protected abstract fun onStarted(
        holder: ActionHolder,
        areas: MutableList<MeteringRectangle?>
    )

    protected abstract fun checkShouldSkip(holder: ActionHolder): Boolean

    protected abstract fun checkIsSupported(holder: ActionHolder): Boolean

    companion object {
        private val TAG: String = BaseMeter::class.java.simpleName
        private val LOG: CameraLogger = CameraLogger.create(TAG)
    }
}
