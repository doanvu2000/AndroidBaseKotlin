package com.example.baseproject.base.utils

import android.annotation.SuppressLint
import android.content.Context
import com.example.baseproject.R
import java.text.SimpleDateFormat
import java.util.*

object CalendarUtil {
    private var calendar = Calendar.getInstance()
    private val listOfMonth = mutableListOf(
        R.string.month_1, R.string.month_2, R.string.month_3,
        R.string.month_4, R.string.month_5, R.string.month_6,
        R.string.month_7, R.string.month_8, R.string.month_9,
        R.string.month_10, R.string.month_11, R.string.month_12
    )

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
                return values().find { it.order == dayOfWeek } ?: MonDay
            }

            fun getDayShortByIndex(dayOfWeek: Int): Int {
                return values().find { it.order == dayOfWeek }?.stringShort ?: MonDay.stringShort
            }
        }
    }

    val listDayOfWeekFull = mutableListOf(
        R.string.monday_full,
        R.string.tuesday_full,
        R.string.wednesday_full,
        R.string.thursday_full,
        R.string.friday_full,
        R.string.saturday_full,
        R.string.sunday_full
    )
    private val mapDayCountOfMonth = mutableMapOf(
        1 to 31, 3 to 31, 4 to 30, 5 to 31, 6 to 30, 7 to 31, 8 to 31, 9 to 30, 10 to 31, 11 to 30, 12 to 31
    )

    fun getListMonthString(context: Context) = listOfMonth.map { context.getString(it) }
    fun getYearInt() = calendar.get(Calendar.YEAR)
    fun getMonthInt() = calendar.get(Calendar.MONTH)
    fun getDayInt() = calendar.get(Calendar.DAY_OF_MONTH)
    fun getCurrentHour(): Int {
        return calendar.get(Calendar.HOUR_OF_DAY)
    }

    fun getCurrentMinutes(): Int {
        return calendar.get(Calendar.MINUTE)
    }

    fun getMonthString(context: Context, index: Int): String {
        return context.getString(listOfMonth[index])
    }

    fun getListMonthStrings(context: Context): List<String> {
        return listOfMonth.map { context.getString(it) }
    }

    fun isMonthFuture(context: Context, month: String) =
        getListMonthString(context).indexOf(month) > getMonthInt()

    //1 - sun, 2 - mon, 3 - tus, 4 - wed, 5 - thu, 6 - fri, 7 - sat
    fun getFirstDayOfWeekOfMonth(): Int {
        val calendar = Calendar.getInstance()
        calendar[Calendar.DAY_OF_MONTH] = 1
        //
        return calendar[Calendar.DAY_OF_WEEK]
    }

    /**
     * get number day of the month != 2
     * */
    private fun getDayCountOfMonth(monthNumber: Int) = mapDayCountOfMonth[monthNumber]

    /**
     * return true if day-month-year is today
     * */
    fun checkToday(day: Int, month: Int, year: Int): Boolean {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
        if (day != currentDay) {
            return false
        }
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        if (month != currentMonth) {
            return false
        }
        val currentYear = calendar.get(Calendar.YEAR)
        if (year != currentYear) {
            return false
        }
        return true
    }

    /**
     * return true if day-month-year is the feature
     * */
    fun checkFeatures(day: Int, month: Int, year: Int): Boolean {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        if (year > currentYear) {
            return true
        }
        if (year < currentYear) {
            return false
        }
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        if (month > currentMonth) {
            return true
        }
        if (month < currentMonth) {
            return false
        }
        if (day <= calendar.get(Calendar.DAY_OF_MONTH)) {
            return false
        }
        return true
    }

    /**
     * format hour-minutes, example: 09:23
     * */
    fun formatTime(hour: Int, minutes: Int): String {
        var result = ""
        result += if (hour >= 10) {
            "$hour:"
        } else {
            "0$hour:"
        }
        result += if (minutes >= 10) {
            "$minutes"
        } else {
            "0$minutes"
        }
        return result
    }

    /**
     * format hour-minutes, example: 09:23
     * */
    fun formatTime(min: Int): String {
        var result = ""
        val hour = min / 60
        val minutes = min - hour * 60
        result += if (hour >= 10) {
            "$hour:"
        } else {
            "0$hour:"
        }
        result += if (minutes >= 10) {
            "$minutes"
        } else {
            "0$minutes"
        }
        return result
    }

    /**
     *  return true if (hour1 : minute1) > (hour2 : minute2)
     * */
    fun compareTwoHour(hour1: Int, minute1: Int, hour2: Int, minute2: Int): Boolean {
        if (hour1 > hour2) {
            return true
        }
        if (hour1 < hour2) {
            return false
        }
        return minute1 > minute2
    }

    fun getTimeFormatDay(context: Context, day: Int, month: Int, year: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.MONTH, month)
            set(Calendar.YEAR, year)
        }
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val dayString = context.getString(DayOfWeek.getDayByIndex(dayOfWeek).stringId)
        val monthString = getMonthString(context, calendar.get(Calendar.MONTH))

        return "$dayString, $monthString $day"
    }

    fun getNameOfDay(context: Context, day: Int, month: Int, year: Int): String {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.MONTH, month)
            set(Calendar.YEAR, year)
        }
        val dayIndex = calendar.get(Calendar.DAY_OF_WEEK)
        val dayName = context.getString(DayOfWeek.getDayShortByIndex(dayIndex))
        return if (day < 10) {
            "0$day $dayName"
        } else {
            "$day $dayName"
        }
    }

    fun getTimeTitleMonthYear(context: Context, month: Int, year: Int) = getMonthString(context, month) + " " + year
    fun getNumberDayOfMonth(month: Int, year: Int): Int {
        return when {
            month == 2 && year % 4 == 0 && year % 100 != 0 || year % 400 == 0 -> 29
            month == 2 -> 28
            else -> getDayCountOfMonth(month) ?: 30
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun convertDateToLong(date: String, pattern: String): Long {
        val df = SimpleDateFormat(pattern)
        return df.parse(date)?.time ?: 0
    }

    fun diffTime(startTime: Long, endTime: Long): String {
        val diff: Long = Date(endTime).time - Date(startTime).time
        val numDay = 1000 * 60 * 60 * 24
        val numHour = 1000 * 60 * 60
        val numMinutes = 1000 * 60
        val numSecond = 1000
        val days = diff / numDay
        val hours = (diff - days * numDay) / numHour
        val minutes = (diff - days * numDay - hours * numHour) / numMinutes
        var rs = ""
        if (days > 0) {
            rs += "$days ngày "
        }
        if (hours > 0) {
            rs += "$hours giờ "
        }
        if (minutes > 0) {
            rs += "$minutes phút "
        }
        return rs
    }

}