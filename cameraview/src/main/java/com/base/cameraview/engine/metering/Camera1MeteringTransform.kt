package com.base.cameraview.engine.metering

import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.hardware.Camera
import com.base.cameraview.CameraLogger
import com.base.cameraview.engine.offset.Angles
import com.base.cameraview.engine.offset.Axis
import com.base.cameraview.engine.offset.Reference
import com.base.cameraview.metering.MeteringTransform
import com.base.cameraview.size.Size
import kotlin.math.cos
import kotlin.math.sin

class Camera1MeteringTransform(angles: Angles, private val previewSize: Size) :
    MeteringTransform<Camera.Area?> {
    private val displayToSensor: Int =
        -angles.offset(Reference.SENSOR, Reference.VIEW, Axis.ABSOLUTE)

    override fun transformMeteringPoint(point: PointF): PointF {
        // First, rescale to the -1000 ... 1000 range.
        val scaled = PointF()
        scaled.x = -1000f + (point.x / previewSize.width) * 2000f
        scaled.y = -1000f + (point.y / previewSize.height) * 2000f

        // Apply rotation to this point.
        // https://academo.org/demos/rotation-about-point/
        val rotated = PointF()
        val theta = (displayToSensor.toDouble()) * Math.PI / 180
        rotated.x = (scaled.x * cos(theta) - scaled.y * sin(theta)).toFloat()
        rotated.y = (scaled.x * sin(theta) + scaled.y * cos(theta)).toFloat()
        LOG.i("scaled:", scaled, "rotated:", rotated)
        return rotated
    }

    override fun transformMeteringRegion(region: RectF, weight: Int): Camera.Area {
        val rect = Rect()
        region.round(rect)
        return Camera.Area(rect, weight)
    }

    companion object {
        private val TAG: String = Camera1MeteringTransform::class.java.simpleName
        private val LOG: CameraLogger = CameraLogger.create(TAG)
    }
}
