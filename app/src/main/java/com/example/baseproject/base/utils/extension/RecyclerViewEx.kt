package com.example.baseproject.base.utils.extension

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

//region Layout Manager Setup

/**
 * Set GridLayoutManager cho RecyclerView
 * @param mContext context
 * @param spanCount số cột
 * @param holderAdapter adapter
 * @param isHorizontalOrientation có phải horizontal orientation không
 */
fun RecyclerView.setGridManager(
    mContext: Context,
    spanCount: Int,
    holderAdapter: RecyclerView.Adapter<*>,
    isHorizontalOrientation: Boolean = false
) {
    val orientation =
        if (isHorizontalOrientation) RecyclerView.HORIZONTAL else RecyclerView.VERTICAL
    val manager = GridLayoutManager(mContext, spanCount, orientation, false)
    this.apply {
        layoutManager = manager
        adapter = holderAdapter
    }
}

/**
 * Set LinearLayoutManager horizontal cho RecyclerView
 */
fun RecyclerView.setLinearLayoutHorizontal(
    context: Context,
    holderAdapter: RecyclerView.Adapter<*>
) {
    setLinearLayoutManager(context, holderAdapter, true)
}

/**
 * Set LinearLayoutManager cho RecyclerView
 * @param context context
 * @param holderAdapter adapter
 * @param isHorizontalOrientation có phải horizontal orientation không
 */
fun RecyclerView.setLinearLayoutManager(
    context: Context,
    holderAdapter: RecyclerView.Adapter<*>,
    isHorizontalOrientation: Boolean = false
) {
    val orientation =
        if (isHorizontalOrientation) RecyclerView.HORIZONTAL else RecyclerView.VERTICAL
    val manager = LinearLayoutManager(context, orientation, false)
    this.apply {
        layoutManager = manager
        adapter = holderAdapter
    }
}

/**
 * Set StaggeredGridLayoutManager cho RecyclerView
 * @param spanCount số cột
 * @param holderAdapter adapter
 * @param isHorizontalOrientation có phải horizontal orientation không
 */
fun RecyclerView.setStaggeredGridManager(
    spanCount: Int,
    holderAdapter: RecyclerView.Adapter<*>,
    isHorizontalOrientation: Boolean = false
) {
    val orientation = if (isHorizontalOrientation) {
        StaggeredGridLayoutManager.HORIZONTAL
    } else {
        StaggeredGridLayoutManager.VERTICAL
    }
    val manager = StaggeredGridLayoutManager(spanCount, orientation)
    this.apply {
        layoutManager = manager
        adapter = holderAdapter
    }
}

//endregion

//region Special Layout Managers

/**
 * Set GridLayoutManager với support cho ads
 * Ads sẽ chiếm full width trong grid
 * @param context context
 * @param spanCount số cột
 * @param holderAdapter adapter
 */
fun RecyclerView.setGridLayoutManagerIncludeAds(
    context: Context,
    spanCount: Int,
    holderAdapter: RecyclerView.Adapter<*>,
) {
    val adsViewType = 1
    this.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    val mLayoutManager = GridLayoutManager(context, spanCount).apply {
        spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (holderAdapter.getItemViewType(position)) {
                    adsViewType -> spanCount  // Ads chiếm full width
                    else -> 1  // Items thường chiếm 1 span
                }
            }
        }
    }
    this.apply {
        layoutManager = mLayoutManager
        adapter = holderAdapter
    }
}

//endregion

//region Scroll Listeners

/**
 * Add scroll listener cho RecyclerView
 * @param action action thực hiện khi scroll state changed
 */
fun RecyclerView.onScrollListener(action: () -> Unit) {
    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            action()
        }
    })
}

///**
// * Tự động ẩn keyboard khi scroll RecyclerView
// */
//fun RecyclerView.hideKeyboardWhenScroll() {
//    onScrollListener {
//        if (isSoftKeyboardVisible()) {
//            hideKeyboard()
//        }
//    }
//}

//endregion

//region Navigation Utilities

/**
 * Scroll về đầu RecyclerView với animation
 */
fun RecyclerView.scrollToTop() {
    this.smoothScrollToPosition(0)
}

//endregion
