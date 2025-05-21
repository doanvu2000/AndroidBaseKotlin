package com.base.cameraview.picture

import com.base.cameraview.CameraLogger
import com.base.cameraview.PictureResult

/**
 * Helps with logging.
 */
abstract class FullPictureRecorder(
    stub: PictureResult.Stub,
    listener: PictureResultListener?
) : PictureRecorder(stub, listener) {
    companion object {
        val TAG: String = FullPictureRecorder::class.java.simpleName
        val LOG: CameraLogger = CameraLogger.create(TAG)
    }
}
