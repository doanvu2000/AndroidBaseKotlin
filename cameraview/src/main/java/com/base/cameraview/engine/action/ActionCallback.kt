package com.base.cameraview.engine.action

/**
 * A callback for [Action] state changes.
 * See the action class.
 *
 *
 * See also [CompletionCallback].
 */
interface ActionCallback {
    /**
     * Action state has just changed.
     *
     * @param action action
     * @param state  new state
     */
    fun onActionStateChanged(action: Action, state: Int)
}
