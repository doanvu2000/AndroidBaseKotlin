package com.base.cameraview.engine.meter

import android.hardware.camera2.params.MeteringRectangle
import com.base.cameraview.CameraLogger
import com.base.cameraview.engine.CameraEngine
import com.base.cameraview.engine.action.ActionHolder
import com.base.cameraview.engine.action.ActionWrapper
import com.base.cameraview.engine.action.Actions.together
import com.base.cameraview.engine.action.BaseAction
import com.base.cameraview.engine.metering.Camera2MeteringTransform
import com.base.cameraview.engine.offset.Reference
import com.base.cameraview.metering.MeteringRegions
import com.base.cameraview.metering.MeteringTransform

class MeterAction(
    private val engine: CameraEngine,
    private val regions: MeteringRegions?,
    private val skipIfPossible: Boolean,
) : ActionWrapper() {
    private var meters: MutableList<BaseMeter>? = null

    val isSuccessful: Boolean
        get() {
            for (meter in meters!!) {
                if (!meter.isSuccessful) {
                    LOG.i("isSuccessful:", "returning false.")
                    return false
                }
            }
            LOG.i("isSuccessful:", "returning true.")
            return true
        }
    override lateinit var action: BaseAction

    override fun onStart(holder: ActionHolder) {
        LOG.w("onStart:", "initializing.")
        initialize(holder)
        LOG.w("onStart:", "initialized.")
        super.onStart(holder)
    }

    private fun initialize(holder: ActionHolder) {
        var areas: MutableList<MeteringRectangle?> = ArrayList()
        if (regions != null) {
            val transform: MeteringTransform<MeteringRectangle?> = Camera2MeteringTransform(
                engine.getAngles(),
                engine.getPreview()!!.surfaceSize,
                engine.getPreviewStreamSize(Reference.VIEW)!!,
                engine.getPreview()!!.isCropping,
                holder.getCharacteristics(this),
                holder.getBuilder(this)
            )
            val transformed = regions.transform(transform)
            areas = transformed.get<MeteringRectangle?>(Int.Companion.MAX_VALUE, transform)
        }

        val ae: BaseMeter = ExposureMeter(areas, skipIfPossible)
        val af: BaseMeter = FocusMeter(areas, skipIfPossible)
        val awb: BaseMeter = WhiteBalanceMeter(areas, skipIfPossible)
        meters = mutableListOf(ae, af, awb)
        action = together(ae, af, awb)
    }

    companion object {
        private val TAG: String = MeterAction::class.java.simpleName
        private val LOG: CameraLogger = CameraLogger.create(TAG)
    }
}
