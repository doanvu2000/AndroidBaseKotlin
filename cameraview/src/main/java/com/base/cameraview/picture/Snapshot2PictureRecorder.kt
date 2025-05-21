package com.base.cameraview.picture

import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.CaptureResult
import android.hardware.camera2.TotalCaptureResult
import com.base.cameraview.PictureResult
import com.base.cameraview.engine.Camera2Engine
import com.base.cameraview.engine.action.Action
import com.base.cameraview.engine.action.ActionHolder
import com.base.cameraview.engine.action.Actions.sequence
import com.base.cameraview.engine.action.Actions.timeout
import com.base.cameraview.engine.action.BaseAction
import com.base.cameraview.engine.action.CompletionCallback
import com.base.cameraview.engine.lock.LockAction
import com.base.cameraview.preview.RendererCameraPreview
import com.base.cameraview.size.AspectRatio

/**
 * Camera2 engine supports metering for snapshots and we expect for them to correctly fire flash as well.
 * The first idea, and in theory, the most correct one, was to set
 * [CaptureRequest.CONTROL_CAPTURE_INTENT] to
 * [CaptureRequest.CONTROL_CAPTURE_INTENT_STILL_CAPTURE].
 *
 *
 * According to documentation, this will automatically trigger the flash if parameters says so.
 * In fact this is what happens, but it is a very fast flash that only lasts for 1 or 2 frames.
 * It's not easy to call super.take() at the exact time so that we capture the frame that was lit.
 * I have tried by comparing [android.graphics.SurfaceTexture.getTimestamp] and
 * [CaptureResult.SENSOR_TIMESTAMP] to identify the correct frame. These timestamps match,
 * but the frame is not the correct one.
 *
 *
 * So what we do here is ignore the [CaptureRequest.CONTROL_CAPTURE_INTENT] and instead
 * open the torch, if requested to do so. Then wait for exposure to settle again and finally
 * take a snapshot. I'd still love to use the capture intent instead of this, but was not able yet.
 */
class Snapshot2PictureRecorder(
    stub: PictureResult.Stub,
    engine: Camera2Engine,
    preview: RendererCameraPreview,
    outputRatio: AspectRatio
) : SnapshotGlPictureRecorder(stub, engine, preview, outputRatio, engine.overlay) {
    private val mAction: Action
    private val mHolder = engine
    private val mActionNeeded: Boolean
    private val mOriginalAeMode: Int?
    private val mOriginalFlashMode: Int?

    init {

        mAction = sequence(
            timeout(LOCK_TIMEOUT, LockAction()),
            FlashAction()
        )
        mAction.addCallback(object : CompletionCallback() {
            override fun onActionCompleted(action: Action) {
                LOG.i("Taking picture with super.take().")
                super@Snapshot2PictureRecorder.take()
            }
        })

        val lastResult: CaptureResult? = mHolder.getLastResult(mAction)
        if (lastResult == null) {
            LOG.w(
                "Picture snapshot requested very early, before the first preview frame.",
                "Metering might not work as intended."
            )
        }
        val aeState = lastResult?.get(CaptureResult.CONTROL_AE_STATE)
        mActionNeeded = engine.pictureSnapshotMetering
                && aeState != null && aeState == CaptureResult.CONTROL_AE_STATE_FLASH_REQUIRED
        mOriginalAeMode = mHolder.getBuilder(mAction).get<Int?>(CaptureRequest.CONTROL_AE_MODE)
        mOriginalFlashMode = mHolder.getBuilder(mAction).get<Int?>(CaptureRequest.FLASH_MODE)
    }

    override fun take() {
        if (!mActionNeeded) {
            LOG.i(
                "take:", "Engine does no metering or needs no flash.",
                "Taking fast snapshot."
            )
            super.take()
        } else {
            LOG.i("take:", "Engine needs flash. Starting action")
            mAction.start(mHolder)
        }
    }

    override fun dispatchResult() {
        // Revert our changes.
        ResetFlashAction().start(mHolder)
        super.dispatchResult()
    }

    private inner class FlashAction : BaseAction() {
        override fun onStart(holder: ActionHolder) {
            super.onStart(holder)
            LOG.i("FlashAction:", "Parameters locked, opening torch.")
            holder.getBuilder(this).set(
                CaptureRequest.FLASH_MODE,
                CaptureRequest.FLASH_MODE_TORCH
            )
            holder.getBuilder(this).set(
                CaptureRequest.CONTROL_AE_MODE,
                CaptureRequest.CONTROL_AE_MODE_ON
            )
            holder.applyBuilder(this)
        }

        override fun onCaptureCompleted(
            holder: ActionHolder,
            request: CaptureRequest,
            result: TotalCaptureResult
        ) {
            super.onCaptureCompleted(holder, request, result)
            val flashState = result.get(CaptureResult.FLASH_STATE)
            when (flashState) {
                null -> {
                    LOG.w(
                        "FlashAction:", "Waiting flash, but flashState is null!",
                        "Taking snapshot."
                    )
                    state = Action.Companion.STATE_COMPLETED
                }

                CaptureResult.FLASH_STATE_FIRED -> {
                    LOG.i(
                        "FlashAction:", "Waiting flash and we have FIRED state!",
                        "Taking snapshot."
                    )
                    state = Action.Companion.STATE_COMPLETED
                }

                else -> {
                    LOG.i(
                        "FlashAction:", "Waiting flash but flashState is",
                        flashState, ". Waiting..."
                    )
                }
            }
        }
    }

    private inner class ResetFlashAction : BaseAction() {
        override fun onStart(holder: ActionHolder) {
            super.onStart(holder)
            try {
                // See Camera2Engine.setFlash() comments: turning TORCH off has bugs and we must do
                // as follows.
                LOG.i("ResetFlashAction:", "Reverting the flash changes.")
                val builder = holder.getBuilder(this)
                builder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
                builder.set(CaptureRequest.FLASH_MODE, CaptureResult.FLASH_MODE_OFF)
                holder.applyBuilder(this, builder)
                builder.set(CaptureRequest.CONTROL_AE_MODE, mOriginalAeMode)
                builder.set(CaptureRequest.FLASH_MODE, mOriginalFlashMode)
                holder.applyBuilder(this)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val LOCK_TIMEOUT: Long = 2500
    }
}
