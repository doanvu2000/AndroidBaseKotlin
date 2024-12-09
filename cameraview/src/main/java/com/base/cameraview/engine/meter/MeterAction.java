package com.base.cameraview.engine.meter;

import android.hardware.camera2.params.MeteringRectangle;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.base.cameraview.CameraLogger;
import com.base.cameraview.engine.CameraEngine;
import com.base.cameraview.engine.action.ActionHolder;
import com.base.cameraview.engine.action.ActionWrapper;
import com.base.cameraview.engine.action.Actions;
import com.base.cameraview.engine.action.BaseAction;
import com.base.cameraview.engine.metering.Camera2MeteringTransform;
import com.base.cameraview.engine.offset.Reference;
import com.base.cameraview.metering.MeteringRegions;
import com.base.cameraview.metering.MeteringTransform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
public class MeterAction extends ActionWrapper {

    private final static String TAG = MeterAction.class.getSimpleName();
    private final static CameraLogger LOG = CameraLogger.create(TAG);
    private final MeteringRegions regions;
    private final CameraEngine engine;
    private final boolean skipIfPossible;
    private List<BaseMeter> meters;
    private BaseAction action;

    public MeterAction(@NonNull CameraEngine engine,
                       @Nullable MeteringRegions regions,
                       boolean skipIfPossible) {
        this.regions = regions;
        this.engine = engine;
        this.skipIfPossible = skipIfPossible;
    }

    @NonNull
    @Override
    public BaseAction getAction() {
        return action;
    }

    public boolean isSuccessful() {
        for (BaseMeter meter : meters) {
            if (!meter.isSuccessful()) {
                LOG.i("isSuccessful:", "returning false.");
                return false;
            }
        }
        LOG.i("isSuccessful:", "returning true.");
        return true;
    }

    @Override
    protected void onStart(@NonNull ActionHolder holder) {
        LOG.w("onStart:", "initializing.");
        initialize(holder);
        LOG.w("onStart:", "initialized.");
        super.onStart(holder);
    }

    private void initialize(@NonNull ActionHolder holder) {
        List<MeteringRectangle> areas = new ArrayList<>();
        if (regions != null) {
            MeteringTransform<MeteringRectangle> transform = new Camera2MeteringTransform(
                    engine.getAngles(),
                    engine.getPreview().getSurfaceSize(),
                    engine.getPreviewStreamSize(Reference.VIEW),
                    engine.getPreview().isCropping(),
                    holder.getCharacteristics(this),
                    holder.getBuilder(this)
            );
            MeteringRegions transformed = regions.transform(transform);
            areas = transformed.get(Integer.MAX_VALUE, transform);
        }

        BaseMeter ae = new ExposureMeter(areas, skipIfPossible);
        BaseMeter af = new FocusMeter(areas, skipIfPossible);
        BaseMeter awb = new WhiteBalanceMeter(areas, skipIfPossible);
        meters = Arrays.asList(ae, af, awb);
        action = Actions.together(ae, af, awb);
    }
}
