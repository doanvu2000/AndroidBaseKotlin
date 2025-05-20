package com.base.cameraview.engine.lock;

import androidx.annotation.NonNull;

import com.base.cameraview.engine.action.ActionWrapper;
import com.base.cameraview.engine.action.Actions;
import com.base.cameraview.engine.action.BaseAction;

public class LockAction extends ActionWrapper {

    private final BaseAction action = Actions.together(
            new ExposureLock(),
            new FocusLock(),
            new WhiteBalanceLock()
    );

    @NonNull
    @Override
    public BaseAction getAction() {
        return action;
    }
}
