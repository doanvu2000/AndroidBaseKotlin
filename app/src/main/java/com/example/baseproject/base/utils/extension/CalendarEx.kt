package com.example.baseproject.base.utils.extension

import com.example.baseproject.base.utils.util.CalendarUtil
import java.util.Calendar

val dayTitles = mapOf(
    Calendar.MONDAY to "Thứ Hai",
    Calendar.TUESDAY to "Thứ Ba",
    Calendar.WEDNESDAY to "Thứ Tư",
    Calendar.THURSDAY to "Thứ Năm",
    Calendar.FRIDAY to "Thứ Sáu",
    Calendar.SATURDAY to "Thứ Bảy",
    Calendar.SUNDAY to "Chủ Nhật"
)

//region milliseconds
fun Calendar.getMilliSeconds() = this[Calendar.MILLISECOND]
//endregion

//region second
fun Calendar.getSeconds() = this[Calendar.SECOND]
//endregion

//region minutes
fun Calendar.getMinutes() = this[Calendar.MINUTE]

fun Calendar.getMinutesFloat(): Float {
    val seconds = getSeconds()
    val minutes = getMinutes()
    return minutes + seconds / 60f
}
//endregion

//region hour
fun Calendar.getHour() = this[Calendar.HOUR_OF_DAY]

fun Calendar.getHourFloat(): Float {
    val hour = getHour()
    val minutes = getMinutes()
    return hour + minutes / 60f
}
//endregion

//region day
//current day
fun Calendar.getDay() = this[Calendar.DAY_OF_MONTH]
fun Calendar.getDayOfWeek() = this[Calendar.DAY_OF_WEEK]

//get title of current day
fun Calendar.getDayTitle() = dayTitles[getDayOfWeek()].toString()

//get day title of first day in month
fun Calendar.getFirstDayTitleInMonth() = this.apply {
    set(Calendar.DAY_OF_MONTH, 1)
}.getDayTitle()

//get day title of first day in year
fun Calendar.getFirstDayTitleInYear() = this.apply {
    set(Calendar.DAY_OF_YEAR, 1)
}.getDayTitle()

//get day title of first day in week
fun Calendar.getFirstDayTitleInWeek() = this.apply {
    set(Calendar.DAY_OF_WEEK, 1)
}.getDayTitle()

/*-----------------get first (day in week) of month-----------------*/
fun getFirstDayWeekInMonth(
    dayOfWeek: Int = Calendar.MONDAY,
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month - 1)  // Calendar months are 0-based
        set(Calendar.DAY_OF_MONTH, 1)
    }

    val mDayOfWeek = calendar.getDayOfWeek()
    if (mDayOfWeek == dayOfWeek) {
        return 1
    }

    calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
    if (calendar.getMonth() != month) {
        calendar.add(Calendar.DAY_OF_MONTH, 7)
    }

    return calendar.getDay()
}

//get first monday of month
fun getFirstMondayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int {
    return getFirstDayWeekInMonth(Calendar.MONDAY, month, year)
}

//get first tuesday of month
fun getFirstTuesdayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int {
    return getFirstDayWeekInMonth(Calendar.TUESDAY, month, year)
}

//get first wednesday of month
fun getFirstWednesdayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int {
    return getFirstDayWeekInMonth(Calendar.WEDNESDAY, month, year)
}

//get first thursday of month
fun getFirstThursdayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int {
    return getFirstDayWeekInMonth(Calendar.THURSDAY, month, year)
}

//get first friday of month
fun getFirstFridayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int {
    return getFirstDayWeekInMonth(Calendar.FRIDAY, month, year)
}

//get first saturday of month
fun getFirstSaturdayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int {
    return getFirstDayWeekInMonth(Calendar.SATURDAY, month, year)
}

//get first sunday of month
fun getFirstSundayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int {
    return getFirstDayWeekInMonth(Calendar.SUNDAY, month, year)
}

/*-----------------get last (day in week) of month-----------------*/
fun getLastDayWeekInMonth(
    dayOfWeek: Int = Calendar.MONDAY,
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int {
    var firstDayOfMonth = getFirstDayWeekInMonth(dayOfWeek, month, year)
    val dayCount = CalendarUtil.getNumberDayOfMonth(month, year)
    while (firstDayOfMonth <= dayCount) {
        firstDayOfMonth += 7
    }
    return firstDayOfMonth - 7
}

//get last monday of month
fun getLastMondayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int {
    return getLastDayWeekInMonth(Calendar.MONDAY, month, year)
}

//get last tuesday of month
fun getLastTuesdayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int {
    return getLastDayWeekInMonth(Calendar.TUESDAY, month, year)
}

//get last wednesday of month
fun getLastWednesdayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int {
    return getLastDayWeekInMonth(Calendar.WEDNESDAY, month, year)
}

//get last thursday of month
fun getLastThursdayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int {
    return getLastDayWeekInMonth(Calendar.THURSDAY, month, year)
}

//get last friday of month
fun getLastFridayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int {
    return getLastDayWeekInMonth(Calendar.FRIDAY, month, year)
}

//get last saturday of month
fun getLastSaturdayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int {
    return getLastDayWeekInMonth(Calendar.SATURDAY, month, year)
}

//get last sunday of month
fun getLastSundayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int {
    return getLastDayWeekInMonth(Calendar.SUNDAY, month, year)
}

//todo: get lunar day
//endregion

//region month
fun Calendar.getMonth() = this[Calendar.MONTH] + 1
//endregion

//region year
fun Calendar.getYear() = this[Calendar.YEAR]
//endregion