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

class ExposureMeter(areas: MutableList<MeteringRectangle?>, skipIfPossible: Boolean) :
    BaseMeter(areas, skipIfPossible) {
    private var mSupportsAreas = false
    private var mSupportsTrigger = false

    override fun checkIsSupported(holder: ActionHolder): Boolean {
        // In our case, this means checking if we support the AE precapture trigger.
        val isLegacy = (readCharacteristic<Int?>(
            CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL, -1
        )
                == CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY)
        val aeMode = holder.getBuilder(this).get(CaptureRequest.CONTROL_AE_MODE)
        val isAEOn = aeMode != null &&
                (aeMode == CameraCharacteristics.CONTROL_AE_MODE_ON || aeMode == CameraCharacteristics.CONTROL_AE_MODE_ON_ALWAYS_FLASH || aeMode == CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH || aeMode == CameraCharacteristics.CONTROL_AE_MODE_ON_AUTO_FLASH_REDEYE || aeMode == 5 /* CameraCharacteristics.CONTROL_AE_MODE_ON_EXTERNAL_FLASH, API 28 */
                        )
        mSupportsTrigger = !isLegacy
        mSupportsAreas = readCharacteristic<Int?>(
            CameraCharacteristics.CONTROL_MAX_REGIONS_AE,
            0
        )!! > 0
        val result = isAEOn && (mSupportsTrigger || mSupportsAreas)
        LOG.i(
            "checkIsSupported:", result,
            "trigger:", mSupportsTrigger,
            "areas:", mSupportsAreas
        )
        return result
    }

    override fun checkShouldSkip(holder: ActionHolder): Boolean {
        val lastResult: CaptureResult? = holder.getLastResult(this)
        if (lastResult != null) {
            val aeState = lastResult.get(CaptureResult.CONTROL_AE_STATE)
            val result = aeState != null && aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED
            LOG.i("checkShouldSkip:", result)
            return result
        } else {
            LOG.i("checkShouldSkip: false - lastResult is null.")
            return false
        }
    }

    override fun onStarted(holder: ActionHolder, areas: MutableList<MeteringRectangle?>) {
        LOG.i("onStarted:", "with areas:", areas)

        if (mSupportsAreas && !areas.isEmpty()) {
            var max: Int =
                readCharacteristic<Int?>(CameraCharacteristics.CONTROL_MAX_REGIONS_AE, 0)!!
            max = min(max, areas.size)
            holder.getBuilder(this).set(
                CaptureRequest.CONTROL_AE_REGIONS,
                areas.subList(0, max).toTypedArray<MeteringRectangle?>()
            )
        }

        if (mSupportsTrigger) {
            holder.getBuilder(this).set(
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START
            )
        }

        // Apply
        holder.applyBuilder(this)
        state = if (mSupportsTrigger) {
            STATE_WAITING_PRECAPTURE
        } else {
            STATE_WAITING_PRECAPTURE_END
        }
    }

    override fun onCompleted(holder: ActionHolder) {
        super.onCompleted(holder)
        // Remove (but not apply) the risky parameter so it is not included in new requests.
        // Documentation about this key says that this should be allowed.
        holder.getBuilder(this).set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, null)
    }

    override fun onCaptureCompleted(
        holder: ActionHolder, request: CaptureRequest,
        result: TotalCaptureResult
    ) {
        super.onCaptureCompleted(holder, request, result)
        val aeState = result.get(CaptureResult.CONTROL_AE_STATE)
        val aeTriggerState = result.get(CaptureResult.CONTROL_AE_PRECAPTURE_TRIGGER)
        LOG.i("onCaptureCompleted:", "aeState: $aeState, aeTriggerState: $aeTriggerState")
        if (aeState == null) return

        if (state == STATE_WAITING_PRECAPTURE) {
            when (aeState) {
                CaptureResult.CONTROL_AE_STATE_PRECAPTURE -> {
                    state = STATE_WAITING_PRECAPTURE_END
                }

                CaptureResult.CONTROL_AE_STATE_CONVERGED, CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED -> {
                    // PRECAPTURE is a transient state. Being here might mean that precapture run
                    // and was successful, OR that the trigger was not even received yet. To
                    // distinguish, check the trigger state.
                    if (aeTriggerState != null && (aeTriggerState
                                == CaptureResult.CONTROL_AE_PRECAPTURE_TRIGGER_START)
                    ) {
                        isSuccessful = true
                        state = Action.Companion.STATE_COMPLETED
                    }
                }

                CaptureResult.CONTROL_AE_STATE_LOCKED -> {
                    // There's nothing we can do, AE was locked, triggers are ignored.
                    isSuccessful = false
                    state = Action.Companion.STATE_COMPLETED
                }

                CaptureResult.CONTROL_AE_STATE_INACTIVE, CaptureResult.CONTROL_AE_STATE_SEARCHING -> {}
            }
        }

        if (state == STATE_WAITING_PRECAPTURE_END) {
            when (aeState) {
                CaptureResult.CONTROL_AE_STATE_CONVERGED, CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED -> {
                    isSuccessful = true
                    state = Action.Companion.STATE_COMPLETED
                }

                CaptureResult.CONTROL_AE_STATE_LOCKED -> {
                    // There's nothing we can do, AE was locked, triggers are ignored.
                    isSuccessful = false
                    state = Action.Companion.STATE_COMPLETED
                }

                CaptureResult.CONTROL_AE_STATE_PRECAPTURE, CaptureResult.CONTROL_AE_STATE_INACTIVE, CaptureResult.CONTROL_AE_STATE_SEARCHING -> {}
            }
        }
    }

    companion object {
        private val TAG: String = ExposureMeter::class.java.simpleName
        private val LOG: CameraLogger = CameraLogger.create(TAG)

        private const val STATE_WAITING_PRECAPTURE = 0
        private const val STATE_WAITING_PRECAPTURE_END = 1
    }
}
