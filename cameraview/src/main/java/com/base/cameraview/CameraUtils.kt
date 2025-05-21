package com.base.cameraview

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.hardware.Camera
import android.hardware.Camera.CameraInfo
import android.os.Handler
import android.os.Looper
import androidx.annotation.WorkerThread
import androidx.exifinterface.media.ExifInterface
import com.base.cameraview.CameraLogger.Companion.create
import com.base.cameraview.controls.Facing
import com.base.cameraview.engine.mappers.Camera1Mapper.Companion.get
import com.base.cameraview.internal.ExifHelper.getOrientation
import com.base.cameraview.internal.WorkerHandler
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Static utilities for dealing with camera I/O, orientations, etc.
 */
@Suppress("unused")
object CameraUtils {
    private val TAG: String = CameraUtils::class.java.simpleName
    private val LOG = create(TAG)

    /**
     * Determines whether the device has valid camera sensors, so the library
     * can be used.
     *
     * @param context a valid Context
     * @return whether device has cameras
     */
    fun hasCameras(context: Context): Boolean {
        val manager = context.packageManager
        // There's also FEATURE_CAMERA_EXTERNAL , should we support it?
        return manager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY) || manager.hasSystemFeature(
            PackageManager.FEATURE_CAMERA_FRONT
        )
    }

    /**
     * Determines whether the device has a valid camera sensor with the given
     * Facing value, so that a session can be started.
     *
     * @param context a valid context
     * @param facing  either [Facing.BACK] or [Facing.FRONT]
     * @return true if such sensor exists
     */
    fun hasCameraFacing(
        @Suppress("unused") context: Context, facing: Facing
    ): Boolean {
        val internal = get().mapFacing(facing)
        val cameraInfo = CameraInfo()
        var i = 0
        val count = Camera.getNumberOfCameras()
        while (i < count) {
            Camera.getCameraInfo(i, cameraInfo)
            if (cameraInfo.facing == internal) return true
            i++
        }
        return false
    }

    /**
     * Simply writes the given data to the given file. It is done synchronously. If you are
     * running on the UI thread, please use [.writeToFile]
     * and pass a file callback.
     *
     *
     * If any error is encountered, this returns null.
     *
     * @param data the data to be written
     * @param file the file to write into
     * @return the source file, or null if error
     */
    @WorkerThread
    @SuppressLint("NewApi")
    fun writeToFile(data: ByteArray, file: File): File? {
        if (file.exists() && !file.delete()) return null
        try {
            BufferedOutputStream(FileOutputStream(file)).use { stream ->
                stream.write(data)
                stream.flush()
                return file
            }
        } catch (e: IOException) {
            LOG.e("writeToFile:", "could not write file.", e)
            return null
        }
    }

    /**
     * Writes the given data to the given file in a background thread, returning on the
     * original thread (typically the UI thread) once writing is done.
     * If some error is encountered, the [FileCallback] will return null instead of the
     * original file.
     *
     * @param data     the data to be written
     * @param file     the file to write into
     * @param callback a callback
     */
    @JvmStatic
    fun writeToFile(
        data: ByteArray, file: File, callback: FileCallback
    ) {
        val ui = Handler(Looper.getMainLooper())
        WorkerHandler.execute {
            ui.post {
                callback.onFileReady(writeToFile(data, file))
            }
        }
    }

    /**
     * Decodes an input byte array and outputs a Bitmap that is ready to be displayed.
     * The difference with [BitmapFactory.decodeByteArray]
     * is that this cares about orientation, reading it from the EXIF header.
     *
     * @param source a JPEG byte array
     * @return decoded bitmap or null if error is encountered
     */
    @WorkerThread
    fun decodeBitmap(source: ByteArray): Bitmap? {
        return decodeBitmap(source, Int.Companion.MAX_VALUE, Int.Companion.MAX_VALUE)
    }

    /**
     * Decodes an input byte array and outputs a Bitmap that is ready to be displayed.
     * The difference with [BitmapFactory.decodeByteArray]
     * is that this cares about orientation, reading it from the EXIF header.
     * This is executed in a background thread, and returns the result to the original thread.
     *
     * @param source   a JPEG byte array
     * @param callback a callback to be notified
     */
    @JvmStatic
    fun decodeBitmap(
        source: ByteArray, callback: BitmapCallback
    ) {
        decodeBitmap(source, Int.Companion.MAX_VALUE, Int.Companion.MAX_VALUE, callback)
    }

    /**
     * Decodes an input byte array and outputs a Bitmap that is ready to be displayed.
     * The difference with [BitmapFactory.decodeByteArray]
     * is that this cares about orientation, reading it from the EXIF header.
     * This is executed in a background thread, and returns the result to the original thread.
     *
     *
     * The image is also downscaled taking care of the maxWidth and maxHeight arguments.
     *
     * @param source    a JPEG byte array
     * @param maxWidth  the max allowed width
     * @param maxHeight the max allowed height
     * @param callback  a callback to be notified
     */
    fun decodeBitmap(
        source: ByteArray, maxWidth: Int, maxHeight: Int, callback: BitmapCallback
    ) {
        decodeBitmap(source, maxWidth, maxHeight, BitmapFactory.Options(), callback)
    }

    /**
     * Decodes an input byte array and outputs a Bitmap that is ready to be displayed.
     * The difference with [BitmapFactory.decodeByteArray]
     * is that this cares about orientation, reading it from the EXIF header.
     * This is executed in a background thread, and returns the result to the original thread.
     *
     *
     * The image is also downscaled taking care of the maxWidth and maxHeight arguments.
     *
     * @param source    a JPEG byte array
     * @param maxWidth  the max allowed width
     * @param maxHeight the max allowed height
     * @param options   the options to be passed to decodeByteArray
     * @param callback  a callback to be notified
     */
    fun decodeBitmap(
        source: ByteArray,
        maxWidth: Int,
        maxHeight: Int,
        options: BitmapFactory.Options,
        callback: BitmapCallback
    ) {
        decodeBitmap(source, maxWidth, maxHeight, options, -1, callback)
    }

    @JvmStatic
    fun decodeBitmap(
        source: ByteArray,
        maxWidth: Int,
        maxHeight: Int,
        options: BitmapFactory.Options,
        rotation: Int,
        callback: BitmapCallback
    ) {
        val ui = Handler(Looper.getMainLooper())
        WorkerHandler.execute {
            val bitmap = decodeBitmap(source, maxWidth, maxHeight, options, rotation)
            ui.post {
                callback.onBitmapReady(bitmap)
            }
        }
    }

    /**
     * Decodes an input byte array and outputs a Bitmap that is ready to be displayed.
     * The difference with [BitmapFactory.decodeByteArray]
     * is that this cares about orientation, reading it from the EXIF header.
     *
     *
     * The image is also downscaled taking care of the maxWidth and maxHeight arguments.
     *
     * @param source    a JPEG byte array
     * @param maxWidth  the max allowed width
     * @param maxHeight the max allowed height
     * @return decoded bitmap or null if error is encountered
     */
    @WorkerThread
    fun decodeBitmap(source: ByteArray, maxWidth: Int, maxHeight: Int): Bitmap? {
        return decodeBitmap(source, maxWidth, maxHeight, BitmapFactory.Options())
    }

    /**
     * Decodes an input byte array and outputs a Bitmap that is ready to be displayed.
     * The difference with [BitmapFactory.decodeByteArray]
     * is that this cares about orientation, reading it from the EXIF header.
     *
     *
     * The image is also downscaled taking care of the maxWidth and maxHeight arguments.
     *
     * @param source    a JPEG byte array
     * @param maxWidth  the max allowed width
     * @param maxHeight the max allowed height
     * @param options   the options to be passed to decodeByteArray
     * @return decoded bitmap or null if error is encountered
     */
    @WorkerThread
    fun decodeBitmap(
        source: ByteArray, maxWidth: Int, maxHeight: Int, options: BitmapFactory.Options
    ): Bitmap? {
        return decodeBitmap(source, maxWidth, maxHeight, options, -1)
    }

    // Null means we got OOM
    // Ignores flipping, but it should be super rare.
    private fun decodeBitmap(
        source: ByteArray,
        maxWidth: Int,
        maxHeight: Int,
        options: BitmapFactory.Options,
        rotation: Int
    ): Bitmap? {
        var maxWidth = maxWidth
        var maxHeight = maxHeight
        if (maxWidth <= 0) maxWidth = Int.Companion.MAX_VALUE
        if (maxHeight <= 0) maxHeight = Int.Companion.MAX_VALUE
        var orientation: Int
        var flip: Boolean
        if (rotation == -1) {
            var stream: InputStream? = null
            try {
                // http://sylvana.net/jpegcrop/exif_orientation.html
                stream = ByteArrayInputStream(source)
                val exif = ExifInterface(stream)
                val exifOrientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL
                )
                orientation = getOrientation(exifOrientation)
                flip =
                    exifOrientation == ExifInterface.ORIENTATION_FLIP_HORIZONTAL || exifOrientation == ExifInterface.ORIENTATION_FLIP_VERTICAL || exifOrientation == ExifInterface.ORIENTATION_TRANSPOSE || exifOrientation == ExifInterface.ORIENTATION_TRANSVERSE
                LOG.i("decodeBitmap:", "got orientation from EXIF.", orientation)
            } catch (e: IOException) {
                LOG.e("decodeBitmap:", "could not get orientation from EXIF.", e)
                orientation = 0
                flip = false
            } finally {
                if (stream != null) {
                    try {
                        stream.close()
                    } catch (ignored: Exception) {
                    }
                }
            }
        } else {
            orientation = rotation
            flip = false
            LOG.i("decodeBitmap:", "got orientation from constructor.", orientation)
        }

        var bitmap: Bitmap?
        try {
            if (maxWidth < Int.Companion.MAX_VALUE || maxHeight < Int.Companion.MAX_VALUE) {
                options.inJustDecodeBounds = true
                BitmapFactory.decodeByteArray(source, 0, source.size, options)

                var outHeight = options.outHeight
                var outWidth = options.outWidth
                if (orientation % 180 != 0) {
                    outHeight = options.outWidth
                    outWidth = options.outHeight
                }

                options.inSampleSize = computeSampleSize(outWidth, outHeight, maxWidth, maxHeight)
                options.inJustDecodeBounds = false
                bitmap = BitmapFactory.decodeByteArray(source, 0, source.size, options)
            } else {
                bitmap = BitmapFactory.decodeByteArray(source, 0, source.size)
            }

            if (orientation != 0 || flip) {
                val matrix = Matrix()
                matrix.setRotate(orientation.toFloat())
                val temp = bitmap
                bitmap = Bitmap.createBitmap(
                    bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true
                )
                temp!!.recycle()
            }
        } catch (e: OutOfMemoryError) {
            bitmap = null
        }
        return bitmap
    }

    private fun computeSampleSize(width: Int, height: Int, maxWidth: Int, maxHeight: Int): Int {
        // https://developer.android.com/topic/performance/graphics/load-bitmap.html
        var inSampleSize = 1
        if (height > maxHeight || width > maxWidth) {
            while ((height / inSampleSize) >= maxHeight || (width / inSampleSize) >= maxWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
