package com.base.cameraview.filter

import android.content.res.TypedArray
import com.base.cameraview.R

/**
 * Parses filters from XML attributes.
 */
class FilterParser(array: TypedArray) {
    private var filter: Filter? = null

    init {
        val filterName = array.getString(R.styleable.CameraView_cameraFilter)
        try {
            val filterClass = Class.forName(filterName)
            filter = filterClass.newInstance() as Filter
        } catch (ignore: Exception) {
            filter = NoFilter()
        }
    }

    fun getFilter(): Filter {
        return filter!!
    }
}
