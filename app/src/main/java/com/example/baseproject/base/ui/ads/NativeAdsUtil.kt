package com.example.baseproject.base.ui.ads

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.example.baseproject.BuildConfig
import com.example.baseproject.base.utils.extension.isDebugMode
import com.example.baseproject.base.utils.util.Constants
import com.example.baseproject.databinding.LayoutNativeAds2Binding
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

object NativeAdsUtil {

    private const val NATIVE_ID_TEST = BuildConfig.ADS_NATIVE_TEST
    private const val NATIVE_ID_TEST_VIDEO = BuildConfig.ADS_NATIVE_TEST_VIDEO
    private const val NATIVE_ID_1 = BuildConfig.ADS_NATIVE_ID1
    private const val NATIVE_ID_2 = BuildConfig.ADS_NATIVE_ID2
    fun loadNativeAds(
        context: Context,
        loadSuccess: (nativeAd: NativeAd?) -> Unit,
        loadFailed: () -> Unit
    ) {
        var nativeAd: NativeAd? = null
        val adLoader: AdLoader?
//        val id = if (isDebugMode()) {
//            NATIVE_ID_TEST
//        } else {
//            NATIVE_ID_1
//        }
        val id = NATIVE_ID_1
        val builder = AdLoader.Builder(context, id)
        adLoader = builder.forNativeAd { nativeAds ->
            nativeAd = nativeAds
        }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.d(Constants.TAG, "Native-loadAds1 success")
                    loadSuccess.invoke(nativeAd)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    Log.e(Constants.TAG, "load native ad1 failed")
                    reloadNativeAdWithId2(context, loadSuccess, loadFailed)
                }
            }).build()
        adLoader.loadAd(AdRequest.Builder().build())
    }

    fun reloadNativeAdWithId2(
        context: Context,
        loadSuccess: (nativeAd: NativeAd?) -> Unit,
        loadFailed: () -> Unit
    ) {
        var nativeAd: NativeAd? = null
        var adLoader: AdLoader?
        Log.d(Constants.TAG, "loadAds2")
        val id = if (isDebugMode()) {
            NATIVE_ID_TEST
        } else {
            NATIVE_ID_2
        }
        val builder = AdLoader.Builder(context, id)
        adLoader = builder.forNativeAd { nativeAds: NativeAd? ->
            nativeAd = nativeAds
        }
            .withAdListener(object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    loadSuccess.invoke(nativeAd)
                    Log.d(Constants.TAG, "Native-loadAds2 success")
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.e(Constants.TAG, "Native-loadAds2 failed")
                    super.onAdFailedToLoad(loadAdError)
                    loadFailed.invoke()
                    adLoader = null
                }
            }).build()
        adLoader?.loadAd(AdRequest.Builder().build())
    }

    fun populateNativeAdView(
        nativeAd: NativeAd?,
        adView: NativeAdView,
        binding: LayoutNativeAds2Binding?
    ) {
        if (nativeAd != null) {
            adView.mediaView = binding?.adMedia
            adView.headlineView = binding?.adHeadLine
            adView.bodyView = binding?.adBody
            adView.callToActionView = binding?.adCallToAction
            adView.iconView = binding?.adIcon
//            adView.advertiserView = binding?.adAdvertiser
            (adView.headlineView as TextView?)?.text = nativeAd.headline
            if (nativeAd.body == null) {
                adView.bodyView?.visibility = View.INVISIBLE
            } else {
                adView.bodyView?.visibility = View.VISIBLE
                (adView.bodyView as TextView?)?.text = nativeAd.body
            }
            if (nativeAd.callToAction == null) {
                adView.callToActionView?.visibility = View.INVISIBLE
            } else {
                adView.callToActionView?.visibility = View.VISIBLE
                (adView.callToActionView as Button?)?.text = nativeAd.callToAction
            }
            if (nativeAd.icon == null) {
                adView.iconView?.visibility = View.GONE
            } else {
                (adView.iconView as ImageView?)?.setImageDrawable(
                    nativeAd.icon?.drawable
                )
                adView.iconView?.visibility = View.VISIBLE
            }
            adView.setNativeAd(nativeAd)
        }
    }
}