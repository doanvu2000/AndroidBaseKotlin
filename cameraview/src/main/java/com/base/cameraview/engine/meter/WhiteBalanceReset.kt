package com.base.cameraview.engine.meter

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.MeteringRectangle
import com.base.cameraview.CameraLogger
import com.base.cameraview.engine.action.Action
import com.base.cameraview.engine.action.ActionHolder

class WhiteBalanceReset : BaseReset(true) {
    override fun onStarted(holder: ActionHolder, area: MeteringRectangle?) {
        LOG.w("onStarted:", "with area:", area)
        val maxRegions: Int = readCharacteristic<Int?>(
            CameraCharacteristics.CONTROL_MAX_REGIONS_AWB,
            0
        )!!
        if (area != null && maxRegions > 0) {
            holder.getBuilder(this).set(
                CaptureRequest.CONTROL_AWB_REGIONS,
                arrayOf(area)
            )
            holder.applyBuilder(this)
        }
        state = Action.Companion.STATE_COMPLETED
    }

    companion object {
        private val TAG: String = WhiteBalanceReset::class.java.simpleName
        private val LOG: CameraLogger = CameraLogger.create(TAG)
    }
}
