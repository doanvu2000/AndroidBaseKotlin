package com.base.simplecropview;

import android.net.Uri;

public class CropRequest {

    private CropImageView cropImageView;
    private Uri sourceUri;
    private int outputWidth;
    private int outputHeight;
    private int outputMaxWidth;
    private int outputMaxHeight;

    public CropRequest(CropImageView cropImageView, Uri sourceUri) {
        this.cropImageView = cropImageView;
        this.sourceUri = sourceUri;
    }

    public CropRequest outputWidth(int outputWidth) {
        this.outputWidth = outputWidth;
        this.outputHeight = 0;
        return this;
    }

    public CropRequest outputHeight(int outputHeight) {
        this.outputHeight = outputHeight;
        this.outputWidth = 0;
        return this;
    }

    public CropRequest outputMaxWidth(int outputMaxWidth) {
        this.outputMaxWidth = outputMaxWidth;
        return this;
    }

    public CropRequest outputMaxHeight(int outputMaxHeight) {
        this.outputMaxHeight = outputMaxHeight;
        return this;
    }

    private void build() {
        if (outputWidth > 0) cropImageView.setOutputWidth(outputWidth);
        if (outputHeight > 0) cropImageView.setOutputHeight(outputHeight);
        cropImageView.setOutputMaxSize(outputMaxWidth, outputMaxHeight);
    }
}
