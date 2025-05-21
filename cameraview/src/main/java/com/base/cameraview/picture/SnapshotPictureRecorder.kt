package com.base.cameraview.picture

import com.base.cameraview.CameraLogger
import com.base.cameraview.PictureResult

/**
 * Helps with logging.
 */
abstract class SnapshotPictureRecorder(
    stub: PictureResult.Stub,
    listener: PictureResultListener?
) : PictureRecorder(stub, listener) {
    companion object {
        private val TAG: String = SnapshotPictureRecorder::class.java.simpleName
        val LOG: CameraLogger = CameraLogger.create(TAG)
    }
}
