package com.jin.week_view_event.listener

import androidx.annotation.ColorInt
import com.jin.WeekViewEvent

interface TextColorPicker {
    @ColorInt
    fun getTextColor(event: WeekViewEvent): Int
}