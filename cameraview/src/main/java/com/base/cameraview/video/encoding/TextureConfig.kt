package com.base.cameraview.video.encoding

import android.opengl.EGLContext
import com.base.cameraview.overlay.Overlay
import com.base.cameraview.overlay.OverlayDrawer

/**
 * Video configuration to be passed as input to the constructor
 * of a [TextureMediaEncoder].
 */
class TextureConfig : VideoConfig() {
    @JvmField
    var textureId: Int = 0

    @JvmField
    var overlayTarget: Overlay.Target? = null

    @JvmField
    var overlayDrawer: OverlayDrawer? = null

    @JvmField
    var overlayRotation: Int = 0

    @JvmField
    var scaleX: Float = 0f

    @JvmField
    var scaleY: Float = 0f

    @JvmField
    var eglContext: EGLContext? = null

    fun copy(): TextureConfig {
        val copy = TextureConfig()
        copy<TextureConfig?>(copy)
        copy.textureId = this.textureId
        copy.overlayDrawer = this.overlayDrawer
        copy.overlayTarget = this.overlayTarget
        copy.overlayRotation = this.overlayRotation
        copy.scaleX = this.scaleX
        copy.scaleY = this.scaleY
        copy.eglContext = this.eglContext
        return copy
    }

    fun hasOverlay(): Boolean {
        return overlayDrawer != null
    }
}
