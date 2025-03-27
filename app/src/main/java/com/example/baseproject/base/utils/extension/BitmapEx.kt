package com.example.baseproject.base.utils.extension

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale

/**
 * append bitmap2 to bottom bitmap_root
 * */
fun Bitmap.appendVertical(bitmap2: Bitmap): Bitmap {
    return mergeBitmapsVertical(this, bitmap2)
}

/**
 * overlay bitmap2 into bitmap_root
 * */
fun Bitmap.overlay(bitmap2: Bitmap): Bitmap {
    return overlayBitMap(this, bitmap2)
}

/**
 * flip bitmap_root to horizontal xAxis or vertical yAxis
 * */
const val FLIP_ORIENTATION = -1.0f
const val DEFAULT_ORIENTATION = 1.0f
fun Bitmap.flip(flipXAxis: Boolean = false): Bitmap {
    return if (flipXAxis) flipHorizontalX() else flipVerticalY()
}

fun Bitmap.flipHorizontalX(): Bitmap {
    return createWithMatrix(Matrix().apply {
        postScale(FLIP_ORIENTATION, DEFAULT_ORIENTATION)
    })
}

fun Bitmap.flipVerticalY(): Bitmap {
    return createWithMatrix(Matrix().apply {
        postScale(DEFAULT_ORIENTATION, FLIP_ORIENTATION)
    })
}

fun Bitmap.rotate(angle: Float): Bitmap {
    return createWithMatrix(Matrix().apply {
        postRotate(angle)
    })
}

fun Bitmap.createWithMatrix(matrix: Matrix): Bitmap = Bitmap.createBitmap(
    this, 0, 0, this.width, this.height, matrix, true
)

fun mergeBitmapsVertical(bitmap1: Bitmap, bitmap2: Bitmap): Bitmap {
    val width = bitmap1.width.coerceAtLeast(bitmap2.width)
    val height = bitmap1.height + bitmap2.height

    val mergedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(mergedBitmap)

    val zeroFloat = 0f
    canvas.drawBitmap(bitmap1, zeroFloat, zeroFloat, null)
    canvas.drawBitmap(bitmap2, zeroFloat, bitmap1.height.toFloat(), null)

    return mergedBitmap
}

fun overlayBitMap(bmp1: Bitmap, bmp2: Bitmap): Bitmap {
    val bmOverlay = createBitmap(bmp1.width, bmp1.height)
    val bitmap2 = bmp2.scale(bmOverlay.width, bmOverlay.height, false)
    val canvas = Canvas(bmOverlay)
    canvas.drawBitmap(bmp1, Matrix(), null)
    canvas.drawBitmap(bitmap2, 0f, 0f, null)
    bmp1.recycle()
    bmp2.recycle()
    bitmap2.recycle()
    return bmOverlay
}