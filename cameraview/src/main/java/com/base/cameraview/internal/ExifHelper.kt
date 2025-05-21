package com.base.cameraview.internal

import androidx.exifinterface.media.ExifInterface

/**
 * Super basic exif utilities.
 */
object ExifHelper {
    /**
     * Maps an [ExifInterface] orientation value
     * to the actual degrees.
     */
    @JvmStatic
    fun getOrientation(exifOrientation: Int): Int {
        val orientation: Int = when (exifOrientation) {
            ExifInterface.ORIENTATION_NORMAL, ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> 0

            ExifInterface.ORIENTATION_ROTATE_180, ExifInterface.ORIENTATION_FLIP_VERTICAL -> 180

            ExifInterface.ORIENTATION_ROTATE_90, ExifInterface.ORIENTATION_TRANSPOSE -> 90

            ExifInterface.ORIENTATION_ROTATE_270, ExifInterface.ORIENTATION_TRANSVERSE -> 270

            else -> 0
        }
        return orientation
    }

    /**
     * Maps a degree value to [ExifInterface] constant.
     */
    @JvmStatic
    fun getExifOrientation(orientation: Int): Int {
        return when ((orientation + 360) % 360) {
            0 -> ExifInterface.ORIENTATION_NORMAL
            90 -> ExifInterface.ORIENTATION_ROTATE_90
            180 -> ExifInterface.ORIENTATION_ROTATE_180
            270 -> ExifInterface.ORIENTATION_ROTATE_270
            else -> throw IllegalArgumentException("Invalid orientation: $orientation")
        }
    }
}

