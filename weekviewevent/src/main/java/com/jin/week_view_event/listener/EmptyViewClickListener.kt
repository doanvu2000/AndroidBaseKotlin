package com.jin.week_view_event.listener

import com.jin.DayTime

interface EmptyViewClickListener {
    /**
     * Triggered when the users clicks on an empty space of the calendar.
     *
     * @param day [DayTime] object set with the day and time of the clicked position on the view.
     */
    fun onEmptyViewClicked(day: DayTime)
}