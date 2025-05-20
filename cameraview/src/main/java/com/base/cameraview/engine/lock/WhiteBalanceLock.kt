package com.base.cameraview.engine.lock;

import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;

import androidx.annotation.NonNull;

import com.base.cameraview.CameraLogger;
import com.base.cameraview.engine.action.ActionHolder;

public class WhiteBalanceLock extends BaseLock {

    private final static String TAG = WhiteBalanceLock.class.getSimpleName();
    private final static CameraLogger LOG = CameraLogger.create(TAG);

    @Override
    protected boolean checkIsSupported(@NonNull ActionHolder holder) {
        boolean isNotLegacy = readCharacteristic(
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL, -1)
                != CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY;
        Integer awbMode = holder.getBuilder(this).get(CaptureRequest.CONTROL_AWB_MODE);
        boolean result = isNotLegacy
                && awbMode != null
                && awbMode == CaptureRequest.CONTROL_AWB_MODE_AUTO;
        LOG.i("checkIsSupported:", result);
        return result;
    }

    @Override
    protected boolean checkShouldSkip(@NonNull ActionHolder holder) {
        CaptureResult lastResult = holder.getLastResult(this);
        if (lastResult != null) {
            Integer awbState = lastResult.get(CaptureResult.CONTROL_AWB_STATE);
            boolean result = awbState != null
                    && awbState == CaptureRequest.CONTROL_AWB_STATE_LOCKED;
            LOG.i("checkShouldSkip:", result);
            return result;
        } else {
            LOG.i("checkShouldSkip: false - lastResult is null.");
            return false;
        }
    }

    @Override
    protected void onStarted(@NonNull ActionHolder holder) {
        holder.getBuilder(this).set(CaptureRequest.CONTROL_AWB_LOCK, true);
        holder.applyBuilder(this);
    }

    @Override
    public void onCaptureCompleted(@NonNull ActionHolder holder,
                                   @NonNull CaptureRequest request,
                                   @NonNull TotalCaptureResult result) {
        super.onCaptureCompleted(holder, request, result);
        Integer awbState = result.get(CaptureResult.CONTROL_AWB_STATE);
        LOG.i("processCapture:", "awbState:", awbState);
        if (awbState == null) return;
        switch (awbState) {
            case CaptureRequest.CONTROL_AWB_STATE_LOCKED: {
                setState(STATE_COMPLETED);
                break;
            }
            case CaptureRequest.CONTROL_AWB_STATE_CONVERGED:
            case CaptureRequest.CONTROL_AWB_STATE_INACTIVE:
            case CaptureRequest.CONTROL_AWB_STATE_SEARCHING: {
                // Wait...
                break;
            }
        }
    }
}
