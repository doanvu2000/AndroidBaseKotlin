package com.base.cameraview.engine.lock

import com.base.cameraview.engine.action.ActionWrapper
import com.base.cameraview.engine.action.Actions.together
import com.base.cameraview.engine.action.BaseAction

class LockAction : ActionWrapper() {
    override val action: BaseAction = together(
        ExposureLock(),
        FocusLock(),
        WhiteBalanceLock()
    )
}
