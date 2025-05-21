package com.base.cameraview.engine.meter

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.params.MeteringRectangle
import com.base.cameraview.CameraLogger
import com.base.cameraview.engine.action.Action
import com.base.cameraview.engine.action.ActionHolder

class FocusReset : BaseReset(true) {
    override fun onStarted(holder: ActionHolder, area: MeteringRectangle?) {
        var changed = false
        val maxRegions: Int = readCharacteristic<Int?>(
            CameraCharacteristics.CONTROL_MAX_REGIONS_AF,
            0
        )!!
        if (area != null && maxRegions > 0) {
            holder.getBuilder(this).set(
                CaptureRequest.CONTROL_AF_REGIONS,
                arrayOf(area)
            )
            changed = true
        }

        // NOTE: trigger might not be supported, in which case I think it will be ignored.
        val lastResult: CaptureResult? = holder.getLastResult(this)
        val trigger = lastResult?.get(CaptureResult.CONTROL_AF_TRIGGER)
        LOG.w("onStarted:", "last focus trigger is $trigger")
        if (trigger != null && trigger == CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START) {
            holder.getBuilder(this).set(
                CaptureRequest.CONTROL_AF_TRIGGER,
                CaptureRequest.CONTROL_AF_TRIGGER_CANCEL
            )
            changed = true
        }

        if (changed) holder.applyBuilder(this)
        state = Action.Companion.STATE_COMPLETED
    }

    companion object {
        private val TAG: String = FocusReset::class.java.simpleName
        private val LOG: CameraLogger = CameraLogger.create(TAG)
    }
}
