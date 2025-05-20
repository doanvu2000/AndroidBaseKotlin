package com.base.cameraview.engine.orchestrator

enum class CameraState(private val mState: Int) {
    OFF(0), ENGINE(1), BIND(2), PREVIEW(3);

    fun isAtLeast(reference: CameraState): Boolean {
        return mState >= reference.mState
    }
}
