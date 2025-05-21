package com.base.cameraview.preview

import android.content.Context
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import com.base.cameraview.CameraLogger.Companion.create
import com.base.cameraview.R

/**
 * This is the fallback preview when hardware acceleration is off, and is the last resort.
 * Currently does not support cropping, which means that
 *
 *
 * Do not use.
 */
class SurfaceCameraPreview(context: Context, parent: ViewGroup) :
    CameraPreview<SurfaceView?, SurfaceHolder?>(context, parent) {
    private var mDispatched = false
    private var mRootView: View? = null

    override fun onCreateView(context: Context, parent: ViewGroup): SurfaceView {
        val root = LayoutInflater.from(context).inflate(
            R.layout.cameraview_surface_view, parent,
            false
        )
        parent.addView(root, 0)
        val surfaceView = root.findViewById<SurfaceView>(R.id.surface_view)
        val holder = surfaceView.holder
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS)
        holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder) {
                // This is too early to call anything.
                // surfaceChanged is guaranteed to be called after, with exact dimensions.
                LOG.i("callback: surfaceCreated.")
            }

            override fun surfaceChanged(p0: SurfaceHolder, format: Int, width: Int, height: Int) {
                LOG.i(
                    "callback:", "surfaceChanged",
                    "w:", width,
                    "h:", height,
                    "dispatched:", mDispatched
                )
                if (!mDispatched) {
                    dispatchOnSurfaceAvailable(width, height)
                    mDispatched = true
                } else {
                    dispatchOnSurfaceSizeChanged(width, height)
                }
            }

            override fun surfaceDestroyed(p0: SurfaceHolder) {
                LOG.i("callback: surfaceDestroyed")
                dispatchOnSurfaceDestroyed()
                mDispatched = false
            }
        })
        mRootView = root
        return surfaceView
    }

    override fun getRootView(): View {
        return mRootView!!
    }

    override fun getOutput(): SurfaceHolder {
        return view.holder
    }

    override fun getOutputClass(): Class<SurfaceHolder?> {
        return SurfaceHolder::class.java as Class<SurfaceHolder?>
    }


    companion object {
        private val LOG = create(SurfaceCameraPreview::class.java.simpleName)
    }
}
