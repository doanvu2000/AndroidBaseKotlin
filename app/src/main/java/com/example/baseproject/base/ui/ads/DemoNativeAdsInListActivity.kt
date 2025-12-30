package com.example.baseproject.base.ui.ads

import android.view.LayoutInflater
import com.example.baseproject.base.base_view.screen.BaseActivity
import com.example.baseproject.base.utils.extension.setGridLayoutManagerIncludeAds
import com.example.baseproject.databinding.ActivityDemoNativeAdsInListBinding

class DemoNativeAdsInListActivity : BaseActivity<ActivityDemoNativeAdsInListBinding>() {
    override fun inflateViewBinding(inflater: LayoutInflater): ActivityDemoNativeAdsInListBinding {
        return ActivityDemoNativeAdsInListBinding.inflate(inflater)
    }

    private val lickingAdapter by lazy {
        LickingAdapter()
    }

    override fun initView() {
        binding.rcvTest.setGridLayoutManagerIncludeAds(
            this, 3, lickingAdapter
        )
    }

    override fun initData() {
        lickingAdapter.setDataList(
            listOf(
                1, 2, 3, -1, 5, 1, 6, 7, 8, 9, -1, 2, 11, 12, 13, 14, 15, -1,
                1, 2, 3, -1, 5, 1, 6, 7, 8, 9, -1, 2, 11, 12, 13, 14, 15, -1,
                1, 2, 3, -1, 5, 1, 6, 7, 8, 9, -1, 2, 11, 12, 13, 14, 15, -1,
            )
        )
    }

    override fun initListener() {

    }
}