package com.base.cameraview.picture

import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.DngCreator
import android.hardware.camera2.TotalCaptureResult
import android.media.Image
import android.media.ImageReader
import android.media.ImageReader.OnImageAvailableListener
import androidx.exifinterface.media.ExifInterface
import com.base.cameraview.PictureResult
import com.base.cameraview.controls.PictureFormat
import com.base.cameraview.engine.Camera2Engine
import com.base.cameraview.engine.action.Action
import com.base.cameraview.engine.action.ActionHolder
import com.base.cameraview.engine.action.BaseAction
import com.base.cameraview.internal.ExifHelper.getExifOrientation
import com.base.cameraview.internal.ExifHelper.getOrientation
import com.base.cameraview.internal.WorkerHandler
import java.io.BufferedOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * A [PictureResult] that uses standard APIs.
 */
class Full2PictureRecorder(
    stub: PictureResult.Stub,
    engine: Camera2Engine,
    pictureBuilder: CaptureRequest.Builder,
    pictureReader: ImageReader
) : FullPictureRecorder(stub, engine), OnImageAvailableListener {
    private val mHolder: ActionHolder = engine
    private val mAction: Action
    private val mPictureReader: ImageReader = pictureReader
    private val mPictureBuilder: CaptureRequest.Builder = pictureBuilder

    private var mDngCreator: DngCreator? = null

    init {
        mPictureReader.setOnImageAvailableListener(this, WorkerHandler.get().handler)
        mAction = object : BaseAction() {
            override fun onStart(holder: ActionHolder) {
                super.onStart(holder)
                mPictureBuilder.addTarget(mPictureReader.surface)
                if (mResult?.format == PictureFormat.JPEG) {
                    mPictureBuilder.set(CaptureRequest.JPEG_ORIENTATION, mResult?.rotation ?: 0)
                }
                mPictureBuilder.setTag(CameraDevice.TEMPLATE_STILL_CAPTURE)
                try {
                    holder.applyBuilder(this, mPictureBuilder)
                } catch (e: CameraAccessException) {
                    mResult = null
                    mError = e
                    dispatchResult()
                    state = Action.Companion.STATE_COMPLETED
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCaptureStarted(
                holder: ActionHolder,
                request: CaptureRequest
            ) {
                super.onCaptureStarted(holder, request)
                if (request.tag == (CameraDevice.TEMPLATE_STILL_CAPTURE as Int?)) {
                    LOG.i("onCaptureStarted:", "Dispatching picture shutter.")
                    dispatchOnShutter(false)
                    state = Action.Companion.STATE_COMPLETED
                }
            }

            override fun onCaptureCompleted(
                holder: ActionHolder,
                request: CaptureRequest,
                result: TotalCaptureResult
            ) {
                try {
                    super.onCaptureCompleted(holder, request, result)
                } catch (e: Exception) {
                    mError = e
                    dispatchResult()
                }

                if (mResult?.format == PictureFormat.DNG) {
                    mDngCreator = DngCreator(holder.getCharacteristics(this), result)
                    mDngCreator!!.setOrientation(getExifOrientation(mResult?.rotation ?: 0))
                    if (mResult?.location != null) {
                        mDngCreator!!.setLocation(mResult?.location!!)
                    }
                }
            }
        }
    }

    override fun take() {
        mAction.start(mHolder)
    }

    override fun onImageAvailable(reader: ImageReader) {
        LOG.i("onImageAvailable started.")
        var image: Image? = null
        try {
            image = reader.acquireNextImage()
            when (mResult?.format) {
                PictureFormat.JPEG -> readJpegImage(image)
                PictureFormat.DNG -> readRawImage(image)
                else -> throw IllegalStateException("Unknown format: " + mResult?.format)
            }
        } catch (e: Exception) {
            mResult = null
            mError = e
            dispatchResult()
            return
        } finally {
            image?.close()
        }

        // Leave.
        LOG.i("onImageAvailable ended.")
        dispatchResult()
    }

    private fun readJpegImage(image: Image) {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        mResult?.data = bytes

        // Just like Camera1, unfortunately, the camera might rotate the image
        // and put EXIF=0 instead of respecting our EXIF and leave the image unaltered.
        mResult?.rotation = 0
        try {
            val exif = ExifInterface(ByteArrayInputStream(mResult?.data))
            val exifOrientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            mResult?.rotation = getOrientation(exifOrientation)
        } catch (ignore: IOException) {
            // Should not happen
            ignore.printStackTrace()
        }
    }

    private fun readRawImage(image: Image) {
        val array = ByteArrayOutputStream()
        val stream = BufferedOutputStream(array)
        try {
            mDngCreator!!.writeImage(stream, image)
            stream.flush()
            mResult?.data = array.toByteArray()
        } catch (e: IOException) {
            mDngCreator!!.close()
            try {
                stream.close()
            } catch (ignore: IOException) {
                ignore.printStackTrace()
            }
            throw RuntimeException(e)
        }
    }
}
