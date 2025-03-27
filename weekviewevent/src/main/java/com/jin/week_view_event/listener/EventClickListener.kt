package com.jin.week_view_event.listener

import android.graphics.RectF
import com.jin.WeekViewEvent

interface EventClickListener {
    /**
     * Triggered when clicked on one existing event
     *
     * @param event event clicked.
     * @param eventRect view containing the clicked event.
     */
    fun onEventClick(event: WeekViewEvent, eventRect: RectF)
}