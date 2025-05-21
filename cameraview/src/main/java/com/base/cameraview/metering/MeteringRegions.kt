package com.base.cameraview.metering

import android.graphics.PointF
import android.graphics.RectF
import androidx.annotation.VisibleForTesting
import com.base.cameraview.size.Size
import kotlin.math.min
import kotlin.math.roundToInt

class MeteringRegions private constructor(@field:VisibleForTesting val mRegions: MutableList<MeteringRegion>) {
    fun transform(transform: MeteringTransform<*>): MeteringRegions {
        val regions: MutableList<MeteringRegion> = ArrayList()
        for (region in mRegions) {
            regions.add(region.transform(transform))
        }
        return MeteringRegions(regions)
    }

    fun <T> get(atMost: Int, transform: MeteringTransform<T?>): MutableList<T?> {
        var atMost = atMost
        val result: MutableList<T?> = ArrayList()
        mRegions.sort()
        for (region in mRegions) {
            result.add(transform.transformMeteringRegion(region.mRegion, region.mWeight))
        }
        atMost = min(atMost, result.size)
        return result.subList(0, atMost)
    }

    companion object {
        @VisibleForTesting
        const val BLUR_FACTOR_WEIGHT: Float = 0.1f
        private const val POINT_AREA = 0.05f
        private const val BLUR_FACTOR_SIZE = 1.5f

        @JvmOverloads
        fun fromPoint(
            bounds: Size,
            point: PointF,
            weight: Int = MeteringRegion.MAX_WEIGHT
        ): MeteringRegions {
            val width: Float = POINT_AREA * bounds.width
            val height: Float = POINT_AREA * bounds.height
            val rectF: RectF = expand(point, width, height)
            return fromArea(bounds, rectF, weight, true)
        }

        @JvmOverloads
        fun fromArea(
            bounds: Size,
            area: RectF,
            weight: Int = MeteringRegion.MAX_WEIGHT,
            blur: Boolean = false
        ): MeteringRegions {
            val regions: MutableList<MeteringRegion> = ArrayList()
            val center = PointF(area.centerX(), area.centerY())
            val width = area.width()
            val height = area.height()
            regions.add(MeteringRegion(area, weight))
            if (blur) {
                val background: RectF = expand(
                    center,
                    BLUR_FACTOR_SIZE * width,
                    BLUR_FACTOR_SIZE * height
                )
                regions.add(
                    MeteringRegion(
                        background,
                        (BLUR_FACTOR_WEIGHT * weight).roundToInt()
                    )
                )
            }
            val clipped: MutableList<MeteringRegion> = ArrayList()
            for (region in regions) {
                clipped.add(region.clip(bounds))
            }
            return MeteringRegions(clipped)
        }

        private fun expand(center: PointF, width: Float, height: Float): RectF {
            return RectF(
                center.x - width / 2f,
                center.y - height / 2f,
                center.x + width / 2f,
                center.y + height / 2f
            )
        }
    }
}
