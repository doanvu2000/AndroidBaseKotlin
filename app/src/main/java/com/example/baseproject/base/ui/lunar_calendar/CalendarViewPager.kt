package com.example.baseproject.base.ui.lunar_calendar

import android.content.Context
import android.util.AttributeSet
import java.util.Calendar

open class CalendarViewPager(context: Context, attrs: AttributeSet? = null) :
    androidx.viewpager.widget.ViewPager(context, attrs) {

    var onDayClickListener: ((Day, Int) -> Unit?)? = null
        set(value) {
            field = value
            (adapter as? CalendarPagerAdapter)?.onDayClickLister = field
        }

    var onDayLongClickListener: ((Day) -> Boolean)? = null
        set(value) {
            field = value
            (adapter as? CalendarPagerAdapter)?.onDayLongClickListener = field
        }

    var onCalendarChangeListener: ((Calendar) -> Unit)? = null

    override fun setAdapter(adapter: androidx.viewpager.widget.PagerAdapter?) {
        super.setAdapter(adapter)
        if (adapter is CalendarPagerAdapter) {
            this.clearOnPageChangeListeners()

            adapter.onDayClickLister = this.onDayClickListener
            adapter.onDayLongClickListener = this.onDayLongClickListener

            setCurrentItem(CalendarPagerAdapter.MAX_VALUE / 2, false)
            this.addOnPageChangeListener(pageChangeListener)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val mode = MeasureSpec.getMode(heightMeasureSpec)
        if (mode == MeasureSpec.AT_MOST) {
            val view = focusedChild ?: getChildAt(0)
            view.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED))
            val newHeight = view.measuredHeight

            val exactlyHeightMeasureSpec =
                MeasureSpec.makeMeasureSpec(newHeight, MeasureSpec.EXACTLY)
            super.onMeasure(widthMeasureSpec, exactlyHeightMeasureSpec)
        }
    }

    fun getCurrentCalendar(): Calendar? =
        (adapter as? CalendarPagerAdapter)?.getCalendar(currentItem)

    fun moveItemBy(position: Int, smoothScroll: Boolean = true) {
        if (position != 0) {
            setCurrentItem(currentItem + position, smoothScroll)
        }
    }

    private val pageChangeListener = object : OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {}
        override fun onPageScrolled(
            position: Int,
            positionOffset: Float,
            positionOffsetPixels: Int
        ) {
        }

        override fun onPageSelected(position: Int) {
            val calendar = (adapter as? CalendarPagerAdapter)?.getCalendar(position) ?: return
            onCalendarChangeListener?.invoke(calendar)
        }
    }
}
