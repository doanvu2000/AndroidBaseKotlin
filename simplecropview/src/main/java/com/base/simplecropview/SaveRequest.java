package com.base.simplecropview;

import android.graphics.Bitmap;

public class SaveRequest {

    private CropImageView cropImageView;
    private Bitmap image;
    private Bitmap.CompressFormat compressFormat;
    private int compressQuality = -1;

    public SaveRequest(CropImageView cropImageView, Bitmap image) {
        this.cropImageView = cropImageView;
        this.image = image;
    }

    public SaveRequest compressFormat(Bitmap.CompressFormat compressFormat) {
        this.compressFormat = compressFormat;
        return this;
    }

    public SaveRequest compressQuality(int compressQuality) {
        this.compressQuality = compressQuality;
        return this;
    }

    private void build() {
        if (compressFormat != null) {
            cropImageView.setCompressFormat(compressFormat);
        }
        if (compressQuality >= 0) {
            cropImageView.setCompressQuality(compressQuality);
        }
    }
}
