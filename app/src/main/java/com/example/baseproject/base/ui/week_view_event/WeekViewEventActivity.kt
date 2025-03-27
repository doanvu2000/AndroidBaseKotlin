package com.example.baseproject.base.ui.week_view_event

import android.content.ClipData
import android.graphics.Color
import android.graphics.RectF
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.example.baseproject.base.base_view.screen.BaseActivity
import com.example.baseproject.databinding.ActivityWeekViewEventBinding
import com.jin.DateUtils
import com.jin.DayTime
import com.jin.WeekViewEvent
import com.jin.week_view_event.listener.AddEventClickListener
import com.jin.week_view_event.listener.DayTimeInterpreter
import com.jin.week_view_event.listener.DropListener
import com.jin.week_view_event.listener.EmptyViewClickListener
import com.jin.week_view_event.listener.EmptyViewLongPressListener
import com.jin.week_view_event.listener.EventClickListener
import com.jin.week_view_event.listener.EventLongPressListener
import com.jin.week_view_event.listener.WeekViewLoader
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale
import java.util.Random

class WeekViewEventActivity : BaseActivity<ActivityWeekViewEventBinding>(),
    EventClickListener, WeekViewLoader,
    EventLongPressListener, EmptyViewLongPressListener,
    EmptyViewClickListener, AddEventClickListener, DropListener {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityWeekViewEventBinding {
        return ActivityWeekViewEventBinding.inflate(inflater)
    }

    companion object {
        private const val TYPE_DAY_VIEW = 1
        private const val TYPE_THREE_DAY_VIEW = 2
        private const val TYPE_WEEK_VIEW = 3
        private val random = Random()

        private fun randomColor(): Int {
            return Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256))
        }
    }

    private var mWeekViewType = TYPE_THREE_DAY_VIEW

    private fun getEventTitle(time: DayTime): String {
        return String.format(
            Locale.getDefault(),
            "Event of %s %02d:%02d",
            time.day?.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            time.hour,
            time.minute
        )
    }

    override fun onAddEventClicked(startTime: DayTime, endTime: DayTime) {
        Toast.makeText(this, "Add event clicked.", Toast.LENGTH_SHORT).show()
    }

    override fun initView() {
        binding.weekView.setOnEventClickListener(this)

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        binding.weekView.setWeekViewLoader(this)

        // Set long press listener for events.
        binding.weekView.setEventLongPressListener(this)

        // Set long press listener for empty view
        binding.weekView.setEmptyViewLongPressListener(this)

        // Set EmptyView Click Listener
        binding.weekView.setEmptyViewClickListener(this)

        // Set AddEvent Click Listener
        binding.weekView.setAddEventClickListener(this)

        // Set Drag and Drop Listener
        binding.weekView.setDropListener(this)

        // binding.weekView.setAutoLimitTime(true)
        // binding.weekView.setLimitTime(4, 16)

        // binding.weekView.setMinTime(10)
        // binding.weekView.setMaxTime(20)

        // Set up a date time interpreter to interpret how the date and time will be formatted in
        // the week view. This is optional.
        binding.weekView.setFirstDayOfWeek(DayOfWeek.MONDAY)
        setupDateTimeInterpreter()
    }

    override fun initData() {
    }

    override fun initListener() {
        binding.draggableView.setOnLongClickListener(DragTapListener())
    }

    override fun onDrop(view: View, day: DayTime) {
        Toast.makeText(this, "View dropped to $day", Toast.LENGTH_SHORT).show()
    }

    override fun onEmptyViewClicked(day: DayTime) {
        Toast.makeText(this, "Empty view clicked: ${getEventTitle(day)}", Toast.LENGTH_SHORT).show()
    }

    override fun onEmptyViewLongPress(time: DayTime) {
        Toast.makeText(this, "Empty view long pressed: ${getEventTitle(time)}", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onEventClick(event: WeekViewEvent, eventRect: RectF) {
        Toast.makeText(this, "Clicked $event", Toast.LENGTH_SHORT).show()
    }

    override fun onEventLongPress(event: WeekViewEvent, eventRect: RectF) {
        Toast.makeText(this, "Long pressed event: ${event.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onWeekViewLoad(): MutableList<WeekViewEvent> {
        // Populate the week view with some events.
        val events = mutableListOf<WeekViewEvent>()
        for (i in 0 until 10) {
            val startTime =
                DayTime(
                    DateUtils.getLocalDateTimeNow()
                        .plusHours((i * (random.nextInt(3) + 1)).toLong())
                )
            val endTime = DayTime(startTime).apply { addMinutes(random.nextInt(30) + 30) }
            val event = WeekViewEvent("ID$i", "Event $i", startTime, endTime).apply {
                color = randomColor()
            }
            events.add(event)
        }
        return events
    }

    /**
     * Set up a date time interpreter which will show short date values when in week view and long
     * date values otherwise.
     */
    private fun setupDateTimeInterpreter() {
        binding.weekView.setDayTimeInterpreter(object : DayTimeInterpreter {
            override fun interpretDay(date: Int): String {
                return DayOfWeek.of(date).getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }

            override fun interpretTime(hour: Int, minutes: Int): String {
                val strMinutes = String.format(Locale.getDefault(), "%02d", minutes)
                return if (hour > 11) {
                    if (hour == 12) "12:$strMinutes" else "${hour - 12}:$strMinutes PM"
                } else {
                    if (hour == 0) "12:$strMinutes AM" else "$hour:$strMinutes AM"
                }
            }
        })
    }

    private inner class DragTapListener : View.OnLongClickListener {
        override fun onLongClick(v: View): Boolean {
            val data = ClipData.newPlainText("", "")
            val shadowBuilder = View.DragShadowBuilder(v)
            v.startDrag(data, shadowBuilder, v, 0)
            return true
        }
    }
}