package com.example.baseproject.base.utils.extension

import android.widget.ScrollView
import androidx.core.widget.NestedScrollView

//region ScrollView Navigation

/**
 * Scroll ScrollView to top position
 */
fun ScrollView.scrollToTop() {
    this.fullScroll(ScrollView.FOCUS_UP)
}

//endregion

//region NestedScrollView Navigation

/**
 * Scroll NestedScrollView to top position with smooth animation
 */
fun NestedScrollView.scrollToTop() {
    fullScroll(ScrollView.FOCUS_UP)
    smoothScrollTo(0, 0)
}

//endregion

//region NestedScrollView Listeners

/**
 * Listener for when NestedScrollView reaches bottom
 * @param whenLoadToBottom callback when scrolled to bottom
 */
fun NestedScrollView.onLoadToBottomListener(whenLoadToBottom: () -> Unit) {
    val diff: Int = this.getChildAt(this.childCount - 1).bottom - this.height + this.scrollY
    if (diff == 0) {
        whenLoadToBottom.invoke()
    }
}

//endregion
