package com.jin

import android.content.res.Resources
import android.text.format.DateFormat
import androidx.core.content.res.ResourcesCompat
import com.jin.week_view_event.listener.DayTimeInterpreter
import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale


object WeekViewUtil {
    /**
     * Converts [DayOfWeek] day of week integer value to [java.util.Calendar.DAY_OF_WEEK] integer value.
     *
     * @param dayOfWeek The [DayOfWeek] integer value representing the day of the week.
     * @return Integer value representing Day Of Week per [java.util.Calendar] standards.
     */
    fun dayOfWeekToCalendarDay(dayOfWeek: Int): Int {
        return dayOfWeek % 7 + 1
    }

    /**
     * Converts [java.util.Calendar.DAY_OF_WEEK] integer value to [DayOfWeek] integer value.
     *
     * @param calendarDay The [java.util.Calendar] integer value representing the day of the week.
     * @return The correct [DayOfWeek] integer value.
     */
    fun calendarDayToDayOfWeek(calendarDay: Int): Int {
        return if (calendarDay == 1) 7 else calendarDay - 1
    }

    /**
     * Returns the amount of days between the second date and the first date.
     *
     * @param dateOne The first date
     * @param dateTwo The second date
     * @return The amount of days between dateTwo and dateOne
     */
    fun daysBetween(dateOne: DayOfWeek, dateTwo: DayOfWeek): Int {
        var daysInBetween = 0
        var currentDate = dateOne
        while (currentDate != dateTwo) {
            daysInBetween++
            currentDate = currentDate.plus(1)
        }
        return daysInBetween
    }

    /**
     * Returns the amount of minutes in the given hours and minutes.
     *
     * @param hour Number of hours
     * @param minute Number of minutes
     * @return Amount of minutes in the given hours and minutes
     */
    fun getPassedMinutesInDay(hour: Int, minute: Int): Int {
        return hour * 60 + minute
    }

    /**
     * Returns the amount of minutes passed in the day before the time in the given date.
     *
     * @param date The DayTime object
     * @return Amount of minutes in day before time
     */
    fun getPassedMinutesInDay(date: DayTime): Int {
        return getPassedMinutesInDay(date.hour, date.minute)
    }
}

fun WeekView.createDayTimeInterpreter() = object : DayTimeInterpreter {
    override fun interpretDay(day: Int): String {
        val dayOfWeek = DayOfWeek.of(day)
        return if (numberOfVisibleDays > 3) dayOfWeek.getDisplayName(
            TextStyle.SHORT, Locale.getDefault()
        )
        else dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    override fun interpretTime(hour: Int, minutes: Int): String {
        val time: LocalTime = LocalTime.of(hour, minutes)
        return time.format(
            if (DateFormat.is24HourFormat(context)) DateTimeFormatter.ofPattern(
                "H"
            ) else DateTimeFormatter.ofPattern("ha")
        )
    }
}

fun Resources.getDrawableById(id: Int, theme: Resources.Theme? = null) =
    ResourcesCompat.getDrawable(this, id, theme)