package com.example.baseproject.base.utils.extension

import android.widget.ScrollView
import androidx.core.widget.NestedScrollView

fun ScrollView.scrollToTop() {
    this.fullScroll(ScrollView.FOCUS_UP)
}

fun NestedScrollView.scrollToTop() {
    fullScroll(ScrollView.FOCUS_UP)
    smoothScrollTo(0, 0)
}

fun NestedScrollView.onLoadToBottomListener(whenLoadToBottom: () -> Unit) {
    val diff: Int = this.getChildAt(this.childCount - 1).bottom - this.height + this.scrollY
    if (diff == 0) {
        whenLoadToBottom.invoke()
    }
}