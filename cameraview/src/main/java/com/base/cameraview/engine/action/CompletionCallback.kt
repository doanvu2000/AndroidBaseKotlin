package com.base.cameraview.engine.action;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * completed state. Handy as an inner anonymous class.
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public abstract class CompletionCallback implements ActionCallback {

    @Override
    public final void onActionStateChanged(@NonNull Action action, int state) {
        if (state == Action.STATE_COMPLETED) {
            onActionCompleted(action);
        }
    }

    /**
     * The given action has just reached the completed state.
     *
     * @param action action
     */
    protected abstract void onActionCompleted(@NonNull Action action);
}
