package com.example.baseproject.base.ui.demo_viewpager.adapter

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.baseproject.base.ui.demo_viewpager.DemoViewPagerActivity
import com.example.baseproject.base.ui.demo_viewpager.enumz.TabInfo
import com.example.baseproject.base.ui.demo_viewpager.fragment.FragmentTab1
import com.example.baseproject.base.ui.demo_viewpager.fragment.FragmentTab2
import com.example.baseproject.base.ui.demo_viewpager.fragment.FragmentTab3
import com.example.baseproject.base.ui.demo_viewpager.fragment.FragmentTab4

class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return TabInfo.getTabCount()
    }

    override fun createFragment(position: Int): Fragment {
        val bundle = bundleOf(DemoViewPagerActivity.TAB_KEY to position)
        return when (TabInfo.getTabByPosition(position)) {
            TabInfo.Tab1 -> FragmentTab1().apply {
                arguments = bundle
            }

            TabInfo.Tab2 -> FragmentTab2().apply {
                arguments = bundle
            }

            TabInfo.Tab3 -> FragmentTab3().apply {
                arguments = bundle
            }

            TabInfo.Tab4 -> FragmentTab4().apply {
                arguments = bundle
            }
        }
    }
}