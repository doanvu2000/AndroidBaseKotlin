package com.example.baseproject.base.ui.flex_box_layout

import android.content.Context
import android.view.View
import com.google.android.flexbox.FlexboxLayoutManager

class TwoElementFlexBoxManager(context: Context, private val itemCountPerLine: Int = 2) : FlexboxLayoutManager(context) {
    override fun getFlexItemAt(index: Int): View {
        val item = super.getFlexItemAt(index)

        val params = item.layoutParams as LayoutParams

        params.isWrapBefore = index % itemCountPerLine == 0
        return item
    }
}