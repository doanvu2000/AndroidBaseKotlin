package com.base.cameraview.filter;

import androidx.annotation.NonNull;


/**
 * A {@link Filter} that draws frames without any modification.
 */
public final class NoFilter extends BaseFilter {

    @NonNull
    @Override
    public String getFragmentShader() {
        return createDefaultFragmentShader();
    }
}
