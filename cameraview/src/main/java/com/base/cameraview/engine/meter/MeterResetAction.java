package com.base.cameraview.engine.meter;

import androidx.annotation.NonNull;

import com.base.cameraview.engine.action.ActionWrapper;
import com.base.cameraview.engine.action.Actions;
import com.base.cameraview.engine.action.BaseAction;


public class MeterResetAction extends ActionWrapper {

    private final BaseAction action;

    public MeterResetAction() {
        this.action = Actions.together(
                new ExposureReset(),
                new FocusReset(),
                new WhiteBalanceReset()
        );
    }

    @NonNull
    @Override
    public BaseAction getAction() {
        return action;
    }
}
