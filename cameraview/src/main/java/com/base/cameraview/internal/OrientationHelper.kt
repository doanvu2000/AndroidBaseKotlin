package com.base.cameraview.internal

import android.content.Context
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.hardware.display.DisplayManager.DisplayListener
import android.os.Handler
import android.os.Looper
import android.view.OrientationEventListener
import android.view.Surface
import android.view.WindowManager
import androidx.annotation.VisibleForTesting

/**
 * Helps with keeping track of both device orientation (which changes when device is rotated)
 * and the display offset (which depends on the activity orientation wrt the device default
 * orientation).
 *
 *
 * Note: any change in the display offset should restart the camera engine, because it reads
 * from the angles container at startup and computes size based on that. This is tricky because
 * activity behavior can differ:
 *
 *
 * - if activity is locked to some orientation, [.mDisplayOffset] won't change, and
 * The library will work fine.
 *
 *
 * - if the activity is unlocked and does NOT handle orientation changes with android:configChanges,
 * the actual behavior differs depending on the rotation.
 * - the configuration callback is never called, of course.
 * - for 90°/-90° rotations, the activity is recreated. Sometime you get [.mDisplayOffset]
 * callback before destruction, sometimes you don't - in any case it's going to recreate.
 * - for 180°/-180°, the activity is NOT recreated! But we can rely on [.mDisplayOffset]
 * changing with a 180 delta and restart the engine.
 *
 *
 * - lastly, if the activity is unlocked and DOES handle orientation changes with android:configChanges,
 * as it will often be the case in a modern Compose app,
 * - you always get the [.mDisplayOffset] callback
 * - for 90°/-90° rotations, the view also gets the configuration changed callback.
 * - for 180°/-180°, the view won't get it because configuration only cares about portrait vs. landscape.
 *
 *
 * In practice, since we don't control the activity and we can't easily inspect the configChanges
 * flags at runtime, a good solution is to always restart when the display offset changes. We might
 * do useless restarts in one rare scenario (unlocked, no android:configChanges, 90° rotation,
 * display offset callback received before destruction) but that's acceptable.
 *
 *
 * Tried to avoid that by looking at [android.app.Activity.isChangingConfigurations], but it's always
 * false by the time the display offset callback is invoked.
 */
class OrientationHelper(private val mContext: Context, private val mCallback: Callback) {
    @VisibleForTesting
    val mDeviceOrientationListener: OrientationEventListener = object : OrientationEventListener(
        mContext.applicationContext,
        SensorManager.SENSOR_DELAY_NORMAL
    ) {
        override fun onOrientationChanged(orientation: Int) {
            var deviceOrientation = 0
            if (orientation == ORIENTATION_UNKNOWN) {
                deviceOrientation =
                    if (lastDeviceOrientation != -1) lastDeviceOrientation else 0
            } else if (orientation >= 315 || orientation < 45) {
                deviceOrientation = 0
            } else if (orientation < 135) {
                deviceOrientation = 90
            } else if (orientation < 225) {
                deviceOrientation = 180
            } else {
                deviceOrientation = 270
            }

            if (deviceOrientation != lastDeviceOrientation) {
                lastDeviceOrientation = deviceOrientation
                mCallback.onDeviceOrientationChanged(lastDeviceOrientation)
            }
        }
    }

    @VisibleForTesting
    val mDisplayOffsetListener: DisplayListener
    private val mHandler = Handler(Looper.getMainLooper())

    /**
     * Returns the current device orientation.
     *
     * @return device orientation
     */
    var lastDeviceOrientation: Int = -1
        private set

    /**
     * Returns the current display offset.
     *
     * @return display offset
     */
    var lastDisplayOffset: Int = -1
        private set
    private var mEnabled = false

    /**
     * Creates a new orientation helper.
     *
     * @param mContext  a valid context
     * @param mCallback a [Callback]
     */
    init {
        mDisplayOffsetListener = object : DisplayListener {
            override fun onDisplayAdded(displayId: Int) {
            }

            override fun onDisplayRemoved(displayId: Int) {
            }

            override fun onDisplayChanged(displayId: Int) {
                val oldDisplayOffset: Int = lastDisplayOffset
                val newDisplayOffset = findDisplayOffset()
                if (newDisplayOffset != oldDisplayOffset) {
                    lastDisplayOffset = newDisplayOffset
                    mCallback.onDisplayOffsetChanged()
                }
            }
        }
    }

    /**
     * Enables this listener.
     */
    fun enable() {
        if (mEnabled) return
        mEnabled = true
        this.lastDisplayOffset = findDisplayOffset()
        val manager = mContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        // Without the handler, this can crash if called from a thread without a looper
        manager.registerDisplayListener(mDisplayOffsetListener, mHandler)
        mDeviceOrientationListener.enable()
    }

    /**
     * Disables this listener.
     */
    fun disable() {
        if (!mEnabled) return
        mEnabled = false
        mDeviceOrientationListener.disable()
        val manager = mContext.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        manager.unregisterDisplayListener(mDisplayOffsetListener)
        this.lastDisplayOffset = -1
        this.lastDeviceOrientation = -1
    }

    private fun findDisplayOffset(): Int {
        val display = (mContext
            .getSystemService(Context.WINDOW_SERVICE) as WindowManager)
            .defaultDisplay
        return when (display.rotation) {
            Surface.ROTATION_0 -> 0
            Surface.ROTATION_90 -> 90
            Surface.ROTATION_180 -> 180
            Surface.ROTATION_270 -> 270
            else -> 0
        }
    }

    /**
     * Receives callback about the orientation changes.
     */
    interface Callback {
        fun onDeviceOrientationChanged(deviceOrientation: Int)

        fun onDisplayOffsetChanged()
    }
}
