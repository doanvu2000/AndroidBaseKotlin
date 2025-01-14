package com.example.baseproject.base.utils.extension

import com.example.baseproject.base.utils.lunar.calendar.LunisolarCalendar
import java.util.Calendar


//region hour
fun Calendar.getGioHoangDao() = LunisolarCalendar.getGioHoangDao(
    getDay(), getMonth(), getYear()
).toList()

private fun Calendar.getChiForHour() = getHourFloat().getChiOfHour()

private fun Calendar.getCanForHour(): String {
    val canOfDay = getCanForDay()
    val chiOfHour = getChiForHour()
    return getCanOfHour(canOfDay, chiOfHour)
}

private fun Int.getCanOfHour(canOfDay: String): String {
    val chiOfHour = getChiOfHour()
    return getCanOfHour(canOfDay, chiOfHour)
}

private fun Float.getChiOfHour(): String {
    return LunisolarCalendar.getTimeName(this)
}

private fun Float.getCanOfHour(canOfDay: String): String {
    val chiOfHour = getChiOfHour()
    return getCanOfHour(canOfDay, chiOfHour)
}

private fun Int.getChiOfHour(): String {
    return LunisolarCalendar.getTimeName(this.toFloat())
}

fun Float.getCanChiOfHour(canOfDay: String): String {
    return "${getCanOfHour(canOfDay)} ${getChiOfHour()}"
}

fun Int.getCanChiOfHour(canOfDay: String): String {
    return "${getCanOfHour(canOfDay)} ${getChiOfHour()}"
}

private fun getCanOfHour(canOfDay: String, chiOfHour: String): String {
    var canOfHour = ""

    val chi =
        listOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tị", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")
    when (canOfDay) {
        "Giáp", "Kỷ" -> {
            val can = listOf(
                "Giáp",
                "Ất",
                "Bính",
                "Đinh",
                "Mậu",
                "Kỷ",
                "Canh",
                "Tân",
                "Nhâm",
                "Quý",
                "Giáp",
                "Ất"
            )
            canOfHour = can[chi.indexOf(chiOfHour)]
        }

        "Ất", "Canh" -> {
            val can = listOf(
                "Bính",
                "Đinh",
                "Mậu",
                "Kỷ",
                "Canh",
                "Tân",
                "Nhâm",
                "Quý",
                "Giáp",
                "Ất",
                "Bính",
                "Đinh"
            )
            canOfHour = can[chi.indexOf(chiOfHour)]
        }

        "Bính", "Tân" -> {
            val can = listOf(
                "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý", "Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ"
            )
            canOfHour = can[chi.indexOf(chiOfHour)]
        }

        "Đinh", "Nhâm" -> {
            val can = listOf(
                "Canh",
                "Tân",
                "Nhâm",
                "Quý",
                "Giáp",
                "Ất",
                "Bính",
                "Đinh",
                "Mậu",
                "Kỷ",
                "Canh",
                "Tân"
            )
            canOfHour = can[chi.indexOf(chiOfHour)]
        }

        "Mậu", "Quý" -> {
            val can = listOf(
                "Nhâm",
                "Quý",
                "Giáp",
                "Ất",
                "Bính",
                "Đinh",
                "Mậu",
                "Kỷ",
                "Canh",
                "Tân",
                "Nhâm",
                "Quý"
            )
            canOfHour = can[chi.indexOf(chiOfHour)]
        }
    }

    return canOfHour
}

fun Calendar.getCanChiForHour() = "${getCanForHour()} ${getChiForHour()}"
//endregion

//region day
enum class DayStatusEnum(val value: Int) {
    NORMAL_DAY(0),
    GOOD_DAY(1),
    BAD_DAY(-1);

    companion object {
        fun getStatus(status: Int) = entries.find { it.value == status } ?: NORMAL_DAY
    }
}

private fun Calendar.getDayStatusInt(): Int {
    val mConvertSolarToLunar = LunisolarCalendar()
    val day = getDay()
    val month = getMonth()
    val year = getYear()
    mConvertSolarToLunar.solar2lunar(day, month, year)

    val dayStatus = mConvertSolarToLunar.getGoodDay(day, month, year)
    return dayStatus
}

fun Calendar.getDayStatus(): DayStatusEnum {
    val status = getDayStatusInt()
    return DayStatusEnum.getStatus(status)
}

fun Calendar.getLunarDay(): Int {
    val mConvertSolarToLunar = LunisolarCalendar()
    mConvertSolarToLunar.solar2lunar(getDay(), getMonth(), getYear())
    return mConvertSolarToLunar.day
}

fun Calendar.getCanForDay(): String = LunisolarCalendar.getCanForDay(
    getDay(), getMonth(), getYear()
)

fun Calendar.getChiForDay(): String = LunisolarCalendar.getChiForDay(
    getDay(), getMonth(), getYear()
)

fun Calendar.getCanChiForDay() = "${getCanForDay()} ${getChiForDay()}"
//endregion

//region month
fun Calendar.getLunarMonth(): Int {
    val mConvertSolarToLunar = LunisolarCalendar()
    mConvertSolarToLunar.solar2lunar(getDay(), getMonth(), getYear())
    return mConvertSolarToLunar.month
}
//endregion

//region year
fun Calendar.getLunarYear(): Int {
    val mConvertSolarToLunar = LunisolarCalendar()
    mConvertSolarToLunar.solar2lunar(getDay(), getMonth(), getYear())
    return mConvertSolarToLunar.year
}
//endregion