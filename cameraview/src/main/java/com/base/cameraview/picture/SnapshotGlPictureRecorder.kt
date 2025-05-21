package com.base.cameraview.picture

import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.opengl.EGL14
import android.opengl.EGLContext
import android.opengl.Matrix
import androidx.annotation.WorkerThread
import com.base.cameraview.PictureResult
import com.base.cameraview.filter.Filter
import com.base.cameraview.internal.CropHelper
import com.base.cameraview.internal.GlTextureDrawer
import com.base.cameraview.internal.WorkerHandler
import com.base.cameraview.overlay.Overlay
import com.base.cameraview.overlay.OverlayDrawer
import com.base.cameraview.preview.RendererCameraPreview
import com.base.cameraview.preview.RendererFrameCallback
import com.base.cameraview.preview.RendererThread
import com.base.cameraview.size.AspectRatio
import com.base.cameraview.size.Size
import com.otaliastudios.opengl.core.EglCore
import com.otaliastudios.opengl.surface.EglSurface
import com.otaliastudios.opengl.surface.EglWindowSurface

/**
 * API 19.
 * Records a picture snapshots from the [RendererCameraPreview]. It works as follows:
 *
 *
 * - We register a one time [RendererFrameCallback] on the preview
 * - We get the textureId and the frame callback on the [RendererThread]
 * - [Optional: we construct another textureId for overlays]
 * - We take a handle of the EGL context from the [RendererThread]
 * - We move to another thread, and create a new EGL surface for that EGL context.
 * - We make this new surface current, and re-draw the textureId on it
 * - [Optional: fill the overlayTextureId and draw it on the same surface]
 * - We use glReadPixels (through [EglSurface.toByteArray])
 * and save to file.
 *
 *
 * We create a new EGL surface and redraw the frame because:
 * 1. We want to go off the renderer thread as soon as possible
 * 2. We have overlays to be drawn - we don't want to draw them on the preview surface,
 * not even for a frame.
 */
