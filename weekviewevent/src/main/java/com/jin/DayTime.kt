package com.jin

import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.LocalTime

data class DayTime(
    var time: LocalTime? = null, var day: DayOfWeek? = null
) : Comparable<DayTime> {
    constructor(dayTime: DayTime?) : this(dayTime?.time, dayTime?.day)

    constructor(day: DayOfWeek?, time: LocalTime?) : this() {
        this.time = time
        this.day = day
    }

    constructor(day: DayOfWeek, hour: Int, minute: Int) : this(LocalTime.of(hour, minute), day)

    constructor(day: Int, hour: Int, minute: Int) : this(DayOfWeek.of(day), hour, minute)

    constructor(localDateTime: LocalDateTime) : this() {
        this.day = localDateTime.dayOfWeek
        this.time = localDateTime.toLocalTime()
    }

    val dayValue: Int
        get() = day?.value ?: 0

    val hour: Int
        get() = time?.hour ?: 0

    val minute: Int
        get() = time?.minute ?: 0

//    fun setDay(day: Int) {
//        this.day = DayOfWeek.of(day)
//    }

//    fun setDay(day: DayOfWeek) {
//        this.day = day
//    }

    fun addDays(days: Long) {
        day?.let { this.day = it.plus(days) }
    }

    fun addHours(hours: Int) {
        time?.let { this.time = it.plusHours(hours.toLong()) }
    }

    fun addMinutes(minutes: Int) {
        time?.let { this.time = it.plusMinutes(minutes.toLong()) }
    }

    fun isAfter(otherDayTime: DayTime): Boolean = this > otherDayTime

    fun isBefore(otherDayTime: DayTime): Boolean = this < otherDayTime

    fun isSame(otherDayTime: DayTime): Boolean = this.compareTo(otherDayTime) == 0

    fun setMinimumTime() {
        this.time = LocalTime.MIN
    }

    fun setTime(hour: Int, minute: Int) {
        this.time = LocalTime.of(hour, minute)
    }

    fun subtractMinutes(minutes: Int) {
        time?.let { this.time = it.minusMinutes(minutes.toLong()) }
    }

    val toNumericalUnit: Long
        get() {
            return (time?.toNanoOfDay() ?: 0) + (day?.value ?: 0)
        }

//    override fun toString(): String {
//        val dtf = DateTimeFormatter.ofPattern("K:ha")
//        return "DayTime{" + "time=${time?.format(dtf) ?: "null"}" + ", day=$day" + "}"
//    }

    override fun compareTo(other: DayTime): Int {
        if (this.day == other.day) {
            return this.time?.compareTo(other.time ?: LocalTime.MIN) ?: -1
        }
        return this.day?.compareTo(other.day ?: DayOfWeek.MONDAY) ?: -1
    }
}