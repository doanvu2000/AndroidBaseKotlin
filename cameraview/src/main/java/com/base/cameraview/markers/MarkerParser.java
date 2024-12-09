package com.base.cameraview.markers;

import android.content.res.TypedArray;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.base.cameraview.R;

/**
 * Parses markers from XML attributes.
 */
public class MarkerParser {

    private AutoFocusMarker autoFocusMarker = null;

    public MarkerParser(@NonNull TypedArray array) {
        String autoFocusName = array.getString(R.styleable.CameraView_cameraAutoFocusMarker);
        if (autoFocusName != null) {
            try {
                Class<?> autoFocusClass = Class.forName(autoFocusName);
                autoFocusMarker = (AutoFocusMarker) autoFocusClass.newInstance();
            } catch (Exception ignore) {
            }
        }
    }

    @Nullable
    public AutoFocusMarker getAutoFocusMarker() {
        return autoFocusMarker;
    }
}
