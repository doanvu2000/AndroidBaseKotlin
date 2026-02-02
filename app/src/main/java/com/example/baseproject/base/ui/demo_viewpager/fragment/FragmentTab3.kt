package com.example.baseproject.base.ui.demo_viewpager.fragment

import com.example.baseproject.base.base_view.screen.BaseFragment
import com.example.baseproject.base.ui.demo_viewpager.DemoViewPagerActivity
import com.example.baseproject.base.ui.demo_viewpager.enumz.TabInfo
import com.example.baseproject.databinding.FragmentTabDemoBinding

class FragmentTab3 : BaseFragment<FragmentTabDemoBinding>() {
    override fun initView() {

    }

    override fun initData() {
        val tabKey: Int? = arguments?.getInt(DemoViewPagerActivity.TAB_KEY)
        tabKey?.let {
            binding.tvDescription.text = TabInfo.getTabName(requireContext(), it)
        }
    }

    override fun initListener() {

    }
}