package com.example.baseproject.base.ui.demo_viewpager

import android.view.LayoutInflater
import com.example.baseproject.base.base_view.screen.BaseActivity
import com.example.baseproject.base.ui.demo_viewpager.adapter.ViewPagerAdapter
import com.example.baseproject.base.ui.demo_viewpager.enumz.TabInfo
import com.example.baseproject.base.utils.extension.finishWithSlide
import com.example.baseproject.databinding.ActivityDemoViewPagerBinding
import com.google.android.material.tabs.TabLayoutMediator

class DemoViewPagerActivity : BaseActivity<ActivityDemoViewPagerBinding>() {

    companion object {
        const val TAB_KEY = "tab_key"
    }

    override fun initView() {
        initViewPager()
    }

    private fun initViewPager() {
        binding.viewPagerGallery.adapter = ViewPagerAdapter(supportFragmentManager, lifecycle)
        TabLayoutMediator(binding.tabLayout, binding.viewPagerGallery) { tabItem, position ->
            tabItem.text = TabInfo.getTabName(this@DemoViewPagerActivity, position)
        }.attach()
    }

    override fun initData() {

    }

    override fun initListener() {
        binding.btnBack.clickSafe {
            onBack()
        }
    }

    override fun onBack() {
        finishWithSlide()
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityDemoViewPagerBinding {
        return ActivityDemoViewPagerBinding.inflate(inflater)
    }
}