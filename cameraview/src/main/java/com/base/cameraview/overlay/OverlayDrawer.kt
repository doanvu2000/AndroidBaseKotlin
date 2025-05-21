package com.base.cameraview.overlay

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.SurfaceTexture
import android.opengl.GLES20
import android.view.Surface
import androidx.annotation.VisibleForTesting
import com.base.cameraview.CameraLogger.Companion.create
import com.base.cameraview.internal.GlTextureDrawer
import com.base.cameraview.internal.Issue514Workaround
import com.base.cameraview.size.Size

/**
 * Draws overlays through [Overlay].
 *
 *
 * - Provides a [Canvas] to be passed to the Overlay
 * - Lets the overlay draw there: [.draw]
 * - Renders this into the current EGL window: [.render]
 * - Applies the [Issue514Workaround] the correct way
 *
 *
 * In the future we might want to use a different approach than [GlTextureDrawer],
 * The current approach has some issues, for example see [Issue514Workaround].
 */
class OverlayDrawer(private val mOverlay: Overlay, size: Size) {

    @VisibleForTesting
    var mTextureDrawer: GlTextureDrawer?
    private var mSurfaceTexture: SurfaceTexture?
    private var mSurface: Surface?
    private var mIssue514Workaround: Issue514Workaround?

    init {
        mTextureDrawer = GlTextureDrawer()
        mSurfaceTexture = SurfaceTexture(mTextureDrawer!!.texture.id)
        mSurfaceTexture!!.setDefaultBufferSize(size.width, size.height)
        mSurface = Surface(mSurfaceTexture)
        mIssue514Workaround = Issue514Workaround(mTextureDrawer!!.texture.id)
    }

    /**
     * Should be called to draw the [Overlay] on the given [Overlay.Target].
     * This will provide a working [Canvas] to the overlay and also update the
     * drawn contents to a GL_ES texture.
     *
     * @param target the target
     */
    fun draw(target: Overlay.Target) {
        try {
            val surfaceCanvas: Canvas
            if (mOverlay.hardwareCanvasEnabled) {
                surfaceCanvas = mSurface!!.lockHardwareCanvas()
            } else {
                surfaceCanvas = mSurface!!.lockCanvas(null)
            }
            surfaceCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            mOverlay.drawOn(target, surfaceCanvas)
            mSurface!!.unlockCanvasAndPost(surfaceCanvas)
        } catch (e: Surface.OutOfResourcesException) {
            LOG.w("Got Surface.OutOfResourcesException while drawing video overlays", e)
        }
        synchronized(this) {
            mIssue514Workaround!!.beforeOverlayUpdateTexImage()
            try {
                mSurfaceTexture!!.updateTexImage()
            } catch (e: IllegalStateException) {
                LOG.w("Got IllegalStateException while updating texture contents", e)
            }
        }
        mSurfaceTexture!!.getTransformMatrix(mTextureDrawer!!.textureTransform)
    }

    val transform: FloatArray?
        /**
         * Returns the transform that should be used to render the drawn content.
         * This should be called after [.draw] and can be modified.
         *
         * @return the transform matrix
         */
        get() = mTextureDrawer!!.textureTransform

    /**
     * Renders the drawn content in the current EGL surface, assuming there is one.
     * Should be called after [.draw] and any [.getTransform]
     * modification.
     *
     * @param timestampUs frame timestamp
     */
    fun render(timestampUs: Long) {
        // Enable blending
        // Reference http://www.learnopengles.com/android-lesson-five-an-introduction-to-blending/
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        synchronized(this) {
            mTextureDrawer!!.draw(timestampUs)
        }
    }

    /**
     * Releases resources.
     */
    fun release() {
        mIssue514Workaround?.end()
        mIssue514Workaround = null

        mSurfaceTexture?.release()
        mSurfaceTexture = null

        mSurface?.release()
        mSurface = null

        mTextureDrawer?.release()
        mTextureDrawer = null
    }

    companion object {
        private val TAG: String = OverlayDrawer::class.java.simpleName
        private val LOG = create(TAG)
    }
}
