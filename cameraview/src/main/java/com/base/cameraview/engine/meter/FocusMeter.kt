package com.base.cameraview.engine.meter

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import android.hardware.camera2.params.MeteringRectangle
import com.base.cameraview.CameraLogger
import com.base.cameraview.engine.action.Action
import com.base.cameraview.engine.action.ActionHolder
import kotlin.math.min

class FocusMeter(areas: MutableList<MeteringRectangle?>, skipIfPossible: Boolean) :
    BaseMeter(areas, skipIfPossible) {
    override fun checkIsSupported(holder: ActionHolder): Boolean {
        // Exclude OFF and ED OF as per docs. These do no support the trigger.
        val afMode = holder.getBuilder(this).get(CaptureRequest.CONTROL_AF_MODE)
        val result = afMode != null &&
                (afMode == CameraCharacteristics.CONTROL_AF_MODE_AUTO || afMode == CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_PICTURE || afMode == CameraCharacteristics.CONTROL_AF_MODE_CONTINUOUS_VIDEO || afMode == CameraCharacteristics.CONTROL_AF_MODE_MACRO)
        LOG.i("checkIsSupported:", result)
        return result
    }

    override fun checkShouldSkip(holder: ActionHolder): Boolean {
        val lastResult: CaptureResult? = holder.getLastResult(this)
        if (lastResult != null) {
            val afState = lastResult.get(CaptureResult.CONTROL_AF_STATE)
            val result = afState != null &&
                    (afState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED ||
                            afState == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED)
            LOG.i("checkShouldSkip:", result)
            return result
        } else {
            LOG.i("checkShouldSkip: false - lastResult is null.")
            return false
        }
    }

    override fun onStarted(holder: ActionHolder, areas: MutableList<MeteringRectangle?>) {
        LOG.i("onStarted:", "with areas:", areas)
        holder.getBuilder(this).set(
            CaptureRequest.CONTROL_AF_TRIGGER,
            CaptureRequest.CONTROL_AF_TRIGGER_START
        )
        val maxRegions: Int = readCharacteristic<Int?>(
            CameraCharacteristics.CONTROL_MAX_REGIONS_AF,
            0
        )!!
        if (!areas.isEmpty() && maxRegions > 0) {
            val max = min(maxRegions, areas.size)
            holder.getBuilder(this).set(
                CaptureRequest.CONTROL_AF_REGIONS,
                areas.subList(0, max).toTypedArray()
            )
        }
        holder.applyBuilder(this)
    }

    override fun onCompleted(holder: ActionHolder) {
        super.onCompleted(holder)
        // Remove (but not apply) the risky parameter so it is not included in new requests.
        // Documentation about this key says that this should be allowed.
        holder.getBuilder(this).set(CaptureRequest.CONTROL_AF_TRIGGER, null)
    }

    override fun onCaptureCompleted(
        holder: ActionHolder, request: CaptureRequest,
        result: TotalCaptureResult
    ) {
        super.onCaptureCompleted(holder, request, result)
        val afState = result.get(CaptureResult.CONTROL_AF_STATE)
        LOG.i("onCaptureCompleted:", "afState:", afState)
        if (afState == null) return
        when (afState) {
            CaptureRequest.CONTROL_AF_STATE_FOCUSED_LOCKED -> {
                isSuccessful = true
                state = Action.Companion.STATE_COMPLETED
            }

            CaptureRequest.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED -> {
                isSuccessful = false
                state = Action.Companion.STATE_COMPLETED
            }

            CaptureRequest.CONTROL_AF_STATE_INACTIVE -> {}
            CaptureRequest.CONTROL_AF_STATE_ACTIVE_SCAN -> {}
            else -> {}
        }
    }

    companion object {
        private val TAG: String = FocusMeter::class.java.simpleName
        private val LOG: CameraLogger = CameraLogger.create(TAG)
    }
}
