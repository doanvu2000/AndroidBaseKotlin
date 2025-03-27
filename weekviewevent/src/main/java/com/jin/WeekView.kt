package com.jin

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.SpannableStringBuilder
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.text.style.StyleSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.DragEvent
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewConfiguration
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.OverScroller
import androidx.core.graphics.scale
import androidx.core.graphics.withClip
import androidx.core.graphics.withSave
import androidx.core.graphics.withTranslation
import com.jin.WeekViewUtil.daysBetween
import com.jin.WeekViewUtil.getPassedMinutesInDay
import com.jin.week_view_event.R
import com.jin.week_view_event.listener.AddEventClickListener
import com.jin.week_view_event.listener.DayTimeInterpreter
import com.jin.week_view_event.listener.DropListener
import com.jin.week_view_event.listener.EmptyViewClickListener
import com.jin.week_view_event.listener.EmptyViewLongPressListener
import com.jin.week_view_event.listener.EventClickListener
import com.jin.week_view_event.listener.EventLongPressListener
import com.jin.week_view_event.listener.TextColorPicker
import com.jin.week_view_event.listener.WeekViewLoader
import com.jin.week_view_event.listener.ZoomEndListener
import com.jin.week_view_event.listener.createGestureListener
import java.time.DayOfWeek
import java.time.LocalDateTime
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class WeekView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private val now: LocalDateTime = DateUtils.getLocalDateTimeNow()

    private lateinit var homeDay: DayOfWeek
    private var minDay: DayOfWeek? = null
    private var maxDay: DayOfWeek? = null
    private var firstVisibleDay: DayOfWeek? = null
    var lastVisibleDay: DayOfWeek? = null
    private var mFirstDayOfWeek = DayOfWeek.SUNDAY
    private var scrollToDay: DayOfWeek? = null

    private var timeTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var headerTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var headerBackgroundPaint: Paint = Paint()
    private var dayBackgroundPaint: Paint = Paint()
    private var hourSeparatorPaint: Paint = Paint()
    private var todayBackgroundPaint: Paint = Paint()
    private var futureBackgroundPaint: Paint = Paint()
    private var pastBackgroundPaint: Paint = Paint()
    private var futureWeekendBackgroundPaint: Paint = Paint()
    private var pastWeekendBackgroundPaint: Paint = Paint()
    private var nowLinePaint: Paint = Paint()
    private var todayHeaderTextPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var eventBackgroundPaint: Paint = Paint()
    private var headerColumnBackgroundPaint: Paint = Paint()

    private var eventTextPaint: TextPaint =
        TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG)

    private var timeTextWidth: Float = 0f
    private var timeTextHeight: Float = 0f
    private var headerTextHeight: Float = 0f
    var headerHeight: Float = 0f
    var widthPerDay: Float = 0f
    var headerMarginBottom: Float = 0f
    var headerColumnWidth: Float = 0f
    var xScrollingSpeed: Float = 0f
    private var mZoomFocusPoint: Float = 0f

    var textSize: Float = 12F
        set(value) {
            field = value
            todayHeaderTextPaint.textSize = value
            headerTextPaint.textSize = value
            timeTextPaint.textSize = value
            invalidate()
        }

    private var scrollToHour: Double = 0.0

    private lateinit var gestureDetector: GestureDetector
    lateinit var mScroller: OverScroller
    var currentOrigin: PointF = PointF(0f, 0f)

    var currentScrollDirection: Direction = Direction.NONE
    var currentFlingDirection: Direction = Direction.NONE

    private var eventRects: MutableList<EventRect> = mutableListOf()
    var events: MutableList<WeekViewEvent> = mutableListOf()

    // the middle period the calendar has fetched.
    private var fetchedPeriod: Int = -1
    private var minimumFlingVelocity: Int = 0
    var scaledTouchSlop: Int = 0
    var hourHeight: Int = 50
        set(value) {
            field = value
            invalidate()
        }
    private var newHourHeight: Int = -1
    var minHourHeight: Int = 0 //no minimum specified (will be dynamic, based on screen)

    //compensates for the fact that you can't keep zooming out.
    private var effectiveMinHourHeight: Int = minHourHeight
    var maxHourHeight: Int = 250
    var columnGap: Int = 10
        set(value) {
            field = value
            invalidate()
        }
    private var headerColumnPadding: Int = 10
    private var headerColumnTextColor: Int = Color.BLACK

    var numberOfVisibleDays: Int = 3
        set(value) {
            field = value
            resetHomeDay()
            currentOrigin.x = 0F
            currentOrigin.y = 0F
            invalidate()
        }
    var headerRowPadding: Int = 10
        set(value) {
            field = value
            invalidate()
        }
    private var headerRowBackgroundColor: Int = Color.WHITE
    var dayBackgroundColor: Int = ColorsDefault.rgb245
        set(value) {
            field = value
            dayBackgroundPaint.color = value
            invalidate()
        }
    private var pastBackgroundColor: Int = ColorsDefault.rgb227
    private var futureBackgroundColor: Int = ColorsDefault.rgb245
    private var pastWeekendBackgroundColor: Int = 0
    private var futureWeekendBackgroundColor: Int = 0
    private var nowLineColor: Int = ColorsDefault.rgb102
    private var nowLineThickness: Int = 5
    private var hourSeparatorColor: Int = ColorsDefault.rgb230
    private var todayBackgroundColor: Int = ColorsDefault.aliceBlue
    private var hourSeparatorHeight: Int = 2
    private var todayHeaderTextColor: Int = ColorsDefault.cyanBlue
    var mEventTextSize: Int = 12
        set(value) {
            field = value
            eventTextPaint.textSize = value.toFloat()
            invalidate()
        }

    private var mEventTextColor: Int = Color.BLACK
        set(value) {
            field = value
            eventTextPaint.color = value
            invalidate()
        }
    private var mEventPadding: Int = 8
        set(value) {
            field = value
            invalidate()
        }
    private var headerColumnBackgroundColor: Int = Color.WHITE
    private var defaultEventColor: Int = Color.WHITE
    var newEventColor: Int = Color.WHITE
        set(value) {
            field = value
            invalidate()
        }
    var newEventLengthInMinutes: Int = 60
    var newEventTimeResolutionInMinutes: Int = 15
    private var overlappingEventGap: Int = 0
    private var eventMarginVertical: Int = 0
    private var eventCornerRadius: Int = 0
    private var mAllDayEventHeight: Int = 100
    private var mScrollDuration: Int = 250
    private var mTimeColumnResolution: Int = 60
    var mMinTime: Int = 0
    var mMaxTime: Int = 24
    var minOverlappingMinutes: Int = 0

    var newEventIdentifier: String = "-100"

    private var newEventIconDrawable: Drawable? = null

    private var refreshEvents: Boolean = false
    var isZooming: Boolean = false
    private var showFirstDayOfWeekFirst: Boolean = false
    private var isFirstDraw: Boolean = true
    private var areDimensionsInvalid: Boolean = true
    private var mShowDistinctWeekendColor: Boolean = false
    private var mShowDistinctPastFutureColor: Boolean = false
    private var mShowNowLine: Boolean = true
    var mHorizontalFlingEnabled: Boolean = true
    var mVerticalFlingEnabled: Boolean = true
    private var mZoomFocusPointEnabled: Boolean = false
    private var mAutoLimitTime: Boolean = false
    private var mEnableDropListener: Boolean = false
    private lateinit var scaleDetector: ScaleGestureDetector

    var newEventRect: EventRect? = null
    private var textColorPicker: TextColorPicker? = null

    private var mTypeface: Typeface = Typeface.DEFAULT_BOLD

    //region Listeners.
    var mEventClickListener: EventClickListener? = null
    var mEventLongPressListener: EventLongPressListener? = null
    private var mWeekViewLoader: WeekViewLoader? = null
    var mEmptyViewClickListener: EmptyViewClickListener? = null
    var mEmptyViewLongPressListener: EmptyViewLongPressListener? = null
    private var mDayTimeInterpreter: DayTimeInterpreter? = null
    var mAddEventClickListener: AddEventClickListener? = null
    private var mDropListener: DropListener? = null
    private var mZoomEndListener: ZoomEndListener? = null

    private val mGestureListener = createGestureListener(eventRects)
    //endregion

    init {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.WeekView, 0, 0)
        try {
            mFirstDayOfWeek = DayOfWeek.of(
                a.getInteger(
                    R.styleable.WeekView_firstDayOfWeek, mFirstDayOfWeek.value
                )
            )
            hourHeight = a.getDimensionPixelSize(R.styleable.WeekView_hourHeight, hourHeight)
            minHourHeight =
                a.getDimensionPixelSize(R.styleable.WeekView_minHourHeight, minHourHeight)
            effectiveMinHourHeight = minHourHeight
            maxHourHeight =
                a.getDimensionPixelSize(R.styleable.WeekView_maxHourHeight, maxHourHeight)
            textSize = a.getDimensionPixelSize(
                R.styleable.WeekView_textSize, TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP, textSize, context.resources.displayMetrics
                ).toInt()
            ).toFloat()
            headerColumnPadding = a.getDimensionPixelSize(
                R.styleable.WeekView_headerColumnPadding, headerColumnPadding
            )
            columnGap = a.getDimensionPixelSize(R.styleable.WeekView_columnGap, columnGap)
            headerColumnTextColor =
                a.getColor(R.styleable.WeekView_headerColumnTextColor, headerColumnTextColor)
            numberOfVisibleDays =
                a.getInteger(R.styleable.WeekView_noOfVisibleDays, numberOfVisibleDays)
            showFirstDayOfWeekFirst =
                a.getBoolean(R.styleable.WeekView_showFirstDayOfWeekFirst, showFirstDayOfWeekFirst)
            headerRowPadding =
                a.getDimensionPixelSize(R.styleable.WeekView_headerRowPadding, headerRowPadding)
            headerRowBackgroundColor =
                a.getColor(R.styleable.WeekView_headerRowBackgroundColor, headerRowBackgroundColor)
            dayBackgroundColor =
                a.getColor(R.styleable.WeekView_dayBackgroundColor, dayBackgroundColor)
            futureBackgroundColor =
                a.getColor(R.styleable.WeekView_futureBackgroundColor, futureBackgroundColor)
            pastBackgroundColor =
                a.getColor(R.styleable.WeekView_pastBackgroundColor, pastBackgroundColor)
            futureWeekendBackgroundColor = a.getColor(
                R.styleable.WeekView_futureWeekendBackgroundColor, futureBackgroundColor
            )
            pastWeekendBackgroundColor = a.getColor(
                R.styleable.WeekView_pastWeekendBackgroundColor, pastWeekendBackgroundColor
            )
            nowLineColor = a.getColor(R.styleable.WeekView_nowLineColor, nowLineColor)
            nowLineThickness =
                a.getDimensionPixelSize(R.styleable.WeekView_nowLineThickness, nowLineThickness)
            hourSeparatorColor =
                a.getColor(R.styleable.WeekView_hourSeparatorColor, hourSeparatorColor)
            todayBackgroundColor =
                a.getColor(R.styleable.WeekView_todayBackgroundColor, todayBackgroundColor)
            hourSeparatorHeight = a.getDimensionPixelSize(
                R.styleable.WeekView_hourSeparatorHeight, hourSeparatorHeight
            )
            todayHeaderTextColor =
                a.getColor(R.styleable.WeekView_todayHeaderTextColor, todayHeaderTextColor)
            mEventTextSize = a.getDimensionPixelSize(
                R.styleable.WeekView_eventTextSize, TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_SP,
                    mEventTextSize.toFloat(),
                    context.resources.displayMetrics
                ).toInt()
            )
            mEventTextColor = a.getColor(R.styleable.WeekView_eventTextColor, mEventTextColor)
            newEventColor = a.getColor(R.styleable.WeekView_newEventColor, newEventColor)
            newEventIconDrawable = a.getDrawable(R.styleable.WeekView_newEventIconResource)
            newEventIdentifier =
                a.getString(R.styleable.WeekView_newEventIdentifier) ?: newEventIdentifier
            newEventLengthInMinutes =
                a.getInt(R.styleable.WeekView_newEventLengthInMinutes, newEventLengthInMinutes)
            newEventTimeResolutionInMinutes = a.getInt(
                R.styleable.WeekView_newEventTimeResolutionInMinutes,
                newEventTimeResolutionInMinutes
            )
            mEventPadding =
                a.getDimensionPixelSize(R.styleable.WeekView_eventPadding, mEventPadding)
            headerColumnBackgroundColor = a.getColor(
                R.styleable.WeekView_headerColumnBackground, headerColumnBackgroundColor
            )
            overlappingEventGap = a.getDimensionPixelSize(
                R.styleable.WeekView_overlappingEventGap, overlappingEventGap
            )
            eventMarginVertical = a.getDimensionPixelSize(
                R.styleable.WeekView_eventMarginVertical, eventMarginVertical
            )
            xScrollingSpeed = a.getFloat(R.styleable.WeekView_xScrollingSpeed, xScrollingSpeed)
            eventCornerRadius =
                a.getDimensionPixelSize(R.styleable.WeekView_eventCornerRadius, eventCornerRadius)
            mShowDistinctPastFutureColor = a.getBoolean(
                R.styleable.WeekView_showDistinctPastFutureColor, mShowDistinctPastFutureColor
            )
            mShowDistinctWeekendColor = a.getBoolean(
                R.styleable.WeekView_showDistinctWeekendColor, mShowDistinctWeekendColor
            )
            mShowNowLine = a.getBoolean(R.styleable.WeekView_showNowLine, mShowNowLine)
            mHorizontalFlingEnabled =
                a.getBoolean(R.styleable.WeekView_horizontalFlingEnabled, mHorizontalFlingEnabled)
            mVerticalFlingEnabled =
                a.getBoolean(R.styleable.WeekView_verticalFlingEnabled, mVerticalFlingEnabled)
            mAllDayEventHeight =
                a.getDimensionPixelSize(R.styleable.WeekView_allDayEventHeight, mAllDayEventHeight)
            mZoomFocusPoint =
                a.getFraction(R.styleable.WeekView_zoomFocusPoint, 1, 1, mZoomFocusPoint)
            mZoomFocusPointEnabled =
                a.getBoolean(R.styleable.WeekView_zoomFocusPointEnabled, mZoomFocusPointEnabled)
            mScrollDuration = a.getInt(R.styleable.WeekView_scrollDuration, mScrollDuration)
            mTimeColumnResolution =
                a.getInt(R.styleable.WeekView_timeColumnResolution, mTimeColumnResolution)
            mAutoLimitTime = a.getBoolean(R.styleable.WeekView_autoLimitTime, mAutoLimitTime)
            mMinTime = a.getInt(R.styleable.WeekView_minTime, mMinTime)
            mMaxTime = a.getInt(R.styleable.WeekView_maxTime, mMaxTime)
            if (a.getBoolean(R.styleable.WeekView_dropListenerEnabled, false)) {
                enableDropListener()
            }
            minOverlappingMinutes = a.getInt(R.styleable.WeekView_minOverlappingMinutes, 0)
        } finally {
            a.recycle()
        }

        init()
    }

    private fun init() {
        resetHomeDay()

        gestureDetector = GestureDetector(context, mGestureListener)
        mScroller = OverScroller(context, AccelerateDecelerateInterpolator())

        minimumFlingVelocity = ViewConfiguration.get(context).scaledMinimumFlingVelocity
        scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop

        // Measure settings for time column.
        timeTextPaint.textAlign = Paint.Align.RIGHT
        timeTextPaint.textSize = textSize
        timeTextPaint.color = headerColumnTextColor

        val rect = Rect()
        val exampleTime = if (mTimeColumnResolution % 60 != 0) "00:00 PM" else "00 PM"
        timeTextPaint.getTextBounds(exampleTime, 0, exampleTime.length, rect)
        timeTextWidth = timeTextPaint.measureText(exampleTime)
        timeTextHeight = rect.height().toFloat()
        headerMarginBottom = timeTextHeight / 2
        initTextTimeWidth()

        // Measure settings for header row.
        headerTextPaint.color = headerColumnTextColor
        headerTextPaint.textAlign = Paint.Align.CENTER
        headerTextPaint.textSize = textSize
        headerTextPaint.getTextBounds(exampleTime, 0, exampleTime.length, rect)
        headerTextHeight = rect.height().toFloat()
        headerTextPaint.typeface = mTypeface

        // Prepare header background paint.
        headerBackgroundPaint.color = headerRowBackgroundColor

        // Prepare day background color paint.
        dayBackgroundPaint.color = dayBackgroundColor
        futureBackgroundPaint.color = futureBackgroundColor
        pastBackgroundPaint.color = pastBackgroundColor
        futureWeekendBackgroundPaint.color = futureWeekendBackgroundColor
        pastWeekendBackgroundPaint.color = pastWeekendBackgroundColor

        // Prepare hour separator color paint.
        hourSeparatorPaint.style = Paint.Style.STROKE
        hourSeparatorPaint.strokeWidth = hourSeparatorHeight.toFloat()
        hourSeparatorPaint.color = hourSeparatorColor

        // Prepare event background color.
        eventBackgroundPaint.color = ColorsDefault.getRgbColor(174, 208, 238)

        // Prepare empty event background color.
        val mNewEventBackgroundPaint = Paint()
        mNewEventBackgroundPaint.color = ColorsDefault.getRgbColor(60, 147, 217)

        // Prepare the "now" line color paint
        nowLinePaint.strokeWidth = nowLineThickness.toFloat()
        nowLinePaint.color = nowLineColor

        headerColumnBackgroundPaint.color = headerColumnBackgroundColor

        // Prepare today background color paint.
        todayBackgroundPaint.color = todayBackgroundColor

        todayHeaderTextPaint.textAlign = Paint.Align.CENTER
        todayHeaderTextPaint.textSize = textSize
        todayHeaderTextPaint.typeface = mTypeface
        todayHeaderTextPaint.color = todayHeaderTextColor


        eventTextPaint.style = Paint.Style.FILL
        eventTextPaint.color = mEventTextColor
        eventTextPaint.textSize = mEventTextSize.toFloat()

        // Set default event color.
        defaultEventColor = ColorsDefault.defaultEventColor

        // Set default empty event color.
        newEventColor = ColorsDefault.newEventColor

        scaleDetector = ScaleGestureDetector(context, WeekViewGestureListener())
    }


    /**
     * limit current time of event by update mMinTime & mMaxTime
     * find smallest of start time & latest of end time
     */
    private fun limitEventTime(days: List<DayOfWeek>) {
        if (eventRects.isNotEmpty()) {
            var startTime: DayTime? = null
            var endTime: DayTime? = null

            for (eventRect in eventRects) {
                for (day in days) {
                    if (eventRect.event.startTime!!.day == day && !eventRect.event.allDay) {
                        if (startTime == null || getPassedMinutesInDay(startTime) > getPassedMinutesInDay(
                                eventRect.event.startTime!!
                            )
                        ) {
                            startTime = eventRect.event.startTime
                        }

                        if (endTime == null || getPassedMinutesInDay(endTime) < getPassedMinutesInDay(
                                eventRect.event.endTime!!
                            )
                        ) {
                            endTime = eventRect.event.endTime
                        }
                    }
                }
            }

            if (startTime != null && endTime != null && startTime.isBefore(endTime)) {
                setLimitTime(
                    max(0, startTime.hour), min(24, (endTime.hour + 1))
                )
            }
        }
    }

    /**
     * Set the interpreter which provides the text to show in the header column and the header row.
     *
     * @param dayTimeInterpreter The day, time interpreter.
     */
    fun setDayTimeInterpreter(dayTimeInterpreter: DayTimeInterpreter?) {
        this.mDayTimeInterpreter = dayTimeInterpreter

        // Refresh time column width.
        initTextTimeWidth()
    }


    fun getDefaultEventColor(): Int {
        return defaultEventColor
    }

    fun setDefaultEventColor(defaultEventColor: Int) {
        this.defaultEventColor = defaultEventColor
        invalidate()
    }

    fun getEmptyViewClickListener(): EmptyViewClickListener? {
        return mEmptyViewClickListener
    }

    fun setEmptyViewClickListener(emptyViewClickListener: EmptyViewClickListener?) {
        this.mEmptyViewClickListener = emptyViewClickListener
    }

    fun getEmptyViewLongPressListener(): EmptyViewLongPressListener? {
        return mEmptyViewLongPressListener
    }

    fun setEmptyViewLongPressListener(emptyViewLongPressListener: EmptyViewLongPressListener?) {
        this.mEmptyViewLongPressListener = emptyViewLongPressListener
    }


    fun getEventClickListener(): EventClickListener? {
        return mEventClickListener
    }

    fun getEventCornerRadius(): Int {
        return eventCornerRadius
    }

    /**
     * Set corner radius for event rect.
     *
     * @param eventCornerRadius the radius in px.
     */
    fun setEventCornerRadius(eventCornerRadius: Int) {
        this.eventCornerRadius = eventCornerRadius
    }


    fun getEventLongPressListener(): EventLongPressListener? {
        return mEventLongPressListener
    }

    fun setEventLongPressListener(eventLongPressListener: EventLongPressListener) {
        this.mEventLongPressListener = eventLongPressListener
    }

    private fun recalculateHourHeight() {
        val height =
            ((height - (headerHeight + headerRowPadding * 2 + timeTextHeight / 2 + headerMarginBottom)) / (this.mMaxTime - this.mMinTime)).toInt()
        if (height > hourHeight) {
            if (height > maxHourHeight) {
                maxHourHeight = height
            }
            maxHourHeight = height
        }
    }

    /**
     * Initialize time column width. Calculate value with all possible hours (supposed widest text).
     */
    private fun initTextTimeWidth() {
        timeTextWidth = 0F
        for (i in 0..<getNumberOfPeriods()) {
            // Measure time string and get max width.
            val time: String = getDayTimeInterpreter().interpretTime(i, (i % 2) * 30)
            checkNotNull(time) { "A DayTimeInterpreter must not return null time" }
            timeTextWidth = timeTextWidth.coerceAtLeast(timeTextPaint.measureText(time))
        }
    }

    /**
     * Get the interpreter which provides the text to show in the header column and the header row.
     *
     * @return The day, time interpreter.
     */
    fun getDayTimeInterpreter(): DayTimeInterpreter {
        return mDayTimeInterpreter ?: createDayTimeInterpreter().also {
            mDayTimeInterpreter = it
        }
    }

    /**
     * Cache and sort events.
     *
     * @param events The events to be cached and sorted.
     */
    fun cacheAndSortEvents(events: MutableList<out WeekViewEvent>) {
        for (event in events) {
            cacheEvent(event)
        }
        sortEventReacts(eventRects)
    }

    /**
     * A simple GestureListener that holds the focused hour while scaling.
     */
    private inner class WeekViewGestureListener : ScaleGestureDetector.OnScaleGestureListener {
        var mFocusedPointY: Float = 0F

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scale = detector.scaleFactor

            newHourHeight = (hourHeight * scale).roundToInt()


            // Calculating difference
            var diffY: Float = mFocusedPointY - currentOrigin.y

            // Scaling difference
            diffY = diffY * scale - diffY

            // Updating week view origin
            currentOrigin.y -= diffY

            invalidate()
            return true
        }

        override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
            isZooming = true
            goToNearestOrigin()

            // Calculate focused point for scale action
            mFocusedPointY = if (isZoomFocusPointEnabled) {
                // Use fractional focus, percentage of height
                (height - headerHeight - headerRowPadding * 2 - headerMarginBottom) * mZoomFocusPoint
            } else {
                // Grab focus
                detector.focusY
            }

            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector) {
            isZooming = false
            mZoomEndListener?.onZoomEnd(hourHeight)
        }
    }

    /**
     * Cache the event for smooth scrolling functionality.
     *
     * @param event The event to cache.
     */
    private fun cacheEvent(event: WeekViewEvent) {
        if (event.startTime!! >= event.endTime!!) {
            return
        }
        val splitEvents: MutableList<WeekViewEvent> = event.splitWeekViewEvents()
        for (eventItem in splitEvents) {
            eventRects.add(EventRect(eventItem, event, null))
        }

        events.add(event)
    }

    /**
     * Set the first day of the week. First day of the week is used only when the week view is first
     * drawn. It does not of any effect after user starts scrolling horizontally.
     *
     *
     * **Note:** This method will only work if the week view is set to display more than 6 days at
     * once.
     *
     *
     * @param firstDayOfWeek The supported values are [DayOfWeek].
     */
    fun setFirstDayOfWeek(firstDayOfWeek: Int) {
        setFirstDayOfWeek(DayOfWeek.of(firstDayOfWeek))
    }

    /**
     * Returns the first visible day in the week view.
     *
     * @return The first visible day in the week view.
     */
    fun getFirstVisibleDay(): DayOfWeek? = firstVisibleDay

    private fun calculateHeaderHeight() {
        //Make sure the header is the right size (depends on AllDay events)
        var containsAllDayEvent = false
        if (eventRects.isNotEmpty()) {
            for (dayNumber in 0..<getRealNumberOfVisibleDays()) {
                val day: DayOfWeek? = getFirstVisibleDay()?.plus(dayNumber.toLong())
                containsAllDayEvent = eventRects.any { item ->
                    item.event.startTime!!.day === day && item.event.allDay
                }
                if (containsAllDayEvent) {
                    break
                }
            }
        }
        headerHeight = if (containsAllDayEvent) {
            headerTextHeight + (mAllDayEventHeight + headerMarginBottom)
        } else {
            headerTextHeight
        }
    }

    fun clearEvents() {
        eventRects.clear()
        events.clear()
    }

    /**
     * Checks if two events overlap.
     *
     * @param event1 The first event.
     * @param event2 The second event.
     * @return true if the events overlap.
     */
    private fun isEventsCollide(event1: WeekViewEvent, event2: WeekViewEvent): Boolean {
        val start1 = event1.startTime!!.toNumericalUnit
        val end1 = event1.endTime!!.toNumericalUnit
        val start2 = event2.startTime!!.toNumericalUnit
        val end2 = event2.endTime!!.toNumericalUnit

        val minOverlappingMillis = minOverlappingMinutes * 60 * 1000L

        return !(start1 + minOverlappingMillis >= end2 || end1 <= start2 + minOverlappingMillis)
    }

    /**
     * Calculates the left and right positions of each events. This comes handy specially if events
     * are overlapping.
     *
     * @param eventReacts The events along with their wrapper class.
     */
    fun computePositionOfEvents(eventReacts: MutableList<EventRect>) {
        // Make "collision groups" for all events that collide with others.
        val collisionGroups: MutableList<MutableList<EventRect>> = mutableListOf()
        for (eventRect in eventReacts) {
            var isPlaced = false

            for (collisionGroup in collisionGroups) {
                for (groupEvent in collisionGroup) {
                    if (isEventsCollide(
                            groupEvent.event, eventRect.event
                        ) && groupEvent.event.allDay == eventRect.event.allDay
                    ) {
                        collisionGroup.add(eventRect)
                        isPlaced = true
                        break
                    }
                }
            }

            if (!isPlaced) {
                val newGroup: MutableList<EventRect> = ArrayList()
                newGroup.add(eventRect)
                collisionGroups.add(newGroup)
            }
        }

        for (collisionGroup in collisionGroups) {
            expandEventsToMaxWidth(collisionGroup)
        }
    }

    /**
     * Draw all the Allday-events of a particular day.
     *
     * @param day The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas The canvas to draw upon.
     */
    private fun drawAllDayEvents(day: DayOfWeek?, startFromPixel: Float, canvas: Canvas) {
        if (eventRects.isEmpty()) {
            return
        }
        eventRects.filter { item -> item.event.startTime!!.day == day && item.event.allDay }
            .forEach { eventRect ->
                try {
                    // Calculate top.

                    val top: Float =
                        headerRowPadding * 2 + headerMarginBottom + +timeTextHeight / 2 + eventMarginVertical

                    // Calculate bottom.
                    val bottom: Float = top + eventRect.bottom!!

                    // Calculate left and right.
                    var left: Float = startFromPixel + eventRect.left!! * widthPerDay
                    if (left < startFromPixel) {
                        left += overlappingEventGap
                    }

                    var right: Float = left + eventRect.width!! * widthPerDay
                    if (right < startFromPixel + widthPerDay) {
                        right -= overlappingEventGap
                    }

                    // Draw the event and the event name on top of it.
                    if (left < right && left < width && top < height && right > headerColumnWidth && bottom > 0) {
                        eventRect.rectF = RectF(left, top, right, bottom)
                        eventBackgroundPaint.color = if (eventRect.event.color == 0) {
                            defaultEventColor
                        } else {
                            eventRect.event.color
                        }
                        eventBackgroundPaint.shader = eventRect.event.shader
                        canvas.drawRoundRect(
                            eventRect.rectF!!,
                            eventCornerRadius.toFloat(),
                            eventCornerRadius.toFloat(),
                            eventBackgroundPaint
                        )
                        drawEventTitle(
                            eventRect.event, eventRect.rectF!!, canvas, top, left
                        )
                    } else {
                        eventRect.rectF = null
                    }
                } catch (e: Exception) {
                    eventRect.rectF = null
                    e.printStackTrace()
                }
            }
    }

    /**
     * Draw the text on top of the rectangle in the empty event.
     */
    private fun drawEmptyImage(
        rect: RectF, canvas: Canvas, originalTop: Float, originalLeft: Float
    ) {
        val height = 0.8 * rect.height()
        val width = 0.8 * rect.width()
        val size = max(1, floor(min(height, width)).toInt())
        if (newEventIconDrawable == null) {
            newEventIconDrawable = resources.getDrawableById(android.R.drawable.ic_input_add)
        }
        var icon = (newEventIconDrawable as BitmapDrawable).bitmap
        icon = icon.scale(size, size, false)
        canvas.drawBitmap(
            icon,
            originalLeft + (rect.width() - icon.width) / 2,
            originalTop + (rect.height() - icon.height) / 2,
            Paint()
        )
    }

    /**
     * Draw the name of the event on top of the event rectangle.
     *
     * @param event The event of which the title (and location) should be drawn.
     * @param rect The rectangle on which the text is to be drawn.
     * @param canvas The canvas to draw upon.
     * @param originalTop The original top position of the rectangle. The rectangle may have some of its portion
     * outside of the visible area.
     * @param originalLeft The original left position of the rectangle. The rectangle may have some of its portion
     * outside of the visible area.
     */
    private fun drawEventTitle(
        event: WeekViewEvent, rect: RectF, canvas: Canvas, originalTop: Float, originalLeft: Float
    ) {
        if (rect.right - rect.left - mEventPadding * 2 < 0) {
            return
        }

        if (rect.bottom - rect.top - mEventPadding * 2 < 0) {
            return
        }

        // Prepare the name of the event.
        val bob = SpannableStringBuilder()
        if (!TextUtils.isEmpty(event.name)) {
            bob.append(event.name)
            bob.setSpan(StyleSpan(Typeface.BOLD), 0, bob.length, 0)
        }
        // Prepare the location of the event.
        if (!TextUtils.isEmpty(event.location)) {
            if (bob.isNotEmpty()) {
                bob.append(' ')
            }
            bob.append(event.location)
        }

        val availableHeight = (rect.bottom - originalTop - mEventPadding * 2).toInt()
        val availableWidth = (rect.right - originalLeft - mEventPadding * 2).toInt()
        // Clip to paint in left column only.
        canvas.withSave {
            clipRect(0F, headerHeight + headerRowPadding * 2, headerColumnWidth, height.toFloat())
        }

        // Get text color if necessary
        textColorPicker?.let {
            eventTextPaint.color = it.getTextColor(event)
        }
        // Get text dimensions.
        var textLayout = StaticLayout(
            bob, eventTextPaint, availableWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false
        )
        if (textLayout.lineCount > 0) {
            val lineHeight: Int = textLayout.height / textLayout.lineCount

            if (availableHeight >= lineHeight) {
                // Calculate available number of line counts.
                var availableLineCount = availableHeight / lineHeight
                do {
                    // Ellipsize text to fit into event rect.
                    if (newEventIdentifier != event.id) {
                        textLayout = StaticLayout(
                            TextUtils.ellipsize(
                                bob,
                                eventTextPaint,
                                (availableLineCount * availableWidth).toFloat(),
                                TextUtils.TruncateAt.END
                            ),
                            eventTextPaint,
                            ((rect.right - originalLeft - mEventPadding * 2)).toInt(),
                            Layout.Alignment.ALIGN_NORMAL,
                            1.0f,
                            0.0f,
                            false
                        )
                    }

                    // Reduce line count.
                    availableLineCount--

                    // Repeat until text is short enough.
                } while (textLayout.height > availableHeight)

                // Draw text.
                canvas.withTranslation(originalLeft + mEventPadding, originalTop + mEventPadding) {
                    textLayout.draw(this)
                }
            }
        }
    }

    /**
     * Draw all the events of a particular day.
     *
     * @param day The day.
     * @param startFromPixel The left position of the day area. The events will never go any left from this value.
     * @param canvas The canvas to draw upon.
     */
    private fun drawEvents(day: DayOfWeek?, startFromPixel: Float, canvas: Canvas) {
        if (eventRects.isEmpty()) {
            return
        }
        eventRects.filter { item -> item.event.startTime!!.day == day && !item.event.allDay }
            .forEach { item ->
                try {
                    val top: Float = hourHeight * item.top!! / 60 + getEventsTop()
                    val bottom: Float = hourHeight * item.bottom!! / 60 + getEventsTop()

                    // Calculate left and right.
                    var left: Float = startFromPixel + item.left!! * widthPerDay
                    if (left < startFromPixel) {
                        left += overlappingEventGap
                    }

                    var right: Float = left + item.width!! * widthPerDay
                    if (right < startFromPixel + widthPerDay) {
                        right -= overlappingEventGap
                    }

                    // Draw the event and the event name on top of it.
                    if (left < right && left < width && top < height && right > headerColumnWidth && bottom > headerHeight + headerRowPadding * 2 + timeTextHeight / 2 + headerMarginBottom) {
                        item.rectF = RectF(left, top, right, bottom)
                        eventBackgroundPaint.color = if (item.event.color == 0) {
                            defaultEventColor
                        } else {
                            item.event.color
                        }
                        eventBackgroundPaint.shader = item.event.shader
                        canvas.drawRoundRect(
                            item.rectF!!,
                            eventCornerRadius.toFloat(),
                            eventCornerRadius.toFloat(),
                            eventBackgroundPaint
                        )

                        var topToUse = top
                        if (item.event.startTime!!.time!!.hour < mMinTime) {
                            topToUse = hourHeight * getPassedMinutesInDay(
                                mMinTime, 0
                            ) / 60 + getEventsTop()
                        }

                        if (newEventIdentifier != item.event.id) {
                            drawEventTitle(
                                item.event, item.rectF!!, canvas, topToUse, left
                            )
                        } else {
                            drawEmptyImage(item.rectF!!, canvas, topToUse, left)
                        }
                    } else {
                        item.rectF = null
                    }
                } catch (e: Exception) {
                    item.rectF = null
                    e.printStackTrace()
                }
            }
    }

    /**
     * Sorts the events in ascending order.
     *
     * @param eventReacts The events to be sorted.
     */
    private fun sortEventReacts(eventReacts: MutableList<EventRect>) {
        eventReacts.sortWith { left, right ->
            val start1: Long = left.event.startTime!!.toNumericalUnit
            val start2: Long = right.event.startTime!!.toNumericalUnit

            var comparator = start1.compareTo(start2)
            if (comparator == 0) {
                val end1: Long = left.event.startTime!!.toNumericalUnit
                val end2: Long = right.event.startTime!!.toNumericalUnit
                comparator = end1.compareTo(end2)
            }
            comparator
        }
    }

    enum class Direction {
        NONE, LEFT, RIGHT, VERTICAL,
    }

    private inner class DragListener : OnDragListener {
        override fun onDrag(v: View, e: DragEvent): Boolean {
            when (e.action) {
                DragEvent.ACTION_DROP -> {
                    if (e.x > headerColumnWidth && e.y > (headerTextHeight + headerRowPadding * 2 + headerMarginBottom)) {
                        val selectedTime = getTimeFromPoint(e.x, e.y)
                        if (selectedTime != null) {
                            mDropListener?.onDrop(v, selectedTime)
                        }
                    }
                }
            }
            return true
        }
    }

    /**
     * Set highest shown time
     *
     * @param endHour limit time display at bottom (between 0~24 and larger than startHour)
     */
    fun setMaxTime(endHour: Int) {
        require(endHour > mMinTime) { "endHour must larger startHour." }
        require(endHour <= 24) { "endHour can't be higher than 24." }
        this.mMaxTime = endHour
        recalculateHourHeight()
        invalidate()
    }

    /**
     * Set the earliest day that can be displayed. This will determine the left horizontal scroll
     * limit. The default value is null (allow unlimited scrolling into the past).
     *
     * @param minDay The new minimum day (pass null for no minimum)
     */
    fun setMinDay(minDay: DayOfWeek?) {
        if (minDay != null) {
            require(!(maxDay != null && minDay > maxDay)) { "minDay cannot be later than maxDay" }
        }

        this.minDay = minDay
        resetHomeDay()
        currentOrigin.x = 0f
        invalidate()
    }

    /**
     * Set minimal shown time
     *
     * @param startHour limit time display on top (between 0~24) and smaller than endHour
     */
    fun setMinTime(startHour: Int) {
        require(mMaxTime > startHour) { "startHour must smaller than endHour" }
        require(startHour >= 0) { "startHour must be at least 0." }
        this.mMinTime = startHour
        recalculateHourHeight()
    }

    /**
     * Set the latest day that can be displayed. This will determine the right horizontal scroll
     * limit. The default value is null (allow unlimited scrolling in to the future).
     *
     * @param maxDay The new maximum day (pass null for no maximum)
     */
    fun setMaxDay(maxDay: DayOfWeek?) {
        if (maxDay != null) {
            require(!(minDay != null && maxDay < minDay)) { "maxDay has to be after minDay" }
        }

        this.maxDay = maxDay
        resetHomeDay()
        currentOrigin.x = 0F
        invalidate()
    }

    private fun getLeftDaysWithGaps(): Int {
        return -(ceil(currentOrigin.x / (widthPerDay + columnGap))).toInt()
    }


    /**
     * Get the latest day that can be displayed. Will return null if no maximum date is set.
     *
     * @return the latest day the can be displayed, null if no max day set
     */
    fun getMaxDay(): DayOfWeek? = maxDay

    fun getNewEventIconDrawable(): Drawable? = newEventIconDrawable

    fun setNewEventIconDrawable(newEventIconDrawable: Drawable?) {
        this.newEventIconDrawable = newEventIconDrawable
    }

    fun getNewEventColor(): Int? = newEventColor

    fun getMaxTime(): Int = mMaxTime

    fun getMinTime(): Int = mMinTime

    /**
     * Get the earliest day that can be displayed. Will return null if no minimum day is set.
     *
     * @return the earliest day that can be displayed, null if no minimum day set
     */
    fun getMinDay(): DayOfWeek? = minDay

    /**
     * Get the time and day where the user clicked on.
     *
     * @param x The x position of the touch event.
     * @param y The y position of the touch event.
     * @return The time and day at the clicked position.
     */
    fun getTimeFromPoint(x: Float, y: Float): DayTime? {
        val leftDaysWithGaps = getLeftDaysWithGaps()
        var startPixel = getXStartPixel()

        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + getRealNumberOfVisibleDays() + 1) {
            val start = if (startPixel < headerColumnWidth) headerColumnWidth else startPixel

            if (widthPerDay + startPixel - start > 0 && x > start && x < startPixel + widthPerDay) {
                val day = DayTime().apply {
                    day = homeDay.plus((dayNumber - 1).toLong())
                    val pixelsFromZero =
                        y - currentOrigin.y - headerHeight - headerRowPadding * 2 - timeTextHeight / 2 - headerMarginBottom
                    val hour = (pixelsFromZero / hourHeight).toInt()
                    val minute = (60 * (pixelsFromZero - hour * hourHeight) / hourHeight).toInt()
                    setTime(hour + mMinTime, minute)
                }
                return day
            }
            startPixel += widthPerDay + columnGap
        }
        return null
    }

    /**
     * Check if scrolling should be stopped.
     *
     * @return true if scrolling should be stopped before reaching the end of animation.
     */
    private fun forceFinishScroll(): Boolean {
        return mScroller.currVelocity <= minimumFlingVelocity
    }

    private fun getXOriginForDay(day: DayOfWeek): Float {
        return -daysBetween(homeDay, day) * (widthPerDay + columnGap)
    }

    fun setZoomEndListener(zoomEndListener: ZoomEndListener) {
        this.mZoomEndListener = zoomEndListener
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the header row.
        drawHeaderRowAndEvents(canvas)

        // Draw the time column and all the axes/separators.
        drawTimeColumnAndAxes(canvas)
    }

    private fun drawHeaderRowAndEvents(canvas: Canvas) {
        // Calculate the available width for each day.
        headerColumnWidth = timeTextWidth + headerColumnPadding * 2
        widthPerDay = width - headerColumnWidth - columnGap * (getRealNumberOfVisibleDays() - 1)
        widthPerDay = widthPerDay / getRealNumberOfVisibleDays()

        calculateHeaderHeight() //Make sure the header is the right size (depends on AllDay events)

        val today: LocalDateTime? = now

        if (areDimensionsInvalid) {
            effectiveMinHourHeight = max(
                minHourHeight,
                ((height - headerHeight - headerRowPadding * 2 - headerMarginBottom) / (mMaxTime - mMinTime)).toInt()
            )

            areDimensionsInvalid = false
            if (scrollToDay != null) {
                goToDay(scrollToDay!!)
            }

            areDimensionsInvalid = false
            if (scrollToHour >= 0) {
                goToHour(scrollToHour)
            }

            scrollToDay = null
            scrollToHour = -1.0
            areDimensionsInvalid = false
        }
        if (isFirstDraw) {
            isFirstDraw = false

            // If the week view is being drawn for the first time, then consider the first day of the week.
            if (getRealNumberOfVisibleDays() >= 7 && homeDay != mFirstDayOfWeek && showFirstDayOfWeekFirst) {
                val difference: Int = homeDay.value - mFirstDayOfWeek.value
                currentOrigin.x += (widthPerDay + columnGap) * difference
            }
            setLimitTime(mMinTime, mMaxTime)
        }

        // Calculate the new height due to the zooming.
        if (newHourHeight > 0) {
            if (newHourHeight < effectiveMinHourHeight) {
                newHourHeight = effectiveMinHourHeight
            } else if (newHourHeight > maxHourHeight) {
                newHourHeight = maxHourHeight
            }

            hourHeight = newHourHeight
            newHourHeight = -1
        }

        // If the new mCurrentOrigin.y is invalid, make it valid.
        if (currentOrigin.y < height - hourHeight * (mMaxTime - mMinTime) - headerHeight - (headerRowPadding * 2) - headerMarginBottom - timeTextHeight / 2) {
            currentOrigin.y =
                height - hourHeight * (mMaxTime - mMinTime) - headerHeight - (headerRowPadding * 2) - headerMarginBottom - timeTextHeight / 2
        }

        // Don't put an "else if" because it will trigger a glitch when completely zoomed out and
        // scrolling vertically.
        if (currentOrigin.y > 0) {
            currentOrigin.y = 0F
        }

        val leftDaysWithGaps = getLeftDaysWithGaps()
        // Consider scroll offset.
        val startFromPixel = getXStartPixel()
        var startPixel = startFromPixel

        // Prepare to iterate for each hour to draw the hour lines.
        var lineCount =
            ((height - headerHeight - headerRowPadding * 2 - headerMarginBottom) / hourHeight).toInt() + 1

        lineCount = (lineCount) * (getRealNumberOfVisibleDays() + 1)

        val hourLines = FloatArray(lineCount * 4)

        // Clear the cache for event rectangles.
        for (eventRect in eventRects) {
            eventRect.rectF = null
        }

        // Clip to paint events only.
        canvas.save()
        canvas.clipRect(
            headerColumnWidth,
            headerHeight + headerRowPadding * 2 + headerMarginBottom + timeTextHeight / 2,
            width.toFloat(),
            height.toFloat()
        )

        // Iterate through each day.
        firstVisibleDay = homeDay
        firstVisibleDay!!.minus((currentOrigin.x / (widthPerDay + columnGap)).roundToInt().toLong())

        if (mAutoLimitTime) {
            val days: MutableList<DayOfWeek> = mutableListOf()
            for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + getRealNumberOfVisibleDays()) {
                val day: DayOfWeek = homeDay
                day.plus((dayNumber - 1).toLong())
                days.add(day)
            }
            limitEventTime(days)
        }

        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + getRealNumberOfVisibleDays() + 1) {
            // Check if the day is today.

            var day: DayOfWeek = homeDay
            lastVisibleDay = day
            day = day.plus((dayNumber - 1).toLong())
            lastVisibleDay?.plus((dayNumber - 2).toLong())
            val isToday = day == today!!.dayOfWeek

            // Don't draw days which are outside requested range
            if (!dayIsValid(day)) {
                continue
            }

            // Get more events if necessary. We want to store the events 3 months beforehand. Get
            // events only when it is the first iteration of the loop.
            if (refreshEvents) {
                getMoreEvents()
                refreshEvents = false
            }

            // Draw background color for each day.
            val start = (if (startPixel < headerColumnWidth) headerColumnWidth else startPixel)
            if (widthPerDay + startPixel - start > 0) {
                if (mShowDistinctPastFutureColor) {
                    val isWeekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
                    val pastPaint: Paint =
                        if (isWeekend && mShowDistinctWeekendColor) pastWeekendBackgroundPaint else pastBackgroundPaint
                    val futurePaint: Paint =
                        if (isWeekend && mShowDistinctWeekendColor) futureWeekendBackgroundPaint else futureBackgroundPaint
                    val startY: Float =
                        (headerHeight + headerRowPadding * 2 + timeTextHeight / 2 + headerMarginBottom + currentOrigin.y)

                    if (isToday) {
                        val beforeNow: Float =
                            (now.hour - mMinTime + now.minute / 60.0f) * hourHeight
                        canvas.drawRect(
                            start, startY, startPixel + widthPerDay, startY + beforeNow, pastPaint
                        )
                        canvas.drawRect(
                            start,
                            startY + beforeNow,
                            startPixel + widthPerDay,
                            height.toFloat(),
                            futurePaint
                        )
                    } else if (day < today.dayOfWeek) {
                        canvas.drawRect(
                            start, startY, startPixel + widthPerDay, height.toFloat(), pastPaint
                        )
                    } else {
                        canvas.drawRect(
                            start, startY, startPixel + widthPerDay, height.toFloat(), futurePaint
                        )
                    }
                } else {
                    canvas.drawRect(
                        start,
                        headerHeight + headerRowPadding * 2 + timeTextHeight / 2 + headerMarginBottom,
                        startPixel + widthPerDay,
                        height.toFloat(),
                        if (isToday) todayBackgroundPaint else dayBackgroundPaint
                    )
                }
            }

            // Prepare the separator lines for hours.
            var i = 0
            for (hourNumber in mMinTime..<mMaxTime) {
                val top: Float =
                    headerHeight + headerRowPadding * 2 + currentOrigin.y + hourHeight * (hourNumber - mMinTime) + timeTextHeight / 2 + headerMarginBottom
                if (top > headerHeight + headerRowPadding * 2 + timeTextHeight / 2 + headerMarginBottom - hourSeparatorHeight && top < height && startPixel + widthPerDay - start > 0) {
                    hourLines[i * 4] = start
                    hourLines[i * 4 + 1] = top
                    hourLines[i * 4 + 2] = startPixel + widthPerDay
                    hourLines[i * 4 + 3] = top
                    i++
                }
            }

            // Draw the lines for hours.
            canvas.drawLines(hourLines, hourSeparatorPaint)

            // Draw the events.
            drawEvents(day, startPixel, canvas)

            // Draw the line at the current time.
            if (mShowNowLine && isToday) {
                val startY: Float =
                    headerHeight + headerRowPadding * 2 + timeTextHeight / 2 + headerMarginBottom + currentOrigin.y
                val beforeNow: Float = (now.hour - mMinTime + now.minute / 60.0f) * hourHeight
                val top = startY + beforeNow
                canvas.drawLine(start, top, startPixel + widthPerDay, top, nowLinePaint)
            }

            // In the next iteration, start from the next day.
            startPixel += widthPerDay + columnGap
        }

        canvas.restore() // Restore previous clip

        // Hide everything in the first cell (top left corner).
        canvas.withClip(
            0F, 0F, timeTextWidth + headerColumnPadding * 2, headerHeight + headerRowPadding * 2
        ) {
            drawRect(
                0f,
                0f,
                timeTextWidth + headerColumnPadding * 2,
                headerHeight + headerRowPadding * 2,
                headerBackgroundPaint
            )
        } // Restore previous clip

        // Clip to paint header row only.
        canvas.withClip(
            headerColumnWidth, 0F, width.toFloat(), headerHeight + headerRowPadding * 2
        ) {
            // Draw the header background.
            drawRect(
                0f, 0f, width.toFloat(), headerHeight + headerRowPadding * 2, headerBackgroundPaint
            )
        }

        // Draw the header row texts.
        startPixel = startFromPixel
        for (dayNumber in leftDaysWithGaps + 1..leftDaysWithGaps + getRealNumberOfVisibleDays() + 1) {
            // Check if the day is today.
            val day: DayOfWeek = homeDay.plus((dayNumber - 1).toLong())
            val isToday = day == today!!.dayOfWeek

            // Don't draw days which are outside requested range
            if (!dayIsValid(day)) {
                continue
            }

            // Draw the day labels.
            val dayLabel = getDayTimeInterpreter().interpretDay(day.value)
            checkNotNull(dayLabel) { "A DayTimeInterpreter must not return null day" }
            canvas.drawText(
                dayLabel,
                startPixel + widthPerDay / 2,
                headerTextHeight + headerRowPadding,
                if (isToday) todayHeaderTextPaint else headerTextPaint
            )
            drawAllDayEvents(day, startPixel, canvas)
            startPixel += widthPerDay + columnGap
        }
    }

    /**
     * Expands all the events to maximum possible width. The events will try to occupy maximum
     * space available horizontally.
     *
     * @param collisionGroup The group of events which overlap with each other.
     */
    private fun expandEventsToMaxWidth(collisionGroup: List<EventRect>) {
        // Expand the events to maximum possible width.
        val columns = mutableListOf<MutableList<EventRect>>()
        columns.add(mutableListOf())
        for (eventRect in collisionGroup) {
            var isPlaced = false
            for (column in columns) {
                if (column.isEmpty()) {
                    column.add(eventRect)
                    isPlaced = true
                } else if (!isEventsCollide(eventRect.event, column.last().event)) {
                    column.add(eventRect)
                    isPlaced = true
                    break
                }
            }
            if (!isPlaced) {
                val newColumn = mutableListOf<EventRect>()
                newColumn.add(eventRect)
                columns.add(newColumn)
            }
        }

        // Calculate left and right position for all the events.
        // Get the maxRowCount by looking in all columns.
        val maxRowCount = columns.maxOfOrNull { it.size } ?: 0
        for (i in 0 until maxRowCount) {
            // Set the left and right values of the event.
            var j = 0f
            for (column in columns) {
                if (column.size >= i + 1) {
                    val eventRect = column[i]
                    eventRect.width = 1f / columns.size
                    eventRect.left = j / columns.size
                    if (!eventRect.event.allDay) {
                        eventRect.top = getPassedMinutesInDay(eventRect.event.startTime!!).toFloat()
                        eventRect.bottom =
                            getPassedMinutesInDay(eventRect.event.endTime!!).toFloat()
                    } else {
                        eventRect.top = 0f
                        eventRect.bottom = mAllDayEventHeight.toFloat()
                    }
                    eventRects.add(eventRect)
                }
                j++
            }
        }
    }

    /**
     * Gets more events of one/more month(s) if necessary. This method is called when the user is
     * scrolling the week view. The week view stores the events of three months: the visible month,
     * the previous month, the next month.
     */
    private fun getMoreEvents() {
        if (mWeekViewLoader == null && !isInEditMode) {
            throw IllegalStateException("You must provide a WeekViewLoader")
        }

        // If a refresh was requested then reset some variables.
        if (refreshEvents) {
            clearEvents()
            fetchedPeriod = -1
        }

        if (mWeekViewLoader != null) {
            if (!isInEditMode && (fetchedPeriod < 0 || refreshEvents)) {
                val newEvents = mWeekViewLoader?.onWeekViewLoad() ?: mutableListOf()
                // Clear events.
                clearEvents()
                cacheAndSortEvents(newEvents)
                calculateHeaderHeight()
            }
        }

        // Prepare to calculate positions of each events.
        val tempEvents = eventRects
        eventRects = mutableListOf()

        // Iterate through each day with events to calculate the position of the events.
        while (tempEvents.isNotEmpty()) {
            val eventReacts = mutableListOf<EventRect>().apply { addAll(tempEvents) }

            // Get first event for a day.
            val eventRect1 = tempEvents.removeAt(0)
            eventReacts.add(eventRect1)

            var i = 0
            while (i < tempEvents.size) {
                // Collect all other events for same day.
                val eventRect2 = tempEvents[i]
                if (eventRect1.event.startTime!!.day == eventRect2.event.startTime!!.day) {
                    tempEvents.removeAt(i)
                    eventReacts.add(eventRect2)
                } else {
                    i++
                }
            }
            computePositionOfEvents(eventReacts)
        }
    }

    private fun drawTimeColumnAndAxes(canvas: Canvas) {
        // Draw the background color for the header column.
        canvas.drawRect(
            0f,
            headerHeight + headerRowPadding * 2,
            headerColumnWidth,
            height.toFloat(),
            headerColumnBackgroundPaint
        )
        // Clip to paint in left column only.
        canvas.withClip(
            0f, headerHeight + headerRowPadding * 2, headerColumnWidth, height.toFloat()
        ) {
            for (i in 0 until getNumberOfPeriods()) {
                // If we are showing half hours (eg. 5:30am), space the times out by half the hour height
                // and need to provide 30 minutes on each odd period, otherwise, minutes is always 0.
                val timesPerHour = 60.0f / mTimeColumnResolution
                val timeSpacing = hourHeight / timesPerHour
                val hour = mMinTime + i / timesPerHour.toInt()
                val minutes = (i % timesPerHour.toInt()) * (60 / timesPerHour.toInt())

                // Calculate the top of the rectangle where the time text will go
                val top =
                    headerHeight + headerRowPadding * 2 + currentOrigin.y + timeSpacing * i + headerMarginBottom

                // Get the time to be displayed, as a String.
                val time = getDayTimeInterpreter().interpretTime(hour, minutes)

                // Draw the text if its y position is not outside of the visible area. The pivot point of the text is the
                // point at the bottom-right corner.
                if (top < height) {
                    drawText(
                        time,
                        timeTextWidth + headerColumnPadding,
                        top + timeTextHeight,
                        timeTextPaint
                    )
                }
            }
        } // Last restore
    }

    // fix rotation changes
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        areDimensionsInvalid = true
    }

    fun getAddEventClickListener(): AddEventClickListener? = mAddEventClickListener

    fun setAddEventClickListener(addEventClickListener: AddEventClickListener) {
        this.mAddEventClickListener = addEventClickListener
    }

    /**
     * Get the height of AllDay-events.
     *
     * @return Height of AllDay-events.
     */
    fun getAllDayEventHeight(): Int {
        return mAllDayEventHeight
    }

    /**
     * Set the height of AllDay-events.
     *
     * @param height the new height of AllDay-events
     */
    fun setAllDayEventHeight(height: Int) {
        mAllDayEventHeight = height
    }

    var eventPadding: Int
        get() = mEventPadding
        set(eventPadding) {
            mEventPadding = eventPadding
            invalidate()
        }

    var eventTextColor: Int
        get() = mEventTextColor
        set(eventTextColor) {
            mEventTextColor = eventTextColor
            eventTextPaint.color = mEventTextColor
            invalidate()
        }

    var eventTextSize: Int
        get() = mEventTextSize
        set(eventTextSize) {
            mEventTextSize = eventTextSize
            eventTextPaint.textSize = eventTextSize.toFloat()
            invalidate()
        }

    fun getEventsTop(): Float {
        // Calculate top.
        return currentOrigin.y + headerHeight + headerRowPadding * 2 + headerMarginBottom + timeTextHeight / 2 + eventMarginVertical - getMinHourOffset()
    }

    private fun getMinHourOffset(): Int = hourHeight * mMinTime

    fun goToNearestOrigin() {
        var leftDays: Float = currentOrigin.x / (widthPerDay + columnGap)

        leftDays = if (currentFlingDirection !== Direction.NONE) {
            // snap to nearest day
            leftDays.roundToInt().toFloat()
        } else if (currentScrollDirection == Direction.LEFT) {
            // snap to last day
            floor(leftDays)
        } else if (currentScrollDirection == Direction.RIGHT) {
            // snap to next day
            ceil(leftDays)
        } else {
            // snap to nearest day
            leftDays.roundToInt().toFloat()
        }

        val nearestOrigin = (currentOrigin.x - leftDays * (widthPerDay + columnGap)).toInt()
        val mayScrollHorizontal =
            currentOrigin.x - nearestOrigin < getXMaxLimit() && currentOrigin.x - nearestOrigin > getXMinLimit()

        if (mayScrollHorizontal) {
            mScroller.startScroll(
                currentOrigin.x.toInt(), currentOrigin.y.toInt(), -nearestOrigin, 0
            )
            postInvalidateOnAnimation()
        }

        if (nearestOrigin != 0 && mayScrollHorizontal) {
            // Stop current animation.
            mScroller.forceFinished(true)
            // Snap to day.
            mScroller.startScroll(
                currentOrigin.x.toInt(),
                currentOrigin.y.toInt(),
                -nearestOrigin,
                0,
                (abs(nearestOrigin.toDouble()) / widthPerDay * mScrollDuration).toInt()
            )
            postInvalidateOnAnimation()
        }
        // Reset scrolling and fling direction.
        currentFlingDirection = Direction.NONE
        currentScrollDirection = currentFlingDirection
    }

    private fun resetHomeDay() {
        var newHomeDay = now.dayOfWeek

        if (minDay != null && newHomeDay < minDay) {
            newHomeDay = minDay
        }
        if (maxDay != null && newHomeDay > maxDay) {
            newHomeDay = maxDay
        }

        if (maxDay != null) {
            var day: DayOfWeek = maxDay!!.plus((1 - getRealNumberOfVisibleDays()).toLong())
            while (day < minDay) {
                day = day.plus(1)
            }

            if (newHomeDay > day) {
                newHomeDay = day
            }
        }

        homeDay = newHomeDay
    }

    fun getFirstDayOfWeek(): DayOfWeek? = mFirstDayOfWeek

    /**
     * Get the first hour that is visible on the screen.
     *
     * @return The first hour that is visible.
     */
    fun getFirstVisibleHour(): Float {
        return -currentOrigin.y / hourHeight
    }

    fun getFutureBackgroundColor(): Int = futureBackgroundColor

    fun setFutureBackgroundColor(futureBackgroundColor: Int) {
        this.futureBackgroundColor = futureBackgroundColor
        futureBackgroundPaint.color = futureBackgroundColor
    }

    fun getFutureWeekendBackgroundColor(): Int = futureWeekendBackgroundColor

    fun setFutureWeekendBackgroundColor(futureWeekendBackgroundColor: Int) {
        this.futureWeekendBackgroundColor = futureWeekendBackgroundColor
        this.futureWeekendBackgroundPaint.color = futureWeekendBackgroundColor
    }

    fun getHeaderColumnBackgroundColor(): Int = headerColumnBackgroundColor

    fun setHeaderColumnBackgroundColor(headerColumnBackgroundColor: Int) {
        this.headerColumnBackgroundColor = headerColumnBackgroundColor
        headerColumnBackgroundPaint.color = headerColumnBackgroundColor
        invalidate()
    }

    fun getHeaderColumnPadding(): Int = headerColumnPadding

    fun setHeaderColumnPadding(headerColumnPadding: Int) {
        this.headerColumnPadding = headerColumnPadding
        invalidate()
    }

    fun getHeaderColumnTextColor(): Int = headerColumnTextColor

    fun setHeaderColumnTextColor(headerColumnTextColor: Int) {
        this.headerColumnTextColor = headerColumnTextColor
        headerTextPaint.color = headerColumnTextColor
        timeTextPaint.color = headerColumnTextColor
        invalidate()
    }

    fun getHeaderRowBackgroundColor(): Int = headerRowBackgroundColor

    fun setHeaderRowBackgroundColor(headerRowBackgroundColor: Int) {
        this.headerRowBackgroundColor = headerRowBackgroundColor
        headerBackgroundPaint.color = headerRowBackgroundColor
        invalidate()
    }

    fun getHourSeparatorColor(): Int = hourSeparatorColor

    fun setHourSeparatorColor(hourSeparatorColor: Int) {
        this.hourSeparatorColor = hourSeparatorColor
        hourSeparatorPaint.color = hourSeparatorColor
        invalidate()
    }

    fun getHourSeparatorHeight(): Int = hourSeparatorHeight

    fun setHourSeparatorHeight(hourSeparatorHeight: Int) {
        this.hourSeparatorHeight = hourSeparatorHeight
        hourSeparatorPaint.strokeWidth = hourSeparatorHeight.toFloat()
        invalidate()
    }

    /**
     * Get the "now" line color.
     *
     * @return The color of the "now" line.
     */
    fun getNowLineColor(): Int = nowLineColor

    /**
     * Set the "now" line color.
     *
     * @param nowLineColor The color of the "now" line.
     */
    fun setNowLineColor(nowLineColor: Int) {
        this.nowLineColor = nowLineColor
        invalidate()
    }

    /**
     * Get the "now" line thickness.
     *
     * @return The thickness of the "now" line.
     */
    fun getNowLineThickness(): Int = nowLineThickness

    /**
     * Set the "now" line thickness.
     *
     * @param nowLineThickness The thickness of the "now" line.
     */
    fun setNowLineThickness(nowLineThickness: Int) {
        this.nowLineThickness = nowLineThickness
        invalidate()
    }

    private fun getNumberOfPeriods(): Int {
        return ((mMaxTime - mMinTime) * (60.0 / mTimeColumnResolution)).toInt()
    }

    fun getOverlappingEventGap(): Int = overlappingEventGap

    /**
     * Set the gap between overlapping events.
     *
     * @param overlappingEventGap The gap between overlapping events.
     */
    fun setOverlappingEventGap(overlappingEventGap: Int) {
        this.overlappingEventGap = overlappingEventGap
        invalidate()
    }

    fun getPastBackgroundColor(): Int = pastBackgroundColor

    fun setPastBackgroundColor(pastBackgroundColor: Int) {
        this.pastBackgroundColor = pastBackgroundColor
        pastBackgroundPaint.color = pastBackgroundColor
    }

    fun getPastWeekendBackgroundColor(): Int = pastWeekendBackgroundColor

    fun setPastWeekendBackgroundColor(pastWeekendBackgroundColor: Int) {
        this.pastWeekendBackgroundColor = pastWeekendBackgroundColor
        this.pastWeekendBackgroundPaint.color = pastWeekendBackgroundColor
    }

    /**
     * Get the real number of visible days
     * If the amount of days between max day and min day is smaller, that value is returned
     *
     * @return The real number of visible days
     */
    fun getRealNumberOfVisibleDays(): Int {
        if (minDay == null || maxDay == null) {
            return numberOfVisibleDays
        }

        return numberOfVisibleDays.coerceAtMost(daysBetween(minDay!!, maxDay!!) + 1)
    }

    /**
     * Get scroll duration
     *
     * @return scroll duration
     */
    fun getScrollDuration(): Int = mScrollDuration

    /**
     * Set the scroll duration
     *
     * @param scrollDuration the new scrollDuration
     */
    fun setScrollDuration(scrollDuration: Int) {
        mScrollDuration = scrollDuration
    }

    fun getTextColorPicker(): TextColorPicker? = textColorPicker

    fun setTextColorPicker(textColorPicker: TextColorPicker) {
        this.textColorPicker = textColorPicker
    }

    fun getTimeColumnResolution(): Int = mTimeColumnResolution

    fun setTimeColumnResolution(resolution: Int) {
        mTimeColumnResolution = resolution
    }

    fun getTodayBackgroundColor(): Int = todayBackgroundColor

    fun setTodayBackgroundColor(todayBackgroundColor: Int) {
        this.todayBackgroundColor = todayBackgroundColor
        todayBackgroundPaint.color = todayBackgroundColor
        invalidate()
    }

    fun getTodayHeaderTextColor(): Int = todayHeaderTextColor

    fun setTodayHeaderTextColor(todayHeaderTextColor: Int) {
        this.todayHeaderTextColor = todayHeaderTextColor
        todayHeaderTextPaint.color = todayHeaderTextColor
        invalidate()
    }

    /**
     * Get event loader in the week view. Event loaders define the  interval after which the events
     * are loaded in week view. For a MonthLoader events are loaded for every month. You can define
     * your custom event loader by extending WeekViewLoader.
     *
     * @return The event loader.
     */
    fun getWeekViewLoader(): WeekViewLoader? = mWeekViewLoader

    /**
     * Set event loader in the week view. For example, a MonthLoader. Event loaders define the
     * interval after which the events are loaded in week view. For a MonthLoader events are loaded
     * for every month. You can define your custom event loader by extending WeekViewLoader.
     *
     * @param loader The event loader.
     */
    fun setWeekViewLoader(loader: WeekViewLoader?) {
        this.mWeekViewLoader = loader
    }

    fun getXMaxLimit(): Float {
        return if (minDay == null) {
            Int.Companion.MAX_VALUE.toFloat()
        } else {
            getXOriginForDay(minDay!!)
        }
    }

    fun getXMinLimit(): Float {
        if (maxDay == null) {
            return Int.Companion.MIN_VALUE.toFloat()
        } else {
            var day: DayOfWeek = maxDay!!.plus((1 - getRealNumberOfVisibleDays()).toLong())
            while (day < minDay) {
                day = day.plus(1)
            }

            return getXOriginForDay(day)
        }
    }

    fun getXStartPixel(): Float {
        return currentOrigin.x + (widthPerDay + columnGap) * getLeftDaysWithGaps() + headerColumnWidth
    }

    fun getYMaxLimit(): Float = 0f

    fun getYMinLimit(): Float {
        return -(hourHeight * (mMaxTime - mMinTime) + headerHeight + headerRowPadding * 2 + headerMarginBottom + timeTextHeight / 2 - height)
    }

    /**
     * Get focus point
     * 0 = top of view, 1 = bottom of view
     * The focused point (multiplier of the view height) where the week view is zoomed around.
     * This point will not move while zooming.
     * @return focus point
     */
    val zoomFocusPoint: Float
        get() = mZoomFocusPoint

    /**
     * Set focus point
     * 0 = top of view, 1 = bottom of view
     * The focused point (multiplier of the view height) where the week view is zoomed around.
     * This point will not move while zooming.
     *
     * @param zoomFocusPoint the new zoomFocusPoint
     */
    fun setZoomFocusPoint(zoomFocusPoint: Float) {
        if (zoomFocusPoint < 0 || zoomFocusPoint > 1) {
            throw IllegalStateException("The zoom focus point percentage has to be between 0 and 1")
        }
        mZoomFocusPoint = zoomFocusPoint
    }

    val isDropListenerEnabled: Boolean
        get() = this.mEnableDropListener

    /**
     * Get whether the week view should fling horizontally.
     *
     * @return True if the week view has horizontal fling enabled.
     */
    val isHorizontalFlingEnabled: Boolean
        get() = mHorizontalFlingEnabled

    /**
     * Set whether the week view should fling horizontally.
     *
     * @param enabled whether the week view should fling horizontally
     */
    fun setHorizontalFlingEnabled(enabled: Boolean) {
        mHorizontalFlingEnabled = enabled
    }

    /**
     * Whether past and future days should have two different background colors. The past and
     * future day colors are defined by the attributes `futureBackgroundColor` and
     * `pastBackgroundColor`.
     *
     * @return True if past and future days should have two different background colors.
     */
    val isShowDistinctPastFutureColor: Boolean
        get() = mShowDistinctPastFutureColor

    /**
     * Set whether weekends should have a background color different from the normal day background
     * color. The past and future day colors are defined by the attributes `futureBackgroundColor`
     * and `pastBackgroundColor`.
     *
     * @param showDistinctPastFutureColor True if past and future should have two different
     * background colors.
     */
    fun setShowDistinctPastFutureColor(showDistinctPastFutureColor: Boolean) {
        this.mShowDistinctPastFutureColor = showDistinctPastFutureColor
        invalidate()
    }

    /**
     * Whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     *
     * @return True if weekends should have different background colors.
     */
    val isShowDistinctWeekendColor: Boolean
        get() = mShowDistinctWeekendColor

    /**
     * Set whether weekends should have a background color different from the normal day background
     * color. The weekend background colors are defined by the attributes
     * `futureWeekendBackgroundColor` and `pastWeekendBackgroundColor`.
     *
     * @param showDistinctWeekendColor True if weekends should have different background colors.
     */
    fun setShowDistinctWeekendColor(showDistinctWeekendColor: Boolean) {
        this.mShowDistinctWeekendColor = showDistinctWeekendColor
        invalidate()
    }

    val isShowFirstDayOfWeekFirst: Boolean
        get() = showFirstDayOfWeekFirst

    fun setShowFirstDayOfWeekFirst(show: Boolean) {
        showFirstDayOfWeekFirst = show
    }

    /**
     * Get whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineThickness`.
     *
     * @return True if "now" line should be displayed.
     */
    val isShowNowLine: Boolean
        get() = mShowNowLine

    /**
     * Set whether "now" line should be displayed. "Now" line is defined by the attributes
     * `nowLineColor` and `nowLineThickness`.
     *
     * @param showNowLine True if "now" line should be displayed.
     */
    fun setShowNowLine(showNowLine: Boolean) {
        this.mShowNowLine = showNowLine
        invalidate()
    }

    /**
     * Get whether the week view should fling vertically.
     *
     * @return True if the week view has vertical fling enabled.
     */
    val isVerticalFlingEnabled: Boolean
        get() = mVerticalFlingEnabled

    /**
     * Set whether the week view should fling vertically.
     *
     * @param enabled whether the week view should fling vertically
     */
    fun setVerticalFlingEnabled(enabled: Boolean) {
        mVerticalFlingEnabled = enabled
    }

    /**
     * Is focus point enabled
     * @return fixed focus point enabled?
     */
    val isZoomFocusPointEnabled: Boolean
        get() = mZoomFocusPointEnabled

    /**
     * Enable zoom focus point
     * If you set this to false the `zoomFocusPoint` won't take effect any more while zooming.
     * The zoom will always be focused at the center of your gesture.
     *
     * @param zoomFocusPointEnabled whether the zoomFocusPoint is enabled
     */
    fun setZoomFocusPointEnabled(zoomFocusPointEnabled: Boolean) {
        mZoomFocusPointEnabled = zoomFocusPointEnabled
    }

    /**
     * auto calculate limit time on events in visible days.
     */
    fun setAutoLimitTime(isAuto: Boolean) {
        this.mAutoLimitTime = isAuto
        invalidate()
    }

    fun setDropListener(dropListener: DropListener) {
        this.mDropListener = dropListener
    }

    /**
     * Set the first day of the week. First day of the week is used only when the week view is first
     * drawn. It does not of any effect after user starts scrolling horizontally.
     *
     * **Note:** This method will only work if the week view is set to display more than 6 days at
     * once.
     *
     * @param firstDayOfWeek First day of the week.
     */
    fun setFirstDayOfWeek(firstDayOfWeek: DayOfWeek) {
        this.mFirstDayOfWeek = firstDayOfWeek
        invalidate()
    }

    fun setOnEventClickListener(listener: EventClickListener) {
        this.mEventClickListener = listener
    }

    fun setTypeface(typeface: Typeface?) {
        typeface?.let {
            eventTextPaint.typeface = it
            todayHeaderTextPaint.typeface = it
            timeTextPaint.typeface = it
            mTypeface = it
            init()
        }
    }

    override fun computeScroll() {
        super.computeScroll()

        if (mScroller.isFinished) {
            if (currentFlingDirection != Direction.NONE) {
                // Snap to day after fling is finished.
                goToNearestOrigin()
            }
        } else {
            when {
                currentFlingDirection != Direction.NONE && forceFinishScroll() -> {
                    goToNearestOrigin()
                }

                mScroller.computeScrollOffset() -> {
                    currentOrigin.y = mScroller.currY.toFloat()
                    currentOrigin.x = mScroller.currX.toFloat()
                    postInvalidateOnAnimation()
                }
            }
        }
    }

    /**
     * Determine whether a given calendar day falls within the scroll limits set for this view.
     *
     * @param day the day to check
     * @return True if there are no limits or the day is within the limits.
     * @see setMinDay
     * @see setMaxDay
     */
    fun dayIsValid(day: DayOfWeek): Boolean {
        return (minDay == null || day >= minDay) && (maxDay == null || day <= maxDay)
    }

    fun disableDropListener() {
        this.mEnableDropListener = false
        setOnDragListener(null)
    }

    fun enableDropListener() {
        this.mEnableDropListener = true
        setOnDragListener(DragListener())
    }

    /**
     * Show a specific day on the week view.
     *
     * @param day The day to show.
     */
    fun goToDay(day: DayOfWeek) {
        mScroller.forceFinished(true)
        currentScrollDirection = Direction.NONE
        currentFlingDirection = Direction.NONE

        if (areDimensionsInvalid) {
            scrollToDay = day
            return
        }

        refreshEvents = true

        currentOrigin.x = -daysBetween(homeDay, day) * (widthPerDay + columnGap)
        invalidate()
    }

    /**
     * Show a specific day on the week view.
     *
     * @param day The day to show.
     */
    fun goToDay(day: Int) {
        goToDay(DayOfWeek.of(day))
    }

    /**
     * Vertically scroll to a specific hour in the week view.
     *
     * @param hour The hour to scroll to in 24-hour format. Supported values are 0-24.
     */
    fun goToHour(hour: Double) {
        if (areDimensionsInvalid) {
            scrollToHour = hour
            return
        }

        var verticalOffset = 0
        if (hour > mMaxTime) {
            verticalOffset = hourHeight * (mMaxTime - mMinTime)
        } else if (hour > mMinTime) {
            verticalOffset = (hourHeight * hour).toInt()
        }

        if (verticalOffset > hourHeight * (mMaxTime - mMinTime) - height + headerHeight + headerRowPadding * 2 + headerMarginBottom) {
            verticalOffset =
                (hourHeight * (mMaxTime - mMinTime) - height + headerHeight + headerRowPadding * 2 + headerMarginBottom).toInt()
        }

        currentOrigin.y = -verticalOffset.toFloat()
        invalidate()
    }

    /**
     * Scrolls the calendar to current day and time.
     */
    fun goToNow() {
        goToDay(now.dayOfWeek)
        goToHour(now.hour.toDouble())
    }

    /**
     * Show today on the week view.
     */
    fun goToToday() {
        goToDay(now.dayOfWeek)
    }

    override fun invalidate() {
        super.invalidate()

        areDimensionsInvalid = true
    }

    /**
     * Refreshes the view and loads the events again.
     */
    fun notifyDatasetChanged() {
        refreshEvents = true
        invalidate()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        val handled = gestureDetector.onTouchEvent(event)

        // Check after call of gestureDetector, so currentFlingDirection and currentScrollDirection are set.
        if (event.action == MotionEvent.ACTION_UP && !isZooming && currentFlingDirection == Direction.NONE) {
            if (currentScrollDirection == Direction.RIGHT || currentScrollDirection == Direction.LEFT) {
                goToNearestOrigin()
            }
            currentScrollDirection = Direction.NONE
        }

        return handled
    }

    /**
     * Set visible time span.
     *
     * @param startHour limit time display on top (between 0~24)
     * @param endHour limit time display at bottom (between 0~24 and larger than startHour)
     */
    fun setLimitTime(startHour: Int, endHour: Int) {
        if (endHour <= startHour) {
            throw IllegalArgumentException("endHour must larger startHour.")
        } else if (startHour < 0) {
            throw IllegalArgumentException("startHour must be at least 0.")
        } else if (endHour > 24) {
            throw IllegalArgumentException("endHour can't be higher than 24.")
        }

        this.mMinTime = startHour
        this.mMaxTime = endHour
        recalculateHourHeight()
        invalidate()
    }

//    interface ScrollListener {
//        /**
//         * Called when the first visible day has changed.
//         *
//         * (this will also be called during the first draw of the week view)
//         *
//         * @param newFirstVisibleDay The new first visible day
//         * @param oldFirstVisibleDay The old first visible day (is null on the first call).
//         */
//        fun onFirstVisibleDayChanged(newFirstVisibleDay: DayOfWeek, oldFirstVisibleDay: DayOfWeek?)
//    }
}