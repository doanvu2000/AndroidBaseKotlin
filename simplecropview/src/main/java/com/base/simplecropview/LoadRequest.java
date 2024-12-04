package com.base.simplecropview;

import android.graphics.RectF;
import android.net.Uri;

public class LoadRequest {

    private float initialFrameScale;
    private RectF initialFrameRect;
    private boolean useThumbnail;
    private CropImageView cropImageView;
    private Uri sourceUri;

    public LoadRequest(CropImageView cropImageView, Uri sourceUri) {
        this.cropImageView = cropImageView;
        this.sourceUri = sourceUri;
    }

    public LoadRequest initialFrameScale(float initialFrameScale) {
        this.initialFrameScale = initialFrameScale;
        return this;
    }

    public LoadRequest initialFrameRect(RectF initialFrameRect) {
        this.initialFrameRect = initialFrameRect;
        return this;
    }

    public LoadRequest useThumbnail(boolean useThumbnail) {
        this.useThumbnail = useThumbnail;
        return this;
    }
}
