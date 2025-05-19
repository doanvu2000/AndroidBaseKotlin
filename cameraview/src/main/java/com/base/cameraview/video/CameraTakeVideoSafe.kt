package com.base.cameraview.video

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import com.base.cameraview.CameraView
import java.io.File

class CameraTakeVideoSafe : CameraView {
    companion object {
        const val TAG = "CameraView-CV2"
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)

    override fun takeVideoSnapshot(file: File) {
        try {
            super.takeVideoSnapshot(file)
        } catch (e: Exception) {
            Log.e(TAG, "takeVideoSnapshot: error: ${e.message}")
            e.printStackTrace()
        }
    }

    override fun stopVideo() {
        try {
            super.stopVideo()
        } catch (e: Exception) {
            Log.e(TAG, "stopVideo: error ${e.message}")
            e.printStackTrace()
        }
    }
}