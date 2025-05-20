package com.base.cameraview.engine.action;

import androidx.annotation.NonNull;

/**
 * A callback for {@link Action} state changes.
 * See the action class.
 * <p>
 * See also {@link CompletionCallback}.
 */
public interface ActionCallback {

    /**
     * Action state has just changed.
     *
     * @param action action
     * @param state  new state
     */
    void onActionStateChanged(@NonNull Action action, int state);
}
