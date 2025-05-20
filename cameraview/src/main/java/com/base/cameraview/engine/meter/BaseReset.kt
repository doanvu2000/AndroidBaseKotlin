package com.base.cameraview.engine.meter;

import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.MeteringRectangle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.base.cameraview.engine.action.ActionHolder;
import com.base.cameraview.engine.action.BaseAction;

public abstract class BaseReset extends BaseAction {

    private boolean resetArea;

    @SuppressWarnings("WeakerAccess")
    protected BaseReset(boolean resetArea) {
        this.resetArea = resetArea;
    }

    @Override
    public final void onStart(@NonNull ActionHolder holder) {
        super.onStart(holder);
        MeteringRectangle area = null;
        if (resetArea) {
            Rect rect = readCharacteristic(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE,
                    new Rect());
            area = new MeteringRectangle(rect, MeteringRectangle.METERING_WEIGHT_DONT_CARE);
        }
        onStarted(holder, area);
    }

    protected abstract void onStarted(@NonNull ActionHolder holder,
                                      @Nullable MeteringRectangle area);
}
