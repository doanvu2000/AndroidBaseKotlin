package com.base.cameraview.preview

import android.graphics.SurfaceTexture
import com.base.cameraview.filter.Filter

/**
 * Callback for renderer frames.
 */
interface RendererFrameCallback {
    /**
     * Called on the renderer thread, hopefully only once, to notify that
     * the texture was created (or to inform a new callback of the old texture).
     *
     * @param textureId the GL texture linked to the image stream
     */
    @RendererThread
    fun onRendererTextureCreated(textureId: Int)

    /**
     * Called on the renderer thread after each frame was drawn.
     * You are not supposed to hold for too long onto this thread, because
     * well, it is the rendering thread.
     *
     * @param surfaceTexture the texture to get transformation
     * @param rotation       the rotation (to reach REF_VIEW)
     * @param scaleX         the scaleX (in REF_VIEW) value
     * @param scaleY         the scaleY (in REF_VIEW) value
     */
    @RendererThread
    fun onRendererFrame(surfaceTexture: SurfaceTexture, rotation: Int, scaleX: Float, scaleY: Float)

    /**
     * Called when the renderer filter changes. This is guaranteed to be called at least once
     * before the first [.onRendererFrame].
     *
     * @param filter the new filter
     */
    @RendererThread
    fun onRendererFilterChanged(filter: Filter)
}
