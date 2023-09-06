package com.example.baseproject.base.utils.extension

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

fun RecyclerView.setGridManager(
    mContext: Context,
    lin: Int,
    adapter: RecyclerView.Adapter<*>,
    orientation: Int = RecyclerView.VERTICAL,
) {
    val manager = GridLayoutManager(mContext, lin)
    manager.orientation = orientation
    this.layoutManager = manager
    this.adapter = adapter
}

fun RecyclerView.setLinearLayoutManager(
    context: Context, adapter: RecyclerView.Adapter<*>, orientation: Int = RecyclerView.VERTICAL
) {
    val manager = LinearLayoutManager(context)
    manager.orientation = orientation
    this.layoutManager = manager
    this.adapter = adapter
}

fun RecyclerView.setGridLayoutManagerIncludeAds(
    context: Context,
    spanCount: Int,
    adapter: RecyclerView.Adapter<*>,
) {
    val adsViewType = 1
    this.overScrollMode = RecyclerView.OVER_SCROLL_NEVER
    val layoutManager = GridLayoutManager(context, spanCount).apply {
        spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return when (adapter.getItemViewType(position)) {
                    adsViewType -> spanCount
                    else -> 1
                }
            }
        }
    }
    this.layoutManager = layoutManager
    this.adapter = adapter
}

fun RecyclerView.onScrollListener(action: () -> Unit) {
    this.addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            action()
        }
    })
}

fun RecyclerView.hideKeyboardWhenScroll() {
    onScrollListener {
        if (isSoftKeyboardVisible()) {
            hideKeyboard()
        }
    }
}

fun RecyclerView.scrollToTop() {
    this.smoothScrollToPosition(0)
}