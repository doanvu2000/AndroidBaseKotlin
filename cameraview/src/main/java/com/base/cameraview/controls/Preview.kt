package com.base.cameraview.controls

/**
 * The preview engine to be used.
 */
enum class Preview(val value: Int) : Control {
    /**
     * Preview engine based on [android.view.SurfaceView].
     * Not recommended.
     */
    SURFACE(0),

    /**
     * Preview engine based on [android.view.TextureView].
     * Stable, but does not support all features (like video snapshots,
     * or picture snapshot while taking videos).
     */
    TEXTURE(1),

    /**
     * Preview engine based on [android.opengl.GLSurfaceView].
     * This is the best engine available. Supports video snapshots,
     * supports picture snapshots while taking videos, supports
     * watermarks and overlays, supports real-time filters.
     */
    GL_SURFACE(2);

    companion object {
        @JvmField
        val DEFAULT: Preview = GL_SURFACE

        @JvmStatic
        fun fromValue(value: Int): Preview {
            val list = Preview.entries.toTypedArray()
            for (action in list) {
                if (action.value == value) {
                    return action
                }
            }
            return DEFAULT
        }
    }
}
