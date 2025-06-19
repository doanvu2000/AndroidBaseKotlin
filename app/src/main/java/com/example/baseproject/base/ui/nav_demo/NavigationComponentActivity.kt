package com.example.baseproject.base.ui.nav_demo

import android.view.LayoutInflater
import com.example.baseproject.base.base_view.screen.BaseActivity
import com.example.baseproject.base.utils.extension.finishWithSlide
import com.example.baseproject.databinding.ActivityNavigationComponentBinding

class NavigationComponentActivity : BaseActivity<ActivityNavigationComponentBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityNavigationComponentBinding {
        return ActivityNavigationComponentBinding.inflate(inflater)
    }

    override fun initView() {

    }

    override fun initData() {

    }

    override fun initListener() {
        binding.btnBack.clickSafe(true) {
            finishWithSlide()
        }
    }
}