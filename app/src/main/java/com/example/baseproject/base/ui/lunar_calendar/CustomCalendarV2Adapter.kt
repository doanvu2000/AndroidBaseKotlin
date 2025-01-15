package com.example.baseproject.base.ui.lunar_calendar

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorRes
import androidx.core.content.res.ResourcesCompat
import com.example.baseproject.R
import com.example.baseproject.base.utils.extension.getDay
import com.example.baseproject.base.utils.extension.getMonth
import com.example.baseproject.base.utils.extension.getYear
import com.example.baseproject.base.utils.extension.hide
import com.example.baseproject.base.utils.extension.show
import com.example.baseproject.base.utils.lunar.calendar.LunisolarCalendar
import com.example.baseproject.databinding.ViewCalendarCellNewBinding
import java.util.Calendar

class CustomCalendarV2Adapter(
    context: Context,
    scrollToPosition: Int,
    selectedDay: Day?
) : CalendarPagerAdapter(context, scrollToPosition, selectedDay) {

    private var mConvertSolarToLunar: LunisolarCalendar? = null

    companion object {
        const val NORMAL_DAY = 0
        const val GOOD_DAY = 1
        const val BAD_DAY = -1
    }

    override fun onCreateView(parent: ViewGroup, viewType: Int): View {
        return LayoutInflater.from(context).inflate(R.layout.view_calendar_cell_new, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindView(view: View, day: Day, selectedPosition: Int) {
        val binding = ViewCalendarCellNewBinding.bind(view)
        val context = binding.root.context
        if (day.state == DayState.ThisMonth) {
            view.show()
            binding.txtSolarDay.text = day.calendar.getDay().toString()
            if (mConvertSolarToLunar != null) {
                mConvertSolarToLunar = null
            }
            mConvertSolarToLunar = LunisolarCalendar()
            mConvertSolarToLunar?.solar2lunar(
                day.calendar.getDay(),
                day.calendar.getMonth(),
                day.calendar.getYear()
            )
            if (mConvertSolarToLunar?.day == 1) {
                binding.txtLunarDay.text =
                    mConvertSolarToLunar?.day.toString() + "/" + mConvertSolarToLunar?.month
            } else {
                binding.txtLunarDay.text = mConvertSolarToLunar?.day.toString()
            }

            val dayStatus = mConvertSolarToLunar?.getGoodDay(
                day.calendar.get(Calendar.DAY_OF_MONTH),
                day.calendar.get(Calendar.MONTH) + 1,
                day.calendar.get(Calendar.YEAR)
            )
            when (dayStatus) {
                GOOD_DAY -> {
                    val background = if (day.isSelected) {
                        if (day.isToday) {
                            R.drawable.bg_item_calendar_good_day_select
                        } else {
                            R.drawable.bg_item_calendar_good_day
                        }
                    } else {
                        if (day.isToday) {
                            R.drawable.bg_item_calendar_current_day_select
                        } else {
                            R.drawable.bg_item_calendar_default
                        }
                    }
                    binding.txtSolarDay.setTextColor(context.getColorById(R.color.good_day))
                    binding.root.setBackgroundResource(background)
                }

                NORMAL_DAY -> {
                    val background = if (day.isSelected) {
                        if (day.isToday) {
                            R.drawable.bg_item_calendar_normal_day_select
                        } else {
                            R.drawable.bg_item_calendar_normal_day
                        }
                    } else {
                        if (day.isToday) {
                            R.drawable.bg_item_calendar_current_day_select
                        } else {
                            R.drawable.bg_item_calendar_default
                        }
                    }
                    binding.txtSolarDay.setTextColor(context.getColorById(R.color.normal_day))
                    binding.root.setBackgroundResource(background)
                }

                else -> {
                    val background = if (day.isSelected) {
                        if (day.isToday) {
                            R.drawable.bg_item_calendar_bad_day_select
                        } else {
                            R.drawable.bg_item_calendar_bad_day
                        }
                    } else {
                        if (day.isToday) {
                            R.drawable.bg_item_calendar_current_day_select
                        } else {
                            R.drawable.bg_item_calendar_default
                        }
                    }
                    binding.txtSolarDay.setTextColor(context.getColorById(R.color.bad_day))
                    binding.root.setBackgroundResource(background)
                }
            }

        } else {
            view.hide()
        }
    }

    fun setSelectedItem(calendar: Calendar) {
        selectedDay = calendar.time
        notifyCalendarItemChanged()
    }
}

fun Context.getColorById(@ColorRes id: Int): Int {
    return ResourcesCompat.getColor(resources, id, null)
}