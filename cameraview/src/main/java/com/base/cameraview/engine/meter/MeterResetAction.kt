package com.base.cameraview.engine.meter

import com.base.cameraview.engine.action.ActionWrapper
import com.base.cameraview.engine.action.Actions.together
import com.base.cameraview.engine.action.BaseAction

class MeterResetAction : ActionWrapper() {
    override val action: BaseAction
        get() = together(
            ExposureReset(), FocusReset(), WhiteBalanceReset()
        )
}
