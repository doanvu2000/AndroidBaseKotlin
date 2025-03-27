package com.jin.week_view_event.listener

interface DayTimeInterpreter {
    fun interpretDay(day: Int): String

    fun interpretTime(hour: Int, minutes: Int): String
}