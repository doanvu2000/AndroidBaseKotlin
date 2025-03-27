package com.jin

import android.graphics.RectF

/**
 * A class to hold reference to the events and their visual representation. An EventRect is
 * actually the rectangle that is drawn on the calendar for a given event. There may be more
 * than one rectangle for a single event (an event that expands more than one day). In that
 * case two instances of the EventRect will be used for a single event. The given event will be
 * stored in "originalEvent". But the event that corresponds to rectangle the rectangle
 * instance will be stored in "event".
 */
data class EventRect(
    var event: WeekViewEvent,
    var originalEvent: WeekViewEvent,
    var rectF: RectF? = null,
    var left: Float? = rectF?.left,
    var width: Float? = rectF?.width(),
    var top: Float? = rectF?.top,
    var bottom: Float? = rectF?.bottom
) {
    /**
     * Create a new instance of event rect. An EventRect is actually the rectangle that is drawn
     * on the calendar for a given event. There may be more than one rectangle for a single
     * event (an event that expands more than one day). In that case two instances of the
     * EventRect will be used for a single event. The given event will be stored in
     * "originalEvent". But the event that corresponds to rectangle the rectangle instance will
     * be stored in "event".
     *
     * @param event Represents the event which this instance of rectangle represents.
     * @param originalEvent The original event that was passed by the user.
     * @param rectF The rectangle.
     */
    constructor(event: WeekViewEvent, originalEvent: WeekViewEvent, rectF: RectF) : this(
        event, originalEvent, rectF, rectF.left, rectF.width(), rectF.top, rectF.bottom
    )
}