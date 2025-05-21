package com.base.cameraview.size

import android.content.res.TypedArray
import com.base.cameraview.R

/**
 * Parses size selectors from XML attributes.
 */
class SizeSelectorParser(array: TypedArray) {
    @JvmField
    val pictureSizeSelector: SizeSelector

    @JvmField
    val videoSizeSelector: SizeSelector

    init {
        val pictureConstraints: MutableList<SizeSelector?> = ArrayList(3)

        if (array.hasValue(R.styleable.CameraView_cameraPictureSizeMinWidth)) {
            pictureConstraints.add(
                SizeSelectors.minWidth(
                    array.getInteger(R.styleable.CameraView_cameraPictureSizeMinWidth, 0)
                )
            )
        }

        if (array.hasValue(R.styleable.CameraView_cameraPictureSizeMaxWidth)) {
            pictureConstraints.add(
                SizeSelectors.maxWidth(
                    array.getInteger(R.styleable.CameraView_cameraPictureSizeMaxWidth, 0)
                )
            )
        }

        if (array.hasValue(R.styleable.CameraView_cameraPictureSizeMinHeight)) {
            pictureConstraints.add(
                SizeSelectors.minHeight(
                    array.getInteger(R.styleable.CameraView_cameraPictureSizeMinHeight, 0)
                )
            )
        }

        if (array.hasValue(R.styleable.CameraView_cameraPictureSizeMaxHeight)) {
            pictureConstraints.add(
                SizeSelectors.maxHeight(
                    array.getInteger(R.styleable.CameraView_cameraPictureSizeMaxHeight, 0)
                )
            )
        }

        if (array.hasValue(R.styleable.CameraView_cameraPictureSizeMinArea)) {
            pictureConstraints.add(
                SizeSelectors.minArea(
                    array.getInteger(R.styleable.CameraView_cameraPictureSizeMinArea, 0)
                )
            )
        }

        if (array.hasValue(R.styleable.CameraView_cameraPictureSizeMaxArea)) {
            pictureConstraints.add(
                SizeSelectors.maxArea(
                    array.getInteger(R.styleable.CameraView_cameraPictureSizeMaxArea, 0)
                )
            )
        }

        if (array.hasValue(R.styleable.CameraView_cameraPictureSizeAspectRatio)) {
            pictureConstraints.add(
                SizeSelectors.aspectRatio(
                    AspectRatio.parse(
                        array.getString(
                            R.styleable.CameraView_cameraPictureSizeAspectRatio
                        )!!
                    ), 0f
                )
            )
        }

        if (array.getBoolean(R.styleable.CameraView_cameraPictureSizeSmallest, false)) {
            pictureConstraints.add(SizeSelectors.smallest())
        }

        if (array.getBoolean(R.styleable.CameraView_cameraPictureSizeBiggest, false)) {
            pictureConstraints.add(SizeSelectors.biggest())
        }

        pictureSizeSelector =
            if (!pictureConstraints.isEmpty()) SizeSelectors.and(*pictureConstraints.toTypedArray<SizeSelector?>()) else SizeSelectors.biggest()

        // Video size selector
        val videoConstraints: MutableList<SizeSelector?> = ArrayList(3)

        if (array.hasValue(R.styleable.CameraView_cameraVideoSizeMinWidth)) {
            videoConstraints.add(
                SizeSelectors.minWidth(
                    array.getInteger(R.styleable.CameraView_cameraVideoSizeMinWidth, 0)
                )
            )
        }

        if (array.hasValue(R.styleable.CameraView_cameraVideoSizeMaxWidth)) {
            videoConstraints.add(
                SizeSelectors.maxWidth(
                    array.getInteger(R.styleable.CameraView_cameraVideoSizeMaxWidth, 0)
                )
            )
        }

        if (array.hasValue(R.styleable.CameraView_cameraVideoSizeMinHeight)) {
            videoConstraints.add(
                SizeSelectors.minHeight(
                    array.getInteger(R.styleable.CameraView_cameraVideoSizeMinHeight, 0)
                )
            )
        }

        if (array.hasValue(R.styleable.CameraView_cameraVideoSizeMaxHeight)) {
            videoConstraints.add(
                SizeSelectors.maxHeight(
                    array.getInteger(R.styleable.CameraView_cameraVideoSizeMaxHeight, 0)
                )
            )
        }

        if (array.hasValue(R.styleable.CameraView_cameraVideoSizeMinArea)) {
            videoConstraints.add(
                SizeSelectors.minArea(
                    array.getInteger(R.styleable.CameraView_cameraVideoSizeMinArea, 0)
                )
            )
        }

        if (array.hasValue(R.styleable.CameraView_cameraVideoSizeMaxArea)) {
            videoConstraints.add(
                SizeSelectors.maxArea(
                    array.getInteger(R.styleable.CameraView_cameraVideoSizeMaxArea, 0)
                )
            )
        }

        if (array.hasValue(R.styleable.CameraView_cameraVideoSizeAspectRatio)) {
            videoConstraints.add(
                SizeSelectors.aspectRatio(
                    AspectRatio.parse(
                        array.getString(
                            R.styleable.CameraView_cameraVideoSizeAspectRatio
                        )!!
                    ), 0f
                )
            )
        }

        if (array.getBoolean(R.styleable.CameraView_cameraVideoSizeSmallest, false)) {
            videoConstraints.add(SizeSelectors.smallest())
        }

        if (array.getBoolean(R.styleable.CameraView_cameraVideoSizeBiggest, false)) {
            videoConstraints.add(SizeSelectors.biggest())
        }

        videoSizeSelector =
            if (!videoConstraints.isEmpty()) SizeSelectors.and(*videoConstraints.toTypedArray<SizeSelector?>()) else SizeSelectors.biggest()
    }
}
