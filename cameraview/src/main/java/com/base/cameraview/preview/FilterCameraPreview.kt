package com.base.cameraview.preview

import com.base.cameraview.filter.Filter


/**
 * A preview that support GL filters defined through the [Filter] interface.
 *
 *
 * The preview has the responsibility of calling [Filter.setSize]
 * whenever the preview size changes and as soon as the filter is applied.
 */
interface FilterCameraPreview {
    /**
     * Sets a new filter.
     *
     * @param filter new filter
     */
    fun setFilter(filter: Filter)

    val currentFilter: Filter
}
