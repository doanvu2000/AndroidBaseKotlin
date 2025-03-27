package com.jin

import android.graphics.Shader
import androidx.annotation.ColorInt


data class WeekViewEvent(
    var id: String? = null,
    var startTime: DayTime? = null,
    var endTime: DayTime? = null,
    var name: String? = null,
    var location: String? = null,
    @ColorInt var color: Int = 0,
    var allDay: Boolean = false,
    var shader: Shader? = null
) {
    // Empty constructor
    constructor() : this(null, null, null, null, null, 0, false, null)

    // Constructor with all parameters including shader
    constructor(
        id: String,
        name: String,
        location: String?,
        startTime: DayTime,
        endTime: DayTime,
        allDay: Boolean,
        shader: Shader?
    ) : this() {
        this.id = id
        this.name = name
        this.location = location
        this.startTime = startTime
        this.endTime = endTime
        this.allDay = allDay
        this.shader = shader
    }

    // Deprecated constructor with long id
    @Deprecated("Use String id constructor instead")
    constructor(
        id: Long,
        name: String,
        location: String?,
        startTime: DayTime,
        endTime: DayTime,
        allDay: Boolean,
        shader: Shader?
    ) : this(id.toString(), name, location, startTime, endTime, allDay, shader)

    // Constructor with location and allDay
    constructor(
        id: String,
        name: String,
        location: String?,
        startTime: DayTime,
        endTime: DayTime,
        allDay: Boolean
    ) : this(id, name, location, startTime, endTime, allDay, null)

    // Constructor with location
    constructor(
        id: String, name: String, location: String?, startTime: DayTime, endTime: DayTime
    ) : this(id, name, location, startTime, endTime, false)

    // Basic constructor
    constructor(
        id: String, name: String, startTime: DayTime, endTime: DayTime
    ) : this(id, name, null, startTime, endTime)

    override fun toString(): String {
        return "WeekViewEvent(mId='$id', mStartTime=$startTime, mEndTime=$endTime, mName='$name', mLocation='$location')"
    }

    fun splitWeekViewEvents(): MutableList<WeekViewEvent> {
        //This function splits the WeekViewEvent in WeekViewEvents by day
        val events: MutableList<WeekViewEvent> = ArrayList()
//        var endTime = DayTime(this.endTime)
//        if (this.startTime!!.day != endTime.day) {
//            endTime = DayTime(
//                localDateTime = LocalTime.MAX,
//                day = this.startTime.day,
//            )
//            val event1 = WeekViewEvent(
//                this.id, this.name, this.mLocation, this.mStartTime, endTime,
//                this.mAllDay
//            )
//            event1.setColor(this.mColor)
//            events.add(event1)
//
//            // Add other days.
//            val otherDay: DayTime = DayTime(DayOfWeek.of(1), this.mStartTime.getTime())
//            while (otherDay.day != this.mEndTime.getDay()) {
//                val overDay = DayTime(otherDay.day!!, LocalTime.MIN)
//                val endOfOverDay = DayTime(overDay.day!!, LocalTime.MAX)
//                val eventMore = WeekViewEvent(
//                    this.mId, this.mName, null, overDay, endOfOverDay, this
//                        .mAllDay
//                )
//                eventMore.setColor(this.getColor())
//                events.add(eventMore)
//
//                // Add next day.
//                otherDay.addDays(1)
//            }
//
//            // Add last day.
//            val startTime: DayTime = DayTime(this.mEndTime.getDay(), LocalTime.MIN)
//            val event2 = WeekViewEvent(
//                this.mId, this.mName, this.mLocation, startTime, this.mEndTime,
//                this.mAllDay
//            )
//            event2.setColor(this.getColor())
//            events.add(event2)
//        } else {
//            events.add(this)
//        }

        return events
    }


    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }

        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val that = o as WeekViewEvent

        return id.equals(that.id)
    }
}