package com.jin.week_view_event.listener

import com.jin.WeekViewEvent

interface WeekViewLoader {
    /**
     * Load the events within the period
     *
     * @return A list with the events of this period
     */
    fun onWeekViewLoad(): MutableList<WeekViewEvent>
}