package com.base.cameraview.engine.meter

import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.params.MeteringRectangle
import com.base.cameraview.engine.action.ActionHolder
import com.base.cameraview.engine.action.BaseAction

abstract class BaseReset protected constructor(private val resetArea: Boolean) : BaseAction() {
    override fun onStart(holder: ActionHolder) {
        super.onStart(holder)
        var area: MeteringRectangle? = null
        if (resetArea) {
            val rect = readCharacteristic<Rect?>(
                CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE,
                Rect()
            )
            area = MeteringRectangle(rect, MeteringRectangle.METERING_WEIGHT_DONT_CARE)
        }
        onStarted(holder, area)
    }

    protected abstract fun onStarted(
        holder: ActionHolder,
        area: MeteringRectangle?
    )
}
