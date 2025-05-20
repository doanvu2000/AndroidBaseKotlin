package com.base.cameraview.metering

import android.graphics.PointF
import android.graphics.RectF

interface MeteringTransform<T> {
    fun transformMeteringPoint(point: PointF): PointF

    fun transformMeteringRegion(region: RectF, weight: Int): T
}
