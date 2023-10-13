package com.example.baseproject.base.ui.demo_viewpager.enumz

import android.content.Context
import androidx.annotation.StringRes
import com.example.baseproject.R

enum class TabInfo(val position: Int, @StringRes val tabNameId: Int) {
    Tab1(0, R.string.txt_tab_name_1),
    Tab2(1, R.string.txt_tab_name_2),
    Tab3(2, R.string.txt_tab_name_3),
    Tab4(3, R.string.txt_tab_name_4);

    companion object {

        fun getTabCount() = values().size
        fun getTabByPosition(position: Int) = values().getOrNull(position) ?: Tab1

        fun getTabName(context: Context, position: Int): String {
            return context.getString(getTabByPosition(position).tabNameId)
        }
    }
}