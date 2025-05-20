package com.example.baseproject.base.ui.lunar_calendar

import android.annotation.SuppressLint
import android.view.LayoutInflater
import com.example.baseproject.base.base_view.screen.BaseActivity
import com.example.baseproject.base.utils.extension.getCurrentYear
import com.example.baseproject.base.utils.extension.getDay
import com.example.baseproject.base.utils.extension.getDayTitle
import com.example.baseproject.base.utils.extension.getHour
import com.example.baseproject.base.utils.extension.getMinutes
import com.example.baseproject.base.utils.extension.getMonth
import com.example.baseproject.base.utils.extension.toStringFormat
import com.example.baseproject.base.utils.lunar.calendar.getLunarDay
import com.example.baseproject.base.utils.lunar.calendar.getLunarMonth
import com.example.baseproject.base.utils.lunar.calendar.getLunarYear
import com.example.baseproject.databinding.ActivityLunarCalendarBinding
import java.util.Calendar

class LunarCalendarActivity : BaseActivity<ActivityLunarCalendarBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityLunarCalendarBinding {
        return ActivityLunarCalendarBinding.inflate(inflater)
    }

    companion object {
        const val NORMAL_DAY = 0
        const val GOOD_DAY = 1
        const val BAD_DAY = -1
        private var mSelectedDay: Day =
            Day(Calendar.getInstance(), DayState.ThisMonth, isToday = true, isSelected = true)
    }

    private var calendarSelected = Calendar.getInstance()

    private val calendarAdapter by lazy {
        CustomCalendarV2Adapter(
            this, 0, mSelectedDay
        )
    }

    override fun initView() {
        setupView()
    }

    override fun initData() {

    }

    override fun initListener() {
        binding.btnNextPage.clickSafe {
            val currentPage = binding.calendarViewPager.currentItem
            binding.calendarViewPager.currentItem = currentPage + 1
        }

        binding.btnPreviousPage.clickSafe {
            val currentPage = binding.calendarViewPager.currentItem
            binding.calendarViewPager.currentItem = currentPage - 1
        }
    }

    private fun setupView() {
//        binding.layoutDetailDay.rcvAusHour.setGridLayoutManager(this, 3, gioHoangDaoAdapter)

        binding.calendarViewPager.adapter = calendarAdapter

        binding.calendarViewPager.onDayClickListener = { daySelected, _ ->
            calendarSelected = daySelected.calendar
            updateView(daySelected.calendar)
        }
        binding.calendarViewPager.onCalendarChangeListener = {
            calendarSelected = it
            updateTitleFormat(it)
        }

        initFirstValue()
    }

    private fun initFirstValue() {
        val calendar = Calendar.getInstance()
        updateTitle(calendar.getMonth(), getCurrentYear())
        updateView(calendar)
    }

    @SuppressLint("SetTextI18n")
    private fun updateTitle(month: Int, year: Int) {
        binding.tvTime.text = "Tháng ${month.toStringFormat()}, $year"
    }

    @SuppressLint("SetTextI18n")
    private fun updateView(calendar: Calendar) {
        calendar.apply {
            set(Calendar.HOUR_OF_DAY, Calendar.getInstance().getHour())
            set(Calendar.MINUTE, Calendar.getInstance().getMinutes())
        }
        val day = calendar.getDay()
        val month = calendar.getMonth()
        val dayTitle = calendar.getDayTitle().uppercase()

        val lunarDay = calendar.getLunarDay()
        val lunarMonth = calendar.getLunarMonth()
        val lunarYear = calendar.getLunarYear()

//        binding.layoutDetailDay.apply {
//            tvCurrentDayTitle.text = dayTitle
//            tvCurrentDayNumber.text = "${day.toStringFormat()}/${month.toStringFormat()}"
//            tvCurrentDayLunar.text =
//                "Âm lịch ${lunarDay.toStringFormat()}/${lunarMonth.toStringFormat()}"
//
//            val dayStatus = calendar.getDayStatus()
//            when (dayStatus) {
//                GOOD_DAY -> {
//                    tvCurrentDayNumber.setTextColor(getColorById(R.color.p2))
//                }
//
//                NORMAL_DAY -> {
//                    tvCurrentDayNumber.setTextColor(getColorById(R.color.n4))
//                }
//
//                BAD_DAY -> {
//                    tvCurrentDayNumber.setTextColor(getColorById(R.color.n1))
//                }
//            }
//
//            tvDesHour.text = "Giờ: " + calendar.getCanChiForHour()
//            tvDesDay.text = "Ngày: " + calendar.getCanChiForDay()
//            tvDesMonth.text = "Tháng: " + calendar.getCanChiForMonth()
//            tvDesYear.text = "Năm: " + lunarYear.getCanChiYear()
//
//            gioHoangDaoAdapter.canOfDay = calendar.getCanForDay()
//            val gioHoangDao = calendar.getGioHoangDao()
//            gioHoangDaoAdapter.setDataList(gioHoangDao)
//        }
    }

    private fun updateTitleFormat(calendar: Calendar) {
        val selectedMonth = calendar.get(Calendar.MONTH) + 1
        val selectedYear = calendar.get(Calendar.YEAR)
        updateTitle(selectedMonth, selectedYear)
    }
}