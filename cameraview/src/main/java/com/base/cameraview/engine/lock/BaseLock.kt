package com.base.cameraview.engine.lock

import com.base.cameraview.engine.action.Action
import com.base.cameraview.engine.action.ActionHolder
import com.base.cameraview.engine.action.BaseAction

abstract class BaseLock : BaseAction() {
    override fun onStart(holder: ActionHolder) {
        super.onStart(holder)
        val isSkipped = checkShouldSkip(holder)
        val isSupported = checkIsSupported(holder)
        if (isSupported && !isSkipped) {
            onStarted(holder)
        } else {
            state = Action.Companion.STATE_COMPLETED
        }
    }

    protected abstract fun onStarted(holder: ActionHolder)

    protected abstract fun checkShouldSkip(holder: ActionHolder): Boolean

    protected abstract fun checkIsSupported(holder: ActionHolder): Boolean
}
