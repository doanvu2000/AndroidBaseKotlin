package com.base.cameraview.internal

import android.graphics.Rect
import com.base.cameraview.size.AspectRatio
import com.base.cameraview.size.Size
import kotlin.math.roundToInt

/**
 * Simply computes the crop between a full size and a desired aspect ratio.
 */
object CropHelper {
    // It's important that size and aspect ratio belong to the same reference.
    @JvmStatic
    fun computeCrop(currentSize: Size, targetRatio: AspectRatio): Rect {
        val currentWidth = currentSize.width
        val currentHeight = currentSize.height
        if (targetRatio.matches(currentSize, 0.0005f)) {
            return Rect(0, 0, currentWidth, currentHeight)
        }

        // They are not equal. Compute.
        val currentRatio = AspectRatio.of(currentWidth, currentHeight)
        val x: Int
        val y: Int
        val width: Int
        val height: Int
        if (currentRatio.toFloat() > targetRatio.toFloat()) {
            height = currentHeight
            width = (height * targetRatio.toFloat()).roundToInt()
            y = 0
            x = ((currentWidth - width) / 2f).roundToInt()
        } else {
            width = currentWidth
            height = (width / targetRatio.toFloat()).roundToInt()
            y = ((currentHeight - height) / 2f).roundToInt()
            x = 0
        }
        return Rect(x, y, x + width, y + height)
    }
}

