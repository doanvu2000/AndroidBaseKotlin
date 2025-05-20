package com.base.cameraview.engine.action

/**
 * completed state. Handy as an inner anonymous class.
 */
abstract class CompletionCallback : ActionCallback {
    override fun onActionStateChanged(action: Action, state: Int) {
        if (state == Action.STATE_COMPLETED) {
            onActionCompleted(action)
        }
    }

    /**
     * The given action has just reached the completed state.
     *
     * @param action action
     */
    protected abstract fun onActionCompleted(action: Action)
}
