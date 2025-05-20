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

class WhiteBalanceMeter(areas: MutableList<MeteringRectangle?>, skipIfPossible: Boolean) :
    BaseMeter(areas, skipIfPossible) {
    override fun checkIsSupported(holder: ActionHolder): Boolean {
        val isNotLegacy = (readCharacteristic<Int?>(
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL, -1
        )
                != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY)
        val awbMode = holder.getBuilder(this).get(CaptureRequest.CONTROL_AWB_MODE)
        val result = isNotLegacy
                && awbMode != null && awbMode == CaptureRequest.CONTROL_AWB_MODE_AUTO
        LOG.i("checkIsSupported:", result)
        return result
    }

    override fun checkShouldSkip(holder: ActionHolder): Boolean {
        val lastResult: CaptureResult? = holder.getLastResult(this)
        if (lastResult != null) {
            val awbState = lastResult.get(CaptureResult.CONTROL_AWB_STATE)
            val result = awbState != null
                    && awbState == CaptureRequest.CONTROL_AWB_STATE_CONVERGED
            LOG.i("checkShouldSkip:", result)
            return result
        } else {
            LOG.i("checkShouldSkip: false - lastResult is null.")
            return false
        }
    }

    override fun onStarted(holder: ActionHolder, areas: MutableList<MeteringRectangle?>) {
        LOG.i("onStarted:", "with areas:", areas)
        val maxRegions: Int = readCharacteristic<Int?>(
            CameraCharacteristics.CONTROL_MAX_REGIONS_AWB,
            0
        )!!
        if (!areas.isEmpty() && maxRegions > 0) {
            val max = min(maxRegions, areas.size)
            holder.getBuilder(this).set(
                CaptureRequest.CONTROL_AWB_REGIONS,
                areas.subList(0, max).toTypedArray()
            )
            holder.applyBuilder(this)
        }
    }

    override fun onCaptureCompleted(
        holder: ActionHolder,
        request: CaptureRequest,
        result: TotalCaptureResult
    ) {
        super.onCaptureCompleted(holder, request, result)
        val awbState = result.get(CaptureResult.CONTROL_AWB_STATE)
        LOG.i("onCaptureCompleted:", "awbState:", awbState)
        if (awbState == null) return

        when (awbState) {
            CaptureRequest.CONTROL_AWB_STATE_CONVERGED -> {
                isSuccessful = true
                state = Action.Companion.STATE_COMPLETED
            }

            CaptureRequest.CONTROL_AWB_STATE_LOCKED -> {
                // Nothing we can do if AWB was locked.
                isSuccessful = false
                state = Action.Companion.STATE_COMPLETED
            }

            CaptureRequest.CONTROL_AWB_STATE_INACTIVE, CaptureRequest.CONTROL_AWB_STATE_SEARCHING -> {}
        }
    }

    companion object {
        private val TAG: String = WhiteBalanceMeter::class.java.simpleName
        private val LOG: CameraLogger = CameraLogger.create(TAG)
    }
}
