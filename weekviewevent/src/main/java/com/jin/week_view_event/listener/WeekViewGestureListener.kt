package com.jin.week_view_event.listener

import android.graphics.RectF
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.SoundEffectConstants
import com.jin.DayTime
import com.jin.EventRect
import com.jin.WeekView
import com.jin.WeekView.Direction
import com.jin.WeekViewEvent
import com.jin.WeekViewUtil.daysBetween
import com.jin.WeekViewUtil.getPassedMinutesInDay
import java.util.Collections
import kotlin.math.abs
import kotlin.math.ceil

fun WeekView.createGestureListener(
    eventReacts: MutableList<EventRect>
) = object : GestureDetector.SimpleOnGestureListener() {
    override fun onLongPress(e: MotionEvent) {
        super.onLongPress(e)

        if (mEventLongPressListener != null) {
            val reversedEventReacts = eventReacts.reversed()
            for (event in reversedEventReacts) {
                if (event.rectF != null && e.x > event.rectF!!.left && e.x < event.rectF!!.right && e.y > event.rectF!!.top && e.y < event.rectF!!.bottom) {
                    mEventLongPressListener!!.onEventLongPress(
                        event.originalEvent, event.rectF!!
                    )
                    performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                    return
                }
            }
        }

        // If the tap was on in an empty space, then trigger the callback.
        if (mEmptyViewLongPressListener != null && e.x > headerColumnWidth && e.y > (headerHeight + headerRowPadding * 2 + headerMarginBottom)) {
            val selectedTime = getTimeFromPoint(e.x, e.y)
            if (selectedTime != null) {
                performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                mEmptyViewLongPressListener!!.onEmptyViewLongPress(selectedTime)
            }
        }
    }

    override fun onScroll(
        e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float
    ): Boolean {
        // Check if view is zoomed.
        if (isZooming) {
            return true
        }

        when (currentScrollDirection) {
            Direction.NONE -> {
                // Allow scrolling only in one direction.
                currentScrollDirection = if (abs(distanceX) > abs(distanceY)) {
                    if (distanceX > 0) Direction.LEFT else Direction.RIGHT
                } else {
                    Direction.VERTICAL
                }
            }

            Direction.LEFT -> {
                // Change direction if there was enough change.
                if (abs(distanceX) > abs(distanceY) && distanceX < -scaledTouchSlop) {
                    currentScrollDirection = Direction.RIGHT
                }
            }

            Direction.RIGHT -> {
                // Change direction if there was enough change.
                if (abs(distanceX) > abs(distanceY) && distanceX > scaledTouchSlop) {
                    currentScrollDirection = Direction.LEFT
                }
            }

            else -> {}
        }

        // Calculate the new origin after scroll.
        when (currentScrollDirection) {
            Direction.LEFT, Direction.RIGHT -> {
                val minX = getXMinLimit()
                val maxX = getXMaxLimit()
                val newX = currentOrigin.x - (distanceX * xScrollingSpeed)
                currentOrigin.x = when {
                    newX > maxX -> maxX
                    newX < minX -> minX
                    else -> newX
                }
                postInvalidateOnAnimation()
            }

            Direction.VERTICAL -> {
                val minY = getYMinLimit()
                val maxY = getYMaxLimit()
                val newY = currentOrigin.y - distanceY
                currentOrigin.y = when {
                    newY > maxY -> maxY
                    newY < minY -> minY
                    else -> newY
                }
                postInvalidateOnAnimation()
            }

            else -> {}
        }
        return true
    }

    override fun onFling(
        e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
    ): Boolean {
        if (isZooming) {
            return true
        }

        if ((currentFlingDirection == Direction.LEFT && !mHorizontalFlingEnabled)
            || (currentFlingDirection == Direction.RIGHT && !mHorizontalFlingEnabled)
            || (currentFlingDirection == Direction.VERTICAL && !mVerticalFlingEnabled)
        ) {
            return true
        }

        mScroller.forceFinished(true)

        currentFlingDirection = currentScrollDirection
        when (currentFlingDirection) {
            Direction.LEFT, Direction.RIGHT -> {
                mScroller.fling(
                    currentOrigin.x.toInt(),
                    currentOrigin.y.toInt(),
                    (velocityX * xScrollingSpeed).toInt(),
                    0,
                    getXMinLimit().toInt(),
                    getXMaxLimit().toInt(),
                    getYMinLimit().toInt(),
                    getYMaxLimit().toInt()
                )
            }

            Direction.VERTICAL -> {
                mScroller.fling(
                    currentOrigin.x.toInt(),
                    currentOrigin.y.toInt(),
                    0,
                    velocityY.toInt(),
                    getXMinLimit().toInt(),
                    getXMaxLimit().toInt(),
                    getYMinLimit().toInt(),
                    getYMaxLimit().toInt()
                )
            }

            else -> { /* no-op */
            }
        }

        postInvalidateOnAnimation()
        return true
    }

    override fun onDown(e: MotionEvent): Boolean {
        goToNearestOrigin()
        return true
    }

    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
        // If the tap was on an event then trigger the callback.

        if (mEventClickListener != null) {
            val reversedEventRects: List<EventRect> = eventReacts
            Collections.reverse(reversedEventRects)
            for (eventRect in reversedEventRects) {
                if (newEventIdentifier != eventRect.event.id && eventRect.rectF != null && e.x > eventRect.rectF!!.left && e.x < eventRect.rectF!!.right && e.y > eventRect.rectF!!.top && e.y < eventRect.rectF!!.bottom) {
                    mEventClickListener!!.onEventClick(
                        eventRect.originalEvent, eventRect.rectF!!
                    )
                    playSoundEffect(SoundEffectConstants.CLICK)
                    return super.onSingleTapConfirmed(e)
                }
            }
        }

        val xOffset = getXStartPixel()

        val x = e.x - xOffset
        val y: Float = e.y - currentOrigin.y
        // If the tap was on add new Event space, then trigger the callback
        if (mAddEventClickListener != null && newEventRect != null && newEventRect?.rectF != null && newEventRect!!.rectF!!.contains(
                x, y
            )
        ) {
            mAddEventClickListener!!.onAddEventClicked(
                newEventRect!!.event.startTime!!, newEventRect!!.event.endTime!!
            )
            return super.onSingleTapConfirmed(e)
        }

        // If the tap was on an empty space, then trigger the callback.
        if ((mEmptyViewClickListener != null || mAddEventClickListener != null) && e.x > headerColumnWidth && e.y > (headerHeight + headerRowPadding * 2 + headerMarginBottom)) {
            val selectedTime = getTimeFromPoint(e.x, e.y)

            if (selectedTime != null) {
                val tempEvents: MutableList<WeekViewEvent> = ArrayList(events)
                if (newEventRect != null) {
                    tempEvents.remove(newEventRect!!.event)
                    newEventRect = null
                }

                playSoundEffect(SoundEffectConstants.CLICK)

                if (mEmptyViewClickListener != null) {
                    mEmptyViewClickListener!!.onEmptyViewClicked(DayTime(selectedTime))
                }

                if (mAddEventClickListener != null) {
                    //round selectedTime to resolution
                    selectedTime.subtractMinutes(newEventLengthInMinutes / 2)
                    //Fix selected time if before the minimum hour
                    if (selectedTime.minute < mMinTime) {
                        selectedTime.setTime(mMinTime, 0)
                    }
                    val unroundedMinutes = selectedTime.minute
                    val mod: Int = unroundedMinutes % newEventTimeResolutionInMinutes
                    selectedTime.addMinutes(if (mod < ceil((newEventTimeResolutionInMinutes / 2).toDouble())) -mod else (newEventTimeResolutionInMinutes - mod))

                    val endTime = DayTime(selectedTime)

                    //Minus one to ensure it is the same day and not midnight (next day)
                    val maxMinutes = (mMaxTime - selectedTime.hour) * 60 - selectedTime.minute - 1
                    endTime.addMinutes(maxMinutes.coerceAtMost(newEventLengthInMinutes))
                    //If clicked at end of the day, fix selected startTime
                    if (maxMinutes < newEventLengthInMinutes) {
                        selectedTime.addMinutes(maxMinutes - newEventLengthInMinutes)
                    }

                    val newEvent = WeekViewEvent(
                        newEventIdentifier, "", null, selectedTime, endTime
                    )

                    val top: Float =
                        hourHeight * getPassedMinutesInDay(selectedTime) / 60 + getEventsTop()
                    val bottom: Float =
                        hourHeight * getPassedMinutesInDay(endTime) / 60 + getEventsTop()

                    // Calculate left and right.
                    val left: Float = widthPerDay * daysBetween(
                        getFirstVisibleDay()!!, selectedTime.day!!
                    )
                    val right: Float = left + widthPerDay

                    // Add the new event if its bounds are valid
                    if (left < right && left < width && top < height && right > headerColumnWidth && bottom > 0) {
                        val dayRectF = RectF(left, top, right, bottom - currentOrigin.y)
                        newEvent.color = newEventColor
                        newEventRect = EventRect(newEvent, newEvent, dayRectF)
                        tempEvents.add(newEvent)
                        clearEvents()
                        cacheAndSortEvents(tempEvents)
                        computePositionOfEvents(eventReacts)
                        invalidate()
                    }
                }
            }
        }
        return super.onSingleTapConfirmed(e)
    }
}