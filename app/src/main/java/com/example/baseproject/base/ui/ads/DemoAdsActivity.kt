package com.example.baseproject.base.ui.ads

import android.annotation.SuppressLint
import android.view.LayoutInflater
import com.example.baseproject.base.base_view.BaseActivity
import com.example.baseproject.base.utils.extension.gone
import com.example.baseproject.base.utils.extension.show
import com.example.baseproject.base.utils.extension.showSnackBar
import com.example.baseproject.databinding.ActivityDemoAdsBinding

class DemoAdsActivity : BaseActivity<ActivityDemoAdsBinding>() {
    override fun initView() {

    }

    override fun initData() {
        initAds()
    }

    @SuppressLint("SetTextI18n")
    private fun initAds() {
        AdmobUtils.getInstance(this).showBannerNormal(this, binding.banner,
            onBannerShowSuccess = {
                binding.tvBanner.text = "Banner ads: load success"
                binding.loadingBanner.gone()
            },
            onBannerShowFailed = {
                binding.tvBanner.text = "Banner ads: load failed"
                binding.loadingBanner.gone()
            })
        AdmobUtils.getInstance(this).initInter(this)
        AdmobUtils.getInstance(this).dialogLoadingInter = DialogLoadingInter(this)
        NativeAdsUtil.loadNativeAds(this, loadSuccess = {
            binding.tvNativeAds.text = "Native ads: load success"
            binding.loadingNative.gone()
            binding.layoutNativeAd.background.show()
            binding.layoutNativeAd.layoutBase.gone()
            NativeAdsUtil.populateNativeAdView(it, binding.layoutNativeAd.root, binding.layoutNativeAd)
        }, loadFailed = {
            binding.tvNativeAds.text = "Native ads: load failed"
            binding.loadingNative.gone()
        })
    }

    override fun initListener() {
        binding.btnShowInter.clickSafety {
            AdmobUtils.getInstance(this).showInter(onDone = {
            }, showInterFailed = {
                showSnackBar(it ?: "Error when show inter", 3000, true)
            })
        }
    }

    override fun inflateViewBinding(inflater: LayoutInflater): ActivityDemoAdsBinding {
        return ActivityDemoAdsBinding.inflate(inflater)
    }
}