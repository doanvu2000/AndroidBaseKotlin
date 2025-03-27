package com.jin

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.Calendar

object DateUtils {
    fun getLocalDateNow(): LocalDate = Calendar.getInstance().toLocalDate()
    fun getLocalTimeNow(): LocalTime = Calendar.getInstance().toLocalTime()
    fun getLocalDateTimeNow(): LocalDateTime = Calendar.getInstance().toLocalDateTime()
}

fun Calendar.toLocalDate(): LocalDate =
    LocalDate.of(get(Calendar.YEAR), get(Calendar.MONTH) + 1, get(Calendar.DAY_OF_MONTH))

fun Calendar.toLocalTime(): LocalTime = LocalTime.of(
    get(Calendar.HOUR_OF_DAY),
    get(Calendar.MINUTE),
    get(Calendar.SECOND),
    get(Calendar.MILLISECOND) * 1000000
)

fun Calendar.toLocalDateTime(): LocalDateTime = LocalDateTime.of(
    get(Calendar.YEAR),
    get(Calendar.MONTH) + 1,
    get(Calendar.DAY_OF_MONTH),
    get(Calendar.HOUR_OF_DAY),
    get(Calendar.MINUTE),
    get(Calendar.SECOND),
    get(Calendar.MILLISECOND) * 1000000
)