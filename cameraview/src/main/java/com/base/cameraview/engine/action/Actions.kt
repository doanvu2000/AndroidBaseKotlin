package com.base.cameraview.engine.action

/**
 * Utilities for creating [Action] sequences.
 */
object Actions {
    /**
     * Creates a [BaseAction] that executes all the child actions
     * together, at the same time, and completes once all of them are
     * completed.
     *
     * @param actions input actions
     * @return a new action
     */
    @JvmStatic
    fun together(vararg actions: BaseAction): BaseAction {
        return TogetherAction(mutableListOf(*actions))
    }

    /**
     * Creates a [BaseAction] that executes all the child actions
     * in sequence, waiting for the first to complete, then going on with
     * the second and so on, finally completing when all are completed.
     *
     * @param actions input actions
     * @return a new action
     */
    @JvmStatic
    fun sequence(vararg actions: BaseAction): BaseAction {
        return SequenceAction(mutableListOf(*actions))
    }

    /**
     * Creates a [BaseAction] that completes as normal, but is also
     * forced to complete if the given timeout is reached, by calling
     * [Action.abort].
     *
     * @param timeoutMillis timeout in milliseconds
     * @param action        action
     * @return a new action
     */
    @JvmStatic
    fun timeout(timeoutMillis: Long, action: BaseAction): BaseAction {
        return TimeoutAction(timeoutMillis, action)
    }
}