open class SnapshotGlPictureRecorder(
    stub: PictureResult.Stub,
    listener: PictureResultListener?,
    private val mPreview: RendererCameraPreview,
    outputRatio: AspectRatio,
    overlay: Overlay?
) : SnapshotPictureRecorder(stub, listener) {
    private var mOutputRatio: AspectRatio?

    private val mOverlay: Overlay?
    private val mHasOverlay: Boolean
    private var mOverlayDrawer: OverlayDrawer? = null
    private var mTextureDrawer: GlTextureDrawer? = null

    init {
        mOutputRatio = outputRatio
        mOverlay = overlay
        mHasOverlay = mOverlay != null && mOverlay.drawsOn(Overlay.Target.PICTURE_SNAPSHOT)
    }

    override fun take() {
        mPreview.addRendererFrameCallback(object : RendererFrameCallback {
            @RendererThread
            override fun onRendererTextureCreated(textureId: Int) {
                this@SnapshotGlPictureRecorder.onRendererTextureCreated(textureId)
            }

            @RendererThread
            override fun onRendererFilterChanged(filter: Filter) {
                this@SnapshotGlPictureRecorder.onRendererFilterChanged(filter)
            }

            @RendererThread
            override fun onRendererFrame(
                surfaceTexture: SurfaceTexture, rotation: Int, scaleX: Float, scaleY: Float
            ) {
                mPreview.removeRendererFrameCallback(this)
                this@SnapshotGlPictureRecorder.onRendererFrame(
                    surfaceTexture, rotation, scaleX, scaleY
                )
            }
        })
    }

    @RendererThread
    protected fun onRendererTextureCreated(textureId: Int) {
        mTextureDrawer = GlTextureDrawer(textureId)
        // Need to crop the size.
        if (mResult?.size == null) {
            mResult?.size = Size.defaultSize()
        }

        mResult?.size?.let { resultSize ->
            val crop = CropHelper.computeCrop(resultSize, mOutputRatio!!)
            mResult?.size = Size(crop.width(), crop.height())
            if (mHasOverlay) {
                mOverlayDrawer = OverlayDrawer(mOverlay!!, resultSize)
            }
        }
    }

    @RendererThread
    protected fun onRendererFilterChanged(filter: Filter) {
        mTextureDrawer!!.setFilter(filter.copy())
    }

    @RendererThread
    protected fun onRendererFrame(
        @Suppress("unused") surfaceTexture: SurfaceTexture,
        rotation: Int,
        scaleX: Float,
        scaleY: Float
    ) {
        // Get egl context from the RendererThread, which is the one in which we have created
        // the textureId and the overlayTextureId, managed by the GlSurfaceView.
        // Next operations can then be performed on different threads using this handle.
        val eglContext = EGL14.eglGetCurrentContext()
        WorkerHandler.execute {
            takeFrame(surfaceTexture, rotation, scaleX, scaleY, eglContext)
        }
    }

    /**
     * The tricky part here is the EGL surface creation.
     *
     *
     * We don't have a real output window for the EGL surface - we will use glReadPixels()
     * and never call swapBuffers(), so what we draw is never published.
     *
     *
     * 1. One option is to use a pbuffer EGL surface. This works, we just have to pass
     * the correct width and height. However, it is significantly slower than the current
     * solution.
     *
     *
     * 2. Another option is to create the EGL surface out of a ImageReader.getSurface()
     * and use the reader to create a JPEG. In this case, we would have to publish
     * the frame with swapBuffers(). However, currently ImageReader does not support
     * all formats, it's risky. This is an example error that we get:
     * "RGBA override BLOB format buffer should have height == width"
     *
     *
     * The third option, which we are using, is to create the EGL surface using whatever
     * [android.view.Surface] or [SurfaceTexture] we have at hand. Since we never call
     * swapBuffers(), the frame will not actually be rendered. This is the fastest.
     *
     * @param scaleX frame scale x in [com.base.cameraview.engine.offset.Reference.VIEW]
     * @param scaleY frame scale y in [com.base.cameraview.engine.offset.Reference.VIEW]
     */
    @WorkerThread
    protected fun takeFrame(
        surfaceTexture: SurfaceTexture,
        rotation: Int,
        scaleX: Float,
        scaleY: Float,
        eglContext: EGLContext
    ) {
        // 0. EGL window will need an output.
        // We create a fake one as explained in javadocs.

        val fakeOutputTextureId = 9999
        val fakeOutputSurface = SurfaceTexture(fakeOutputTextureId)
        fakeOutputSurface.setDefaultBufferSize(
            mResult!!.size?.width ?: Size.DEFAULT_WIDTH,
            mResult!!.size?.height ?: Size.DEFAULT_HEIGHT
        )

        // 1. Create an EGL surface
        val core = EglCore(eglContext, EglCore.Companion.FLAG_RECORDABLE)
        val eglSurface: EglSurface = EglWindowSurface(core, fakeOutputSurface)
        eglSurface.makeCurrent()
        val transform = mTextureDrawer!!.textureTransform

        // 2. Apply preview transformations
        surfaceTexture.getTransformMatrix(transform)
        val scaleTranslX = (1f - scaleX) / 2f
        val scaleTranslY = (1f - scaleY) / 2f
        Matrix.translateM(transform, 0, scaleTranslX, scaleTranslY, 0f)
        Matrix.scaleM(transform, 0, scaleX, scaleY, 1f)

        // 3. Apply rotation and flip
        // If this doesn't work, rotate "rotation" before scaling, like GlCameraPreview does.
        Matrix.translateM(transform, 0, 0.5f, 0.5f, 0f) // Go back to 0,0
        Matrix.rotateM(
            transform, 0, (rotation + mResult!!.rotation).toFloat(), 0f, 0f, 1f
        ) // Rotate to OUTPUT
        Matrix.scaleM(transform, 0, 1f, -1f, 1f) // Vertical flip because we'll use glReadPixels
        Matrix.translateM(transform, 0, -0.5f, -0.5f, 0f) // Go back to old position

        // 4. Do pretty much the same for overlays
        if (mHasOverlay) {
            // 1. First we must draw on the texture and get latest image
            mOverlayDrawer!!.draw(Overlay.Target.PICTURE_SNAPSHOT)

            // 2. Then we can apply the transformations
            Matrix.translateM(mOverlayDrawer!!.transform, 0, 0.5f, 0.5f, 0f)
            Matrix.rotateM(
                mOverlayDrawer!!.transform, 0, mResult!!.rotation.toFloat(), 0f, 0f, 1f
            )
            Matrix.scaleM(
                mOverlayDrawer!!.transform, 0, 1f, -1f, 1f
            ) // Vertical flip because we'll use glReadPixels
            Matrix.translateM(mOverlayDrawer!!.transform, 0, -0.5f, -0.5f, 0f)
        }
        mResult!!.rotation = 0

        // 5. Draw and save
        val timestampUs = surfaceTexture.timestamp / 1000L
        LOG.i("takeFrame:", "timestampUs:", timestampUs)
        mTextureDrawer!!.draw(timestampUs)
        if (mHasOverlay) mOverlayDrawer!!.render(timestampUs)
        mResult!!.data = eglSurface.toByteArray(Bitmap.CompressFormat.JPEG)

        // 6. Cleanup
        eglSurface.release()
        mTextureDrawer!!.release()
        fakeOutputSurface.release()
        if (mHasOverlay) mOverlayDrawer!!.release()
        core.release()
        dispatchResult()
    }

    override fun dispatchResult() {
        mOutputRatio = null
        super.dispatchResult()
    }
}
