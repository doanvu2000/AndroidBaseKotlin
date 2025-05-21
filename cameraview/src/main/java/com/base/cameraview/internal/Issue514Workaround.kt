package com.base.cameraview.internal

import android.opengl.GLES11Ext
import android.opengl.GLES20

/**
 * Fixes an issue for some devices with snapshot picture and video recording.
 * This is so unclear that I wanted to have a separate class holding code and comments.
 *
 *
 * WHEN TO USE THIS CLASS
 * There is actually no need of this class in some cases:
 * - when we don't have overlays, everything works
 * - on the majority of devices, everything works
 * But some devices will show the issue #514 and so they need this class to fix it.
 * We will use this always since it should have close to none performance impact.
 *
 *
 * SNAPSHOT PROCEDURE
 * The issue is about picture and video snapshots with overlays. In both cases, we:
 * 1. Take textureId from the camera preview
 * 3. Create an overlayTextureId
 * 4. Create an overlaySurfaceTexture
 * 5. Create an overlaySurface
 * 6. Move to another thread
 * 7. Create a new EGLContext using the old context as a shared context so we have texture data
 * 8. Create a new EGLWindow using some surface as output
 * 9. For each frame:
 * 9A. Draw overlays on the overlaySurface.lockCanvas() / unlockCanvasAndPost()
 * 9B. Publish overlays to GL texture using overlaySurfaceTexture.updateTexImage()
 * 9C. GLES - draw textureId
 * 9D. GLES - draw overlayTextureId
 * Both textures are drawn on the same EGLWindow and we manage to overlay them with
 * [GLES20.GL_BLEND]. This is the whole procedure and it works for the majority of
 * devices and situations.
 *
 *
 * ISSUE DESCRIPTION
 * The #514 issue can be described as follows:
 * - Overlays have no transparency: background is [android.graphics.Color.BLACK] and covers the video
 * - Overlays have distorted colors: [android.graphics.Color.RED] becomes greenish,
 * [android.graphics.Color.GREEN] becomes blueish,
 * [android.graphics.Color.BLUE] becomes reddish
 *
 *
 * ISSUE INSIGHTS
 * After painful debugging, we have reached these conclusions:
 * 1. Overlays are drawn on [android.graphics.Canvas] with the correct format
 * This can be checked for example by applying alpha to one overlay. The final color will
 * be faded out, although on a black background. So the [android.graphics.Canvas] drawing step works well.
 * 2. The GLES shader will always receive pixels in RGBA
 * This seems to be a constant in Android - someone does the conversion for us at a lower level.
 * This was confirmed for example by forcing A=0.5 and seeing the video frames behind the overlay
 * black background, or by forcing to 0.0 some of the channels and seeing the output.
 * 3. The [android.graphics.Canvas] / [android.view.Surface] pixels are wrongly treated as YUV!
 * On problematic devices, some component down there thinks that our overlays RGBA are in YUV,
 * and will CONVERT THEM TO RGBA. This means:
 * 3A. Original alpha is dropped. The algorithm thinks we have passed YUV.
 * 3B. Original colors are messed up. For example, (255,0,0,255,RGBA) is treated as (255,0,0,YUV)
 * and converted back to rgb becoming greenish (74,255,27,255,RGBA).
 * Doing the same conversion for [android.graphics.Color.GREEN] and [android.graphics.Color.BLUE] confirms what we
 * were seeing in the issue screenshots.
 *
 *
 * So a pixel format conversion takes place, when it shouldn't happen. We can't solve this:
 * - It is done at a lower level, there's no real way for us to specify the surface format, but
 * it seems that these devices will prefer a YUV format and misunderstand our [android.graphics.Canvas]
 * pixels.
 * - There is also no way to identify which devices will present this issue, it's a bug somewhere
 * and it is implementation specific.
 *
 *
 * THE MAGIC
 * Hard to say why, but using this class fixes the described issue.
 * It seems that when the [android.graphics.SurfaceTexture.updateTexImage] method for the overlay surface
 * is called - the one that updates the overlayTextureId - we must ensure that the CURRENTLY
 * BOUND TEXTURE ID IS NOT 0. The id we choose to apply might be cameraTextureId, or
 * overlayTextureId, or probably whatever other valid id, and should be passed to
 * [.Issue514Workaround].
 * [Tested with cameraTextureId and overlayTextureId: both do work.]
 * [Tested with invalid id like 9999. This won't work.]
 *
 *
 * This makes no sense, since overlaySurfaceTexture.updateTexImage() is setting it to
 * overlayTextureId anyway, but it fixes the issue. Specifically, after any draw operation with
 * - [.beforeOverlayUpdateTexImage] to be called before the
 * [android.graphics.SurfaceTexture.updateTexImage] call
 * - [.end] to release and bring things back to normal state
 *
 *
 * Since updating and rendering can happen on different threads with a shared EGL context,
 * in case they do, the [.beforeOverlayUpdateTexImage], the actual updateTexImage() and
 * finally the [GlTextureDrawer] drawing operations should be synchronized with a lock.
 *
 *
 * REFERENCES
 * [...](https://github.com/natario1/CameraView/issues/514)
 * [...](https://android.googlesource.com/platform/frameworks/native/+/5c1139f/libs/gui/SurfaceTexture.cpp)
 * I can see here that SurfaceTexture does indeed call glBindTexture with the same parameters
 * whenever updateTexImage is called, but it also does other gl stuff first. This other gl stuff
 * might be breaking when we don't have a bound texture on some specific hardware implementation.
 */
class Issue514Workaround(private val textureId: Int) {
    fun beforeOverlayUpdateTexImage() {
        bindTexture(textureId)
    }

    fun end() {
        bindTexture(0)
    }

    private fun bindTexture(textureId: Int) {
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId)
    }
}
