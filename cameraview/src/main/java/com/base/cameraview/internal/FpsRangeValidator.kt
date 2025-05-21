package com.base.cameraview.internal

import android.os.Build
import android.util.Range
import com.base.cameraview.CameraLogger

object FpsRangeValidator {
    private val LOG: CameraLogger = CameraLogger.create("FpsRangeValidator")
    private val sIssues: MutableMap<String?, MutableList<Range<Int>?>?> =
        HashMap()

    init {
        sIssues.put("Google Pixel 4", mutableListOf<Range<Int>?>(Range<Int>(15, 60)))
        sIssues.put("Google Pixel 4a", mutableListOf<Range<Int>?>(Range<Int>(15, 60)))
        sIssues.put("Google Pixel 4 XL", mutableListOf<Range<Int>?>(Range<Int>(15, 60)))
    }

    fun validate(range: Range<Int>?): Boolean {
        LOG.i(
            "Build.MODEL:",
            Build.MODEL,
            "Build.BRAND:",
            Build.BRAND,
            "Build.MANUFACTURER:",
            Build.MANUFACTURER
        )
        val descriptor = Build.MANUFACTURER + " " + Build.MODEL
        val ranges = sIssues[descriptor]
        if (ranges != null && ranges.contains(range)) {
            LOG.i("Dropping range:", range)
            return false
        }
        return true
    }
}
