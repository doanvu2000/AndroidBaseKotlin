package com.example.baseproject.base.utils.extension

import com.example.baseproject.base.utils.util.CalendarUtil
import java.util.Calendar

//region Constants và Mappings

/**
 * Map các ngày trong tuần với tên tiếng Việt
 */
val dayTitles = mapOf(
    Calendar.MONDAY to "Thứ Hai",
    Calendar.TUESDAY to "Thứ Ba",
    Calendar.WEDNESDAY to "Thứ Tư",
    Calendar.THURSDAY to "Thứ Năm",
    Calendar.FRIDAY to "Thứ Sáu",
    Calendar.SATURDAY to "Thứ Bảy",
    Calendar.SUNDAY to "Chủ Nhật"
)

//endregion

//region Time Components - Milliseconds

/**
 * Lấy millisecond từ Calendar
 */
fun Calendar.getMilliSeconds(): Int = this[Calendar.MILLISECOND]

//endregion

//region Time Components - Seconds

/**
 * Lấy giây từ Calendar
 */
fun Calendar.getSeconds(): Int = this[Calendar.SECOND]

//endregion

//region Time Components - Minutes

/**
 * Lấy phút từ Calendar
 */
fun Calendar.getMinutes(): Int = this[Calendar.MINUTE]

/**
 * Lấy phút dưới dạng Float (bao gồm cả giây)
 */
fun Calendar.getMinutesFloat(): Float {
    val seconds = getSeconds()
    val minutes = getMinutes()
    return minutes + seconds / 60f
}

//endregion

//region Time Components - Hours

/**
 * Lấy giờ từ Calendar (24h format)
 */
fun Calendar.getHour(): Int = this[Calendar.HOUR_OF_DAY]

/**
 * Lấy giờ dưới dạng Float (bao gồm cả phút)
 */
fun Calendar.getHourFloat(): Float {
    val hour = getHour()
    val minutes = getMinutes()
    return hour + minutes / 60f
}

//endregion

//region Date Components - Days

/**
 * Lấy ngày trong tháng
 */
fun Calendar.getDay(): Int = this[Calendar.DAY_OF_MONTH]

/**
 * Lấy thứ trong tuần
 */
fun Calendar.getDayOfWeek(): Int = this[Calendar.DAY_OF_WEEK]

/**
 * Lấy tên thứ hiện tại
 */
fun Calendar.getDayTitle(): String = dayTitles[getDayOfWeek()] ?: "Unknown"

/**
 * Lấy tên thứ của ngày đầu tiên trong tháng
 */
fun Calendar.getFirstDayTitleInMonth(): String = this.apply {
    set(Calendar.DAY_OF_MONTH, 1)
}.getDayTitle()

/**
 * Lấy tên thứ của ngày đầu tiên trong năm
 */
fun Calendar.getFirstDayTitleInYear(): String = this.apply {
    set(Calendar.DAY_OF_YEAR, 1)
}.getDayTitle()

/**
 * Lấy tên thứ của ngày đầu tiên trong tuần
 */
fun Calendar.getFirstDayTitleInWeek(): String = this.apply {
    set(Calendar.DAY_OF_WEEK, 1)
}.getDayTitle()

//endregion

//region First Day of Week in Month Utilities

/**
 * Lấy ngày đầu tiên của thứ cụ thể trong tháng
 */
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

/**
 * Lấy thứ Hai đầu tiên trong tháng
 */
fun getFirstMondayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int = getFirstDayWeekInMonth(Calendar.MONDAY, month, year)

/**
 * Lấy thứ Ba đầu tiên trong tháng
 */
fun getFirstTuesdayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int = getFirstDayWeekInMonth(Calendar.TUESDAY, month, year)

/**
 * Lấy thứ Tư đầu tiên trong tháng
 */
fun getFirstWednesdayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int = getFirstDayWeekInMonth(Calendar.WEDNESDAY, month, year)

/**
 * Lấy thứ Năm đầu tiên trong tháng
 */
fun getFirstThursdayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int = getFirstDayWeekInMonth(Calendar.THURSDAY, month, year)

/**
 * Lấy thứ Sáu đầu tiên trong tháng
 */
fun getFirstFridayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int = getFirstDayWeekInMonth(Calendar.FRIDAY, month, year)

/**
 * Lấy thứ Bảy đầu tiên trong tháng
 */
fun getFirstSaturdayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int = getFirstDayWeekInMonth(Calendar.SATURDAY, month, year)

/**
 * Lấy Chủ Nhật đầu tiên trong tháng
 */
fun getFirstSundayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int = getFirstDayWeekInMonth(Calendar.SUNDAY, month, year)

//endregion

//region Last Day of Week in Month Utilities

/**
 * Lấy ngày cuối cùng của thứ cụ thể trong tháng
 */
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

/**
 * Lấy thứ Hai cuối cùng trong tháng
 */
fun getLastMondayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int = getLastDayWeekInMonth(Calendar.MONDAY, month, year)

/**
 * Lấy thứ Ba cuối cùng trong tháng
 */
fun getLastTuesdayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int = getLastDayWeekInMonth(Calendar.TUESDAY, month, year)

/**
 * Lấy thứ Tư cuối cùng trong tháng
 */
fun getLastWednesdayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int = getLastDayWeekInMonth(Calendar.WEDNESDAY, month, year)

/**
 * Lấy thứ Năm cuối cùng trong tháng
 */
fun getLastThursdayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int = getLastDayWeekInMonth(Calendar.THURSDAY, month, year)

/**
 * Lấy thứ Sáu cuối cùng trong tháng
 */
fun getLastFridayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int = getLastDayWeekInMonth(Calendar.FRIDAY, month, year)

/**
 * Lấy thứ Bảy cuối cùng trong tháng
 */
fun getLastSaturdayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int = getLastDayWeekInMonth(Calendar.SATURDAY, month, year)

/**
 * Lấy Chủ Nhật cuối cùng trong tháng
 */
fun getLastSundayInMonth(
    month: Int = Calendar.getInstance().getMonth(),
    year: Int = Calendar.getInstance().getYear()
): Int = getLastDayWeekInMonth(Calendar.SUNDAY, month, year)

//endregion

//region Date Components - Months

/**
 * Lấy tháng từ Calendar (1-12)
 */
fun Calendar.getMonth(): Int = this[Calendar.MONTH] + 1

//endregion

//region Date Components - Years

/**
 * Lấy năm từ Calendar
 */
fun Calendar.getYear(): Int = this[Calendar.YEAR]

/**
 * Lấy năm hiện tại
 */
fun getCurrentYear(): Int = Calendar.getInstance()[Calendar.YEAR]

//endregion

//region Weekend and Weekday Utilities

/**
 * Kiểm tra xem có phải cuối tuần không
 */
fun Calendar.isWeekend(): Boolean {
    val today = getDayOfWeek()
    return today == Calendar.SATURDAY || today == Calendar.SUNDAY
}

/**
 * Kiểm tra xem có phải ngày trong tuần không (không phải cuối tuần)
 */
fun Calendar.isInWeek(): Boolean = !isWeekend()

/**
 * Kiểm tra hôm nay có phải cuối tuần không
 */
fun todayIsWeekend(): Boolean = now().toCalendar().isWeekend()

/**
 * Kiểm tra hôm nay có phải ngày trong tuần không
 */
fun todayIsInWeek(): Boolean = now().toCalendar().isInWeek()

//endregion
