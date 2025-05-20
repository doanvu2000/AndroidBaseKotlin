package com.base.cameraview.overlay

import android.graphics.Canvas

/**
 * Base interface for overlays.
 */
interface Overlay {
    /**
     * Called for this overlay to draw itself on the specified target and canvas.
     *
     * @param target target
     * @param canvas target canvas
     */
    fun drawOn(target: Target, canvas: Canvas)

    /**
     * Called to understand if this overlay would like to draw onto the given
     * target or not. If true is returned, [.drawOn] can be
     * called at a future time.
     *
     * @param target the target
     * @return true to draw on it
     */
    fun drawsOn(target: Target): Boolean

    /**
     * Returns true if hardware canvas capture is enabled, false by default
     *
     * @return true if capturing hardware surfaces
     */
    /**
     * Sets the overlay renderer to lock and capture the hardware canvas in order
     * to capture hardware accelerated views such as video players
     *
     * @param on enabled
     */
    var hardwareCanvasEnabled: Boolean

    enum class Target {
        PREVIEW, PICTURE_SNAPSHOT, VIDEO_SNAPSHOT
    }
}
