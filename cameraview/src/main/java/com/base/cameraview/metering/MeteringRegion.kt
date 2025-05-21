package com.base.cameraview.metering

import android.graphics.PointF
import android.graphics.RectF
import com.base.cameraview.size.Size
import kotlin.math.max
import kotlin.math.min

class MeteringRegion(@JvmField val mRegion: RectF, @JvmField val mWeight: Int) :
    Comparable<MeteringRegion?> {
    fun transform(transform: MeteringTransform<*>): MeteringRegion {
        val result = RectF(
            Float.Companion.MAX_VALUE,
            Float.Companion.MAX_VALUE,
            -Float.Companion.MAX_VALUE,
            -Float.Companion.MAX_VALUE
        )
        var point = PointF()
        // top-left
        point.set(mRegion.left, mRegion.top)
        point = transform.transformMeteringPoint(point)
        updateRect(result, point)
        // top-right
        point.set(mRegion.right, mRegion.top)
        point = transform.transformMeteringPoint(point)
        updateRect(result, point)
        // bottom-right
        point.set(mRegion.right, mRegion.bottom)
        point = transform.transformMeteringPoint(point)
        updateRect(result, point)
        // bottom-left
        point.set(mRegion.left, mRegion.bottom)
        point = transform.transformMeteringPoint(point)
        updateRect(result, point)
        return MeteringRegion(result, mWeight)
    }

    private fun updateRect(rect: RectF, point: PointF) {
        rect.left = min(rect.left, point.x)
        rect.top = min(rect.top, point.y)
        rect.right = max(rect.right, point.x)
        rect.bottom = max(rect.bottom, point.y)
    }

    fun clip(bounds: Size): MeteringRegion {
        return clip(
            bounds = RectF(
                0f, 0f, bounds.width.toFloat(), bounds.height.toFloat()
            )
        )
    }

    fun clip(bounds: RectF): MeteringRegion {
        val region = RectF()
        region.set(
            max(bounds.left, mRegion.left),
            max(bounds.top, mRegion.top),
            min(bounds.right, mRegion.right),
            min(bounds.bottom, mRegion.bottom)
        )
        return MeteringRegion(region, mWeight)
    }

    override fun compareTo(other: MeteringRegion?): Int {
        return -mWeight.compareTo(other?.mWeight ?: 0)
    }

    companion object {
        const val MAX_WEIGHT: Int = 1000
    }
}
