package com.jin.week_view_event.listener

import android.graphics.RectF
import com.jin.WeekViewEvent

interface EventLongPressListener {
    /**
     * Similar to [EventClickListener] but with a long press.
     *
     * @param event event clicked.
     * @param eventRect view containing the clicked event.
     */
    fun onEventLongPress(event: WeekViewEvent, eventRect: RectF)
}