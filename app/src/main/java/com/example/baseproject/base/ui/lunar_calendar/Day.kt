package com.example.baseproject.base.ui.lunar_calendar

import android.content.Context
import com.example.baseproject.base.utils.extension.getDayOfWeek
import com.example.baseproject.base.utils.extension.getMonth
import com.example.baseproject.base.utils.extension.getYear
import org.apache.commons.lang3.time.DateUtils
import java.util.Calendar
import java.util.Date
import kotlin.properties.Delegates

data class Day(
    var calendar: Calendar,
    var state: DayState,
    var isToday: Boolean,
    var isSelected: Boolean
)

enum class DayState {
    PreviousMonth,
    ThisMonth,
    NextMonth
}

abstract class CalendarCellAdapter :
    androidx.recyclerview.widget.RecyclerView.Adapter<androidx.recyclerview.widget.RecyclerView.ViewHolder> {
    private val context: Context
    private val calendar: Calendar
    private val weekOfMonth: Int
    private val startDate: Calendar

    var items: List<Day> by Delegates.observable(emptyList()) { _, old, new ->
        CalendarDiff(old, new).calculateDiff().dispatchUpdatesTo(this)
    }

    constructor(context: Context, date: Date, preselectedDay: Date? = null) : this(
        context,
        Calendar.getInstance().apply { time = date },
        CalendarPagerAdapter.DayOfWeek.Sunday,
        preselectedDay
    )

    constructor(
        context: Context,
        calendar: Calendar,
        startingAt: CalendarPagerAdapter.DayOfWeek,
        preselectedDay: Date? = null
    ) : super() {
        this.context = context
        this.calendar = calendar

        val start = DateUtils.truncate(calendar, Calendar.DAY_OF_MONTH)
        if (start.getDayOfWeek() != (startingAt.getDifference() + 2)) {
            start.set(
                Calendar.DAY_OF_MONTH,
                if (startingAt.isLessFirstWeek(calendar)) -startingAt.getDifference() else 0
            )
            start.add(
                Calendar.DAY_OF_MONTH,
                -start.getDayOfWeek() + 2 + startingAt.getDifference()
            )
        }
        startDate = start
        this.weekOfMonth =
            calendar.getActualMaximum(Calendar.WEEK_OF_MONTH) + (if (startingAt.isLessFirstWeek(
                    calendar
                )
            ) 1 else 0) - (if (startingAt.isMoreLastWeek(
                    calendar
                )
            ) 1 else 0)

        updateItems(preselectedDay)
    }

    fun updateItems(selectedDate: Date? = null) {
        val now = Calendar.getInstance()

        this.items = (0..itemCount).map {
            val calendarStart = Calendar.getInstance().apply { time = startDate.time }
            calendarStart.add(Calendar.DAY_OF_MONTH, it)

            val thisTime = calendar.getYear() * 12 + (calendar.getMonth() - 1)
            val compareTime = calendarStart.getYear() * 12 + (calendarStart.getMonth() - 1)

            val state = when (thisTime.compareTo(compareTime)) {
                -1 -> DayState.NextMonth
                0 -> DayState.ThisMonth
                1 -> DayState.PreviousMonth
                else -> throw IllegalStateException()
            }
            val isSelected = when (selectedDate) {
                null -> false
                else -> DateUtils.isSameDay(calendarStart.time, selectedDate)
            }
            val isToday = DateUtils.isSameDay(calendarStart, now)

            Day(calendarStart, state, isToday, isSelected)
        }
    }

    override fun onBindViewHolder(
        holder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        position: Int
    ) {
        onBindViewHolder(holder, items[holder.layoutPosition], position)
    }

    override fun getItemCount(): Int = 7 * weekOfMonth

    abstract fun onBindViewHolder(
        holder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
        day: Day,
        selectedPosition: Int
    )
}