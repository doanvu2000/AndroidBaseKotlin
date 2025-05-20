package com.base.cameraview.preview

/**
 * Base interface for previews that support renderer frame callbacks,
 * see [RendererFrameCallback].
 */
interface RendererCameraPreview {
    /**
     * Adds a [RendererFrameCallback] to receive renderer frame events.
     *
     * @param callback a callback
     */
    fun addRendererFrameCallback(callback: RendererFrameCallback)

    /**
     * Removes a [RendererFrameCallback] that was previously added to receive renderer
     * frame events.
     *
     * @param callback a callback
     */
    fun removeRendererFrameCallback(callback: RendererFrameCallback)
}
