package com.base.cameraview.preview

import android.content.Context
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.view.LayoutInflater
import android.view.TextureView
import android.view.TextureView.SurfaceTextureListener
import android.view.View
import android.view.ViewGroup
import com.base.cameraview.R
import com.base.cameraview.size.AspectRatio
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.ExecutionException

/**
 * A preview implementation based on [TextureView].
 * Better than [SurfaceCameraPreview] but much less powerful than [GlCameraPreview].
 */
class TextureCameraPreview(context: Context, parent: ViewGroup) :
    CameraPreview<TextureView?, SurfaceTexture?>(context, parent) {
    private var mRootView: View? = null

    override fun onCreateView(context: Context, parent: ViewGroup): TextureView {
        val root = LayoutInflater.from(context).inflate(
            R.layout.cameraview_texture_view, parent,
            false
        )
        parent.addView(root, 0)
        val texture = root.findViewById<TextureView>(R.id.texture_view)
        texture.surfaceTextureListener = object : SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                dispatchOnSurfaceAvailable(width, height)
            }

            override fun onSurfaceTextureSizeChanged(p0: SurfaceTexture, width: Int, height: Int) {
                dispatchOnSurfaceSizeChanged(width, height)
            }

            override fun onSurfaceTextureDestroyed(p0: SurfaceTexture): Boolean {
                dispatchOnSurfaceDestroyed()
                return true
            }

            override fun onSurfaceTextureUpdated(p0: SurfaceTexture) {
            }
        }
        mRootView = root
        return texture
    }

    override fun getRootView(): View {
        return mRootView!!
    }

    override fun getOutputClass(): Class<SurfaceTexture?> {
        return SurfaceTexture::class.java as Class<SurfaceTexture?>
    }

    override fun getOutput(): SurfaceTexture {
        return view.surfaceTexture!!
    }

    override fun supportsCropping(): Boolean {
        return true
    }

    override fun crop(callback: CropCallback?) {
        view.post(object : Runnable {
            override fun run() {
                if (mInputStreamHeight == 0 || mInputStreamWidth == 0 || mOutputSurfaceHeight == 0 || mOutputSurfaceWidth == 0) {
                    callback?.onCrop()
                    return
                }
                var scaleX = 1f
                var scaleY = 1f
                val current = AspectRatio.of(mOutputSurfaceWidth, mOutputSurfaceHeight)
                val target = AspectRatio.of(mInputStreamWidth, mInputStreamHeight)
                if (current.toFloat() >= target.toFloat()) {
                    // We are too short. Must increase height.
                    scaleY = current.toFloat() / target.toFloat()
                } else {
                    // We must increase width.
                    scaleX = target.toFloat() / current.toFloat()
                }

                view.scaleX = scaleX
                view.scaleY = scaleY

                mCropping = scaleX > 1.02f || scaleY > 1.02f
                LOG.i("crop:", "applied scaleX=", scaleX)
                LOG.i("crop:", "applied scaleY=", scaleY)
                callback?.onCrop()
            }
        })
    }

    override fun setDrawRotation(drawRotation: Int) {
        super.setDrawRotation(drawRotation)
        val task = TaskCompletionSource<Void?>()
        view.post {
            val matrix = Matrix()
            // Output surface coordinates
            val outputCenterX = mOutputSurfaceWidth / 2f
            val outputCenterY = mOutputSurfaceHeight / 2f
            val flip = drawRotation % 180 != 0
            // If dimensions are swapped, we must also do extra work to flip
            // the two dimensions, using the view width and height (to support cropping).
            if (flip) {
                val scaleX = mOutputSurfaceHeight.toFloat() / mOutputSurfaceWidth
                matrix.postScale(scaleX, 1f / scaleX, outputCenterX, outputCenterY)
            }
            matrix.postRotate(drawRotation.toFloat(), outputCenterX, outputCenterY)
            view.setTransform(matrix)
            task.setResult(null)
        }
        try {
            Tasks.await<Void?>(task.getTask())
        } catch (ignore: InterruptedException) {
        } catch (ignore: ExecutionException) {
        }
    }
}
