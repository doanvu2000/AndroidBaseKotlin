package com.example.baseproject.base.utils.util

import android.annotation.SuppressLint
import android.content.Context
import com.example.baseproject.R
import com.example.baseproject.base.utils.extension.getDay
import com.example.baseproject.base.utils.extension.getDayOfWeek
import com.example.baseproject.base.utils.extension.getHour
import com.example.baseproject.base.utils.extension.getMinutes
import com.example.baseproject.base.utils.extension.getMonth
import com.example.baseproject.base.utils.extension.getYear
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

object CalendarUtil {

    //region Constants & Data

    private var calendar = Calendar.getInstance()

    private val listOfMonth = mutableListOf(
        R.string.month_1, R.string.month_2, R.string.month_3, R.string.month_4,
        R.string.month_5, R.string.month_6, R.string.month_7, R.string.month_8,
        R.string.month_9, R.string.month_10, R.string.month_11, R.string.month_12
    )

    val listDayOfWeekFull = mutableListOf(
        R.string.monday_full, R.string.tuesday_full, R.string.wednesday_full,
        R.string.thursday_full, R.string.friday_full, R.string.saturday_full, R.string.sunday_full
    )

    private val mapDayCountOfMonth = mutableMapOf(
        1 to 31, 3 to 31, 4 to 30, 5 to 31, 6 to 30, 7 to 31,
        8 to 31, 9 to 30, 10 to 31, 11 to 30, 12 to 31
    )

    //endregion

    //region Day of Week Enum

    enum class DayOfWeek(val order: Int, val stringId: Int, val stringShort: Int) {
        SunDay(Calendar.SUNDAY, R.string.sunday_full, R.string.sunday),
        MonDay(Calendar.MONDAY, R.string.monday_full, R.string.monday),
        Tuesday(Calendar.TUESDAY, R.string.tuesday_full, R.string.tuesday),
        Wednesday(Calendar.WEDNESDAY, R.string.wednesday_full, R.string.wednesday),
        Thursday(Calendar.THURSDAY, R.string.thursday_full, R.string.thursday),
        Friday(Calendar.FRIDAY, R.string.friday_full, R.string.friday),
        Saturday(Calendar.SATURDAY, R.string.saturday_full, R.string.saturday);

        companion object {
            fun getDayByIndex(dayOfWeek: Int): DayOfWeek {
                return entries.find { it.order == dayOfWeek } ?: MonDay
            }

            fun getDayShortByIndex(dayOfWeek: Int): Int {
                return entries.find { it.order == dayOfWeek }?.stringShort ?: MonDay.stringShort
            }
        }
    }

    //endregion

    //region Basic Time Functions

    /**
     * Get current timestamp
     */
    fun now(): Long = calendar.timeInMillis

    /**
     * Get current year
     */
    fun getYearInt(): Int = calendar.getYear()

    /**
     * Get current month (0-based)
     */
    fun getMonthInt(): Int = calendar.getMonth() - 1

    /**
     * Get current day
     */
    fun getDayInt(): Int = calendar.getDay()

    /**
     * Get current hour
     */
    fun getCurrentHour(): Int = calendar.getHour()

    /**
     * Get current minutes
     */
    fun getCurrentMinutes(): Int = calendar.getMinutes()

    /**
     * Get current day of week
     */
    fun getDayOfWeek(): DayOfWeek = DayOfWeek.getDayByIndex(calendar.getDayOfWeek())

    //endregion

    //region Month Management

    /**
     * Get list of month strings
     */
    fun getListMonthString(context: Context): List<String> =
        listOfMonth.map { context.getString(it) }

    /**
     * Get month string by index
     */
    fun getMonthString(context: Context, index: Int): String = context.getString(listOfMonth[index])

    /**
     * Get list of month strings (alternative method)
     */
    fun getListMonthStrings(context: Context): List<String> =
        listOfMonth.map { context.getString(it) }

    /**
     * Check if month is in future
     */
    fun isMonthFuture(context: Context, month: String): Boolean =
        getListMonthString(context).indexOf(month) > getMonthInt()

    //endregion

    //region Day of Week Calculations

    /**
     * Get first day of week of current month
     * 1 - sun, 2 - mon, 3 - tus, 4 - wed, 5 - thu, 6 - fri, 7 - sat
     */
    fun getFirstDayOfWeekOfMonth(): Int {
        val cal = Calendar.getInstance()
        cal[Calendar.DAY_OF_MONTH] = 1
        return cal.getDayOfWeek()
    }

    //endregion

    //region Date Validation

    /**
     * Check if given date is today
     */
    fun checkToday(day: Int, month: Int, year: Int): Boolean {
        val cal = Calendar.getInstance()
        return day == cal.getDay() && month == cal.getMonth() && year == cal.getYear()
    }

