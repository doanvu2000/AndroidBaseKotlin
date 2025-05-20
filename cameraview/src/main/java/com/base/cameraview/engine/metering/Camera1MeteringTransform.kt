package com.base.cameraview.engine.metering;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;

import androidx.annotation.NonNull;

import com.base.cameraview.CameraLogger;
import com.base.cameraview.engine.offset.Angles;
import com.base.cameraview.engine.offset.Axis;
import com.base.cameraview.engine.offset.Reference;
import com.base.cameraview.metering.MeteringTransform;
import com.base.cameraview.size.Size;


public class Camera1MeteringTransform implements MeteringTransform<Camera.Area> {

    protected static final String TAG = Camera1MeteringTransform.class.getSimpleName();
    protected static final CameraLogger LOG = CameraLogger.create(TAG);

    private final int displayToSensor;
    private final Size previewSize;

    public Camera1MeteringTransform(@NonNull Angles angles, @NonNull Size previewSize) {
        this.displayToSensor = -angles.offset(Reference.SENSOR, Reference.VIEW, Axis.ABSOLUTE);
        this.previewSize = previewSize;
    }

    @NonNull
    @Override
    public PointF transformMeteringPoint(@NonNull PointF point) {
        // First, rescale to the -1000 ... 1000 range.
        PointF scaled = new PointF();
        scaled.x = -1000F + (point.x / previewSize.getWidth()) * 2000F;
        scaled.y = -1000F + (point.y / previewSize.getHeight()) * 2000F;

        // Apply rotation to this point.
        // https://academo.org/demos/rotation-about-point/
        PointF rotated = new PointF();
        double theta = ((double) displayToSensor) * Math.PI / 180;
        rotated.x = (float) (scaled.x * Math.cos(theta) - scaled.y * Math.sin(theta));
        rotated.y = (float) (scaled.x * Math.sin(theta) + scaled.y * Math.cos(theta));
        LOG.i("scaled:", scaled, "rotated:", rotated);
        return rotated;
    }

    @NonNull
    @Override
    public Camera.Area transformMeteringRegion(@NonNull RectF region, int weight) {
        Rect rect = new Rect();
        region.round(rect);
        return new Camera.Area(rect, weight);
    }
}
