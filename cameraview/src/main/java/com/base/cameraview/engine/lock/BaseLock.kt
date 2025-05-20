package com.base.cameraview.engine.lock;

import androidx.annotation.NonNull;

import com.base.cameraview.engine.action.ActionHolder;
import com.base.cameraview.engine.action.BaseAction;


public abstract class BaseLock extends BaseAction {

    @Override
    public final void onStart(@NonNull ActionHolder holder) {
        super.onStart(holder);
        boolean isSkipped = checkShouldSkip(holder);
        boolean isSupported = checkIsSupported(holder);
        if (isSupported && !isSkipped) {
            onStarted(holder);
        } else {
            setState(STATE_COMPLETED);
        }
    }

    protected abstract void onStarted(@NonNull ActionHolder holder);

    protected abstract boolean checkShouldSkip(@NonNull ActionHolder holder);

    protected abstract boolean checkIsSupported(@NonNull ActionHolder holder);
}
