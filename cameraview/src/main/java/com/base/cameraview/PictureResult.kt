package com.base.cameraview

import android.graphics.BitmapFactory
import android.location.Location
import com.base.cameraview.CameraUtils.decodeBitmap
import com.base.cameraview.CameraUtils.writeToFile
import com.base.cameraview.controls.Facing
import com.base.cameraview.controls.PictureFormat
import com.base.cameraview.size.Size
import java.io.File

@Suppress("unused")
class PictureResult internal constructor(builder: Stub) {
    /**
     * Returns whether this result comes from a snapshot.
     *
     * @return whether this is a snapshot
     */
    val isSnapshot: Boolean = builder.isSnapshot

    /**
     * Returns geographic information for this picture, if any.
     * If it was set, it is also present in the file metadata.
     *
     * @return a nullable Location
     */
    val location: Location? = builder.location

    /**
     * Returns the clock-wise rotation that should be applied to the
     * picture before displaying. If it is non-zero, it is also present
     * in the EXIF metadata.
     *
     * @return the clock-wise rotation
     */
    val rotation: Int = builder.rotation

    /**
     * Returns the size of the picture after the rotation is applied.
     *
     * @return the Size of this picture
     */
    val size: Size = builder.size!!

    /**
     * Returns the facing value with which this video was recorded.
     *
     * @return the Facing of this video
     */
    val facing: Facing = builder.facing!!

    /**
     * Returns the raw compressed, ready to be saved to file,
     * in the given format.
     *
     * @return the compressed data stream
     */
    val data: ByteArray = builder.data

    /**
     * Returns the format for [.getData].
     *
     * @return the format
     */
    val format: PictureFormat = builder.format!!

    /**
     * Decodes this picture on a background thread and posts the result in the UI thread using
     * the given callback.
     *
     * @param maxWidth  the max. width of final bitmap
     * @param maxHeight the max. height of final bitmap
     * @param callback  a callback to be notified of image decoding
     */
    fun toBitmap(maxWidth: Int, maxHeight: Int, callback: BitmapCallback) {
        when (format) {
            PictureFormat.JPEG -> {
                decodeBitmap(
                    this.data, maxWidth, maxHeight, BitmapFactory.Options(),
                    rotation, callback
                )
            }

            PictureFormat.DNG -> {
                // Apparently: BitmapFactory added DNG support in API 24.
                // https://github.com/aosp-mirror/platform_frameworks_base/blob/nougat-mr1-release/core/jni/android/graphics/BitmapFactory.cpp
                decodeBitmap(
                    this.data, maxWidth, maxHeight, BitmapFactory.Options(),
                    rotation, callback
                )
            }
        }
    }

    /**
     * Shorthand for [CameraUtils.decodeBitmap].
     * Decodes this picture on a background thread and posts the result in the UI thread using
     * the given callback.
     *
     * @param callback a callback to be notified of image decoding
     */
    fun toBitmap(callback: BitmapCallback) {
        toBitmap(-1, -1, callback)
    }

    /**
     * Shorthand for [CameraUtils.writeToFile].
     * This writes this picture to file on a background thread and posts the result in the UI
     * thread using the given callback.
     *
     * @param file     the file to write into
     * @param callback a callback
     */
    fun toFile(file: File, callback: FileCallback) {
        writeToFile(this.data, file, callback)
    }

    /**
     * A result stub, for internal use only.
     */
    class Stub internal constructor() {
        var isSnapshot: Boolean = false
        var location: Location? = null
        var rotation: Int = 0
        var size: Size? = null
        var facing: Facing? = null
        var data: ByteArray = ByteArray(0)
        var format: PictureFormat? = null
    }
}