    /**
     * Check if given date is in the future
     */
    fun checkFeatures(day: Int, month: Int, year: Int): Boolean {
        val cal = Calendar.getInstance()
        val currentYear = cal.getYear()

        when {
            year > currentYear -> return true
            year < currentYear -> return false
        }

        val currentMonth = cal.getMonth()
        when {
            month > currentMonth -> return true
            month < currentMonth -> return false
        }

        return day > cal.getDay()
    }

    //endregion

    //region Time Formatting

    /**
     * Format time to HH:MM format
     * @param hour hour value
     * @param minutes minutes value
     */
    fun formatTime(hour: Int, minutes: Int): String {
        val hourStr = if (hour >= 10) "$hour" else "0$hour"
        val minuteStr = if (minutes >= 10) "$minutes" else "0$minutes"
        return "$hourStr:$minuteStr"
    }

    /**
     * Format minutes to HH:MM format
     * @param min total minutes
     */
    fun formatTime(min: Int): String {
        val hour = min / 60
        val minutes = min - hour * 60
        return formatTime(hour, minutes)
    }

    /**
     * Compare two times
     * @return true if (hour1:minute1) > (hour2:minute2)
     */
    fun compareTwoHour(hour1: Int, minute1: Int, hour2: Int, minute2: Int): Boolean {
        return when {
            hour1 > hour2 -> true
            hour1 < hour2 -> false
            else -> minute1 > minute2
        }
    }

    //endregion

    //region Date Formatting

    /**
     * Get formatted day string
     */
    fun getTimeFormatDay(context: Context, day: Int, month: Int, year: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.MONTH, month)
            set(Calendar.YEAR, year)
        }
        val dayString = context.getString(getDayOfWeek().stringId)
        val monthString = getMonthString(context, cal[Calendar.MONTH])

        return "$dayString, $monthString $day"
    }

    /**
     * Get name of day with date
     */
    fun getNameOfDay(context: Context, day: Int, month: Int, year: Int): String {
        val cal = initCalendar(day = day, month = month, year = year)
        val dayIndex = cal.getDayOfWeek()
        val dayName = context.getString(DayOfWeek.getDayShortByIndex(dayIndex))
        return if (day < 10) "0$day $dayName" else "$day $dayName"
    }

    /**
     * Get title for month and year
     */
    fun getTimeTitleMonthYear(context: Context, month: Int, year: Int): String =
        "${getMonthString(context, month)} $year"

    //endregion

    //region Calendar Utilities

    /**
     * Get number of days in month
     */
    fun getNumberDayOfMonth(month: Int, year: Int): Int {
        return when {
            month == 2 && isLeapYear(year) -> 29
            month == 2 -> 28
            else -> getDayCountOfMonth(month) ?: 30
        }
    }

    /**
     * Check if year is leap year
     */
    private fun isLeapYear(year: Int): Boolean =
        year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)

    /**
     * Get day count of month (excluding February)
     */
    private fun getDayCountOfMonth(monthNumber: Int): Int? = mapDayCountOfMonth[monthNumber]

    /**
     * Initialize calendar with specific values
     */
    private fun initCalendar(
        second: Int? = null,
        minutes: Int? = null,
        hour: Int? = null,
        day: Int? = null,
        month: Int? = null,
        year: Int? = null
    ): Calendar {
        return Calendar.getInstance().apply {
            second?.let { set(Calendar.SECOND, it) }
            minutes?.let { set(Calendar.MINUTE, it) }
            hour?.let { set(Calendar.HOUR_OF_DAY, it) }
            day?.let { set(Calendar.DAY_OF_MONTH, it) }
            month?.let { set(Calendar.MONTH, it) }
            year?.let { set(Calendar.YEAR, it) }
        }
    }

    //endregion

    //region Date Conversion

    /**
     * Convert date string to timestamp
     */
    @SuppressLint("SimpleDateFormat")
    fun convertDateToLong(date: String, pattern: String): Long {
        val df = SimpleDateFormat(pattern)
        return df.parse(date)?.time ?: 0
    }

    /**
     * Calculate time difference between two timestamps
     */
    fun diffTime(startTime: Long, endTime: Long): String {
        val diff: Long = Date(endTime).time - Date(startTime).time
        val numDay = 1000 * 60 * 60 * 24
        val numHour = 1000 * 60 * 60
        val numMinutes = 1000 * 60

        val days = diff / numDay
        val hours = (diff - days * numDay) / numHour
        val minutes = (diff - days * numDay - hours * numHour) / numMinutes

        var result = ""
        if (days > 0) result += "$days ngày "
        if (hours > 0) result += "$hours giờ "
        if (minutes > 0) result += "$minutes phút "

        return result
    }

    //endregion
}