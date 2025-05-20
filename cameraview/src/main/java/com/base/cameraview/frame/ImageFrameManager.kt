package com.base.cameraview.frame

import android.media.Image

class ImageFrameManager(poolSize: Int) : FrameManager<Image?>(
    poolSize,
    Image::class.java as Class<Image?>
) {
    override fun onFrameDataReleased(data: Image?, recycled: Boolean) {
        try {
            data?.close()
        } catch (ignore: Exception) {
        }
    }

    override fun onCloneFrameData(data: Image?): Image {
        throw RuntimeException(
            "Cannot freeze() an Image Frame. " +
                    "Please consider using the frame synchronously in your process() method, " +
                    "which also gives better performance."
        )
    }
}
