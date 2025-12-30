package com.example.baseproject.base.ui.ads

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.example.baseproject.base.base_view.screen.BaseAdapterRecyclerView
import com.example.baseproject.base.utils.extension.gone
import com.example.baseproject.base.utils.extension.show
import com.example.baseproject.databinding.LickingAdsBinding
import com.example.baseproject.databinding.LickingContentBinding

class LickingAdapter : BaseAdapterRecyclerView<Int, ViewBinding>() {
    override fun inflateBinding(
        inflater: LayoutInflater, parent: ViewGroup, viewType: Int
    ): ViewBinding {
        return when (viewType) {
            LickingViewType.Ads.ordinal -> {
                LickingAdsBinding.inflate(inflater, parent, false)
            }

            LickingViewType.Content.ordinal -> {
                LickingContentBinding.inflate(inflater, parent, false)
            }

            else -> LickingContentBinding.inflate(inflater, parent, false)
        }
    }

    override fun bindData(
        binding: ViewBinding, item: Int, position: Int
    ) {
        if (binding is LickingContentBinding) {
            //fill data content
        } else if (binding is LickingAdsBinding) {
            loadAndShowNativeAds(binding, position)
        }
    }

    private fun loadAndShowNativeAds(binding: LickingAdsBinding, position: Int) {
        if (NativeCacheManager.nativeIsReadyInPosition(position)) {
            //populate native from cache
            NativeCacheManager.nativeInPosition(position)?.let { ad ->
                binding.root.show()
                binding.background.show()
                binding.layoutBase.gone()
                NativeAdsUtil.populateNativeAdView(ad, binding.nativeAdView, binding)
            }
        } else if (NativeCacheManager.nativeIsLoadingInPosition(position)) {
            return
        } else {
            NativeCacheManager.setNativeLoadingInPosition(position)
            NativeAdsUtil.loadNativeAds(binding.root.context, loadSuccess = { ad ->
                ad?.let {
                    NativeCacheManager.setNativeLoadedInPosition(position, ad)
                }
                binding.root.show()
                binding.background.show()
                binding.layoutBase.gone()
                NativeAdsUtil.populateNativeAdView(ad, binding.nativeAdView, binding)
            }, loadFailed = {
                binding.root.gone()
                NativeCacheManager.setNativeFailedInPosition(position)
            })
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (dataList[position] == -1) {
            LickingViewType.Ads.ordinal
        } else {
            LickingViewType.Content.ordinal
        }
    }
}