package com.example.baseproject.base.ui.lunar_calendar

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import org.apache.commons.lang3.time.DateUtils
import java.util.Calendar
import java.util.Date

open class CalendarPagerAdapter(
    val context: Context,
    var scrollToPosition: Int,
    var mSelectedDay: Day?,
    base: Calendar = Calendar.getInstance(),
    val startingAt: DayOfWeek = DayOfWeek.Monday
) : androidx.viewpager.widget.PagerAdapter() {
    private val baseCalendar: Calendar = DateUtils.truncate(base, Calendar.DAY_OF_MONTH).apply {
        set(Calendar.DAY_OF_MONTH, 1)
        firstDayOfWeek = Calendar.MONDAY + startingAt.getDifference()
        minimalDaysInFirstWeek = 1
    }
    private var viewContainer: ViewGroup? = null

    private var recyclerView: RecyclerView? = null

    var selectedDay: Date? = null
        set(value) {
            field = value
            notifyCalendarItemChanged()
        }
    var onDayClickLister: ((Day, Int) -> Unit?)? = null

    var onDayLongClickListener: ((Day) -> Boolean)? = null

    companion object {
        const val MAX_VALUE = 1000
    }

    override fun getCount(): Int = MAX_VALUE

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        recyclerView = RecyclerView(context).apply {
            layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, 7) // 1週間 なので
            isNestedScrollingEnabled = false
            hasFixedSize()
            mSelectedDay?.let {
                this@CalendarPagerAdapter.selectedDay = it.calendar.time
                notifyCalendarItemChanged()
            }
            scrollToSelectedPosition(scrollToPosition)

            adapter = object :
                CalendarCellAdapter(context, getCalendar(position), startingAt, selectedDay) {
                override fun onBindViewHolder(
                    holder: RecyclerView.ViewHolder,
                    day: Day,
                    selectedPosition: Int
                ) {
                    holder.itemView.setOnClickListener {
                        this@CalendarPagerAdapter.selectedDay = day.calendar.time
                        this@CalendarPagerAdapter.onDayClickLister?.invoke(day, selectedPosition)
                        notifyCalendarItemChanged()
                    }
                    holder.itemView.setOnLongClickListener {
                        if (this@CalendarPagerAdapter.onDayLongClickListener != null) {
                            this@CalendarPagerAdapter.onDayLongClickListener!!.invoke(day)
                        } else {
                            false
                        }
                    }
                    this@CalendarPagerAdapter.onBindView(holder.itemView, day, selectedPosition)
                }

                override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
                    object : RecyclerView.ViewHolder(
                        this@CalendarPagerAdapter.onCreateView(
                            parent,
                            viewType
                        )
                    ) {}

            }
        }
        container.addView(
            recyclerView,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )
        viewContainer = container

        return recyclerView as RecyclerView
    }

    open fun setSelectedPosition(position: Int) {
        scrollToPosition = position
    }

    open fun setSelectedDay(day: Day) {
        mSelectedDay = day
    }

    open fun scrollToSelectedPosition(position: Int) {
        recyclerView?.scrollToPosition(position)
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View?)
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean = (view == `object`)

    fun getCalendar(position: Int): Calendar {
        return (baseCalendar.clone() as Calendar).apply {
            add(Calendar.MONTH, position - MAX_VALUE / 2)
        }
    }

    fun notifyCalendarChanged() {
        val views = viewContainer ?: return
        (0 until views.childCount).forEach { i ->
            ((views.getChildAt(i) as? RecyclerView)?.adapter as? CalendarCellAdapter)?.run {
                notifyItemRangeChanged(0, items.size)
            }
        }
    }

    fun notifyCalendarItemChanged() {
        val views = viewContainer ?: return
        (0 until views.childCount).forEach { i ->
            ((views.getChildAt(i) as? RecyclerView)?.adapter as? CalendarCellAdapter)?.updateItems(
                selectedDay
            )
        }
    }

    open fun onCreateView(parent: ViewGroup, viewType: Int): View {
        return TextView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 96)
        }
    }

    open fun onBindView(view: View, day: Day, selectedPosition: Int) {
        val textView = view as TextView
        textView.text = when (day.state) {
            DayState.ThisMonth -> day.calendar.get(Calendar.DAY_OF_MONTH).toString()
            else -> ""
        }
    }

    enum class DayOfWeek {
        Monday,
        Tuesday,
        Wednesday,
        Thursday,
        Friday,
        Saturday,
        Sunday;

        fun getDifference(): Int {
            return when (this) {
                Monday -> 0
                Tuesday -> 1
                Wednesday -> 2
                Thursday -> 3
                Friday -> 4
                Saturday -> 5
                Sunday -> 6
            }
        }

        fun isLessFirstWeek(calendar: Calendar): Boolean {
            return calendar.get(Calendar.DAY_OF_WEEK) < getDifference() + 1
        }

        fun isMoreLastWeek(calendar: Calendar): Boolean {
            val end = DateUtils.truncate(calendar, Calendar.DAY_OF_MONTH)
            end.add(Calendar.MONTH, 1)
            end.add(Calendar.DATE, -1)
            return end.get(Calendar.DAY_OF_WEEK) < getDifference() + 1
        }
    }
}
