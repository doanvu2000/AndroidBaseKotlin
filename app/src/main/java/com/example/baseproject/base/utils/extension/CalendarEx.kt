package com.example.baseproject.base.utils.extension

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

//get title of current day
fun Calendar.getDayTitle() = dayTitles[this[Calendar.DAY_OF_WEEK]].toString()

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
//endregion

//region month
fun Calendar.getMonth() = this[Calendar.MONTH] + 1
//endregion

//region year
fun Calendar.getYear() = this[Calendar.YEAR]
//endregion