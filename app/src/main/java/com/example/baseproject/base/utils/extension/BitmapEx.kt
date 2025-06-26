package com.example.baseproject.base.utils.extension

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

// Constants for bitmap flipping
private const val FLIP_ORIENTATION = -1.0f
private const val DEFAULT_ORIENTATION = 1.0f

/**
 * Append bitmap2 to bottom of bitmap_root vertically
 */
fun Bitmap.appendVertical(bitmap2: Bitmap): Bitmap {
    return mergeBitmapsVertical(this, bitmap2)
}

/**
 * Overlay bitmap2 into bitmap_root
 */
fun Bitmap.overlay(bitmap2: Bitmap): Bitmap {
    return overlayBitMap(this, bitmap2)
}

/**
 * Flip bitmap horizontally (X-axis) or vertically (Y-axis)
 * @param flipXAxis true for horizontal flip, false for vertical flip
 */
fun Bitmap.flip(flipXAxis: Boolean = false): Bitmap {
    return if (flipXAxis) flipHorizontalX() else flipVerticalY()
}

/**
 * Flip bitmap horizontally along X-axis
 */
fun Bitmap.flipHorizontalX(): Bitmap {
    return createWithMatrix(Matrix().apply {
        postScale(FLIP_ORIENTATION, DEFAULT_ORIENTATION)
    })
}

/**
 * Flip bitmap vertically along Y-axis
 */
fun Bitmap.flipVerticalY(): Bitmap {
    return createWithMatrix(Matrix().apply {
        postScale(DEFAULT_ORIENTATION, FLIP_ORIENTATION)
    })
}

/**
 * Rotate bitmap by specified angle in degrees
 * @param angle rotation angle in degrees
 */
fun Bitmap.rotate(angle: Float): Bitmap {
    return createWithMatrix(Matrix().apply {
        postRotate(angle)
    })
}

/**
 * Create a new bitmap with applied matrix transformation
 * @param matrix transformation matrix to apply
 */
fun Bitmap.createWithMatrix(matrix: Matrix): Bitmap =
    Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)

/**
 * Merge two bitmaps vertically (bitmap2 placed below bitmap1)
 * @param bitmap1 top bitmap
 * @param bitmap2 bottom bitmap
 * @return merged bitmap with bitmap2 below bitmap1
 */
fun mergeBitmapsVertical(bitmap1: Bitmap, bitmap2: Bitmap): Bitmap {
    val width = bitmap1.width.coerceAtLeast(bitmap2.width)
    val height = bitmap1.height + bitmap2.height

    val mergedBitmap = createBitmap(width, height)
    val canvas = Canvas(mergedBitmap)

    canvas.drawBitmap(bitmap1, 0f, 0f, null)
    canvas.drawBitmap(bitmap2, 0f, bitmap1.height.toFloat(), null)

    return mergedBitmap
}

/**
 * Overlay one bitmap on top of another
 * @param bmp1 base bitmap
 * @param bmp2 overlay bitmap (will be scaled to match base bitmap size)
 * @return composite bitmap with bmp2 overlaid on bmp1
 */
fun overlayBitMap(bmp1: Bitmap, bmp2: Bitmap): Bitmap {
    val bmOverlay = createBitmap(bmp1.width, bmp1.height)
    val bitmap2Scaled = bmp2.scale(bmOverlay.width, bmOverlay.height, false)
    val canvas = Canvas(bmOverlay)

    canvas.drawBitmap(bmp1, Matrix(), null)
    canvas.drawBitmap(bitmap2Scaled, 0f, 0f, null)

    // Clean up resources
    bmp1.recycle()
    bmp2.recycle()
    bitmap2Scaled.recycle()

    return bmOverlay
}