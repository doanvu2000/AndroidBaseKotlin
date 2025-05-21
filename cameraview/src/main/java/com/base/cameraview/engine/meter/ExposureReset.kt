package com.base.cameraview.engine.meter

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.MeteringRectangle
import com.base.cameraview.CameraLogger
import com.base.cameraview.engine.action.Action
import com.base.cameraview.engine.action.ActionHolder

class ExposureReset : BaseReset(true) {
    override fun onStarted(holder: ActionHolder, area: MeteringRectangle?) {
        val maxRegions: Int = readCharacteristic<Int?>(
            CameraCharacteristics.CONTROL_MAX_REGIONS_AE,
            0
        )!!
        if (area != null && maxRegions > 0) {
            holder.getBuilder(this).set(
                CaptureRequest.CONTROL_AE_REGIONS,
                arrayOf(area)
            )
        }

        // NOTE: precapture might not be supported, in which case I think it will be ignored.
        val lastResult: CaptureResult? = holder.getLastResult(this)
        val trigger = lastResult?.get(CaptureResult.CONTROL_AE_PRECAPTURE_TRIGGER)
        LOG.i("onStarted:", "last precapture trigger is $trigger")
        if (trigger != null && trigger == CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START) {
            LOG.i("onStarted:", "canceling precapture.")
            val newTrigger = CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_CANCEL
            holder.getBuilder(this).set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                newTrigger
            )
        }

        // Documentation about CONTROL_AE_PRECAPTURE_TRIGGER says that, if it was started but not
        // followed by a CAPTURE_INTENT_STILL_PICTURE request, the internal AE routine might remain
        // locked unless we unlock manually.
        // This is often the case for us, since the snapshot picture recorder does not use the
        // intent and anyway we use the precapture sequence for touch metering as well.
        // To reset docs suggest the use of CONTROL_AE_PRECAPTURE_TRIGGER_CANCEL, which we do above,
        // or the technique used below: locking then unlocking. This proved to be the ONLY method
        // to unlock reliably, unlike the cancel trigger (which we'll run anyway).
        holder.getBuilder(this).set(CaptureRequest.CONTROL_AE_LOCK, true)
        holder.applyBuilder(this)
        state = STATE_WAITING_LOCK
    }

    override fun onCaptureCompleted(
        holder: ActionHolder, request: CaptureRequest,
        result: TotalCaptureResult
    ) {
        super.onCaptureCompleted(holder, request, result)
        if (state == STATE_WAITING_LOCK) {
            holder.getBuilder(this).set(CaptureRequest.CONTROL_AE_LOCK, false)
            holder.applyBuilder(this)
            changeState(Action.Companion.STATE_COMPLETED)
        }
    }

    companion object {
        private val TAG: String = ExposureReset::class.java.simpleName
        private val LOG: CameraLogger = CameraLogger.create(TAG)

        private const val STATE_WAITING_LOCK = 0
    }
}
