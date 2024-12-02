package com.example.baseproject.base.utils.extension

import android.content.Context
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

fun RecyclerView.setGridManager(
    mContext: Context,
    lin: Int,
    holderAdapter: RecyclerView.Adapter<*>,
    orientation: Int = RecyclerView.VERTICAL,
) {
    val manager = GridLayoutManager(mContext, lin, orientation, false)
    this.apply {
        layoutManager = manager
        adapter = holderAdapter
    }
}

fun RecyclerView.setLinearLayoutHorizontal(
    context: Context, holderAdapter: RecyclerView.Adapter<*>
) {
    setLinearLayoutManager(context, holderAdapter, RecyclerView.HORIZONTAL)
}

fun RecyclerView.setLinearLayoutManager(
    context: Context, holderAdapter: RecyclerView.Adapter<*>, orientation: Int = RecyclerView.VERTICAL
) {
    val manager = LinearLayoutManager(context, orientation, false)
    this.apply {
        layoutManager = manager
        adapter = holderAdapter
    }
}

fun RecyclerView.setStaggeredGridManager(
    lin: Int,
    holderAdapter: RecyclerView.Adapter<*>,
    orientation: Int = StaggeredGridLayoutManager.VERTICAL,
) {
    val manager = StaggeredGridLayoutManager(lin, orientation)
    this.apply {
        layoutManager = manager
        adapter = holderAdapter
    }
}

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
                    adsViewType -> spanCount
                    else -> 1
                }
            }
        }
    }
    this.apply {
        layoutManager = mLayoutManager
        adapter = holderAdapter
    }
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