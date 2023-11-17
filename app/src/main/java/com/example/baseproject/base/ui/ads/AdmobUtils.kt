package com.example.baseproject.base.ui.ads

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.example.baseproject.BuildConfig
import com.example.baseproject.base.utils.Constant
import com.example.baseproject.base.utils.extension.getAdSizeFollowScreen
import com.example.baseproject.base.utils.extension.removeSelf
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

class AdmobUtils constructor(val context: Activity) {
    private val ADS_INTER_TEST = BuildConfig.ADS_INTER_TEST
    private val ADS_BANNER_TEST = BuildConfig.ADS_BANNER_TEST
    private var mInterstitialAd: InterstitialAd? = null
    private val adRequest: AdRequest
    private val adListener: AdListener? = null

    init {
        MobileAds.initialize(context) { initializationStatus: InitializationStatus ->
            Log.i(
                TAG, "onInitializationComplete: $initializationStatus"
            )
        }
        val testDevices: MutableList<String> = ArrayList()
        testDevices.add(AdRequest.DEVICE_ID_EMULATOR)
        val requestConfiguration = RequestConfiguration.Builder().setTestDeviceIds(testDevices).build()
        MobileAds.setRequestConfiguration(requestConfiguration)
        Log.d(TAG, "Test device: " + MobileAds.getRequestConfiguration().testDeviceIds)
        adRequest = AdRequest.Builder().build()
        Log.d(TAG, "isTestDevice: " + adRequest.isTestDevice(context))
    }

    fun loadBannerCollapsible(activity: Activity, collapsible: String?) {
        val extras = Bundle()
        extras.putString(COLLAPSIBLE, collapsible)
        val adRequest: AdRequest
        adRequest = if (collapsible != null) {
            AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
        } else {
            AdRequest.Builder().build()
        }
        var adUnit = BuildConfig.ADS_ADMOB_BANNER
        if (BuildConfig.DEBUG) {
            adUnit = ADS_BANNER_TEST
        }
        adView = AdView(activity)
        adView!!.setAdSize(activity.getAdSizeFollowScreen())
        adView!!.adUnitId = adUnit
        adView!!.loadAd(adRequest)
        adView!!.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.d(TAG, "onAdLoaded: banner collapsible loaded")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.e(TAG, "onAdLoaded: banner collapsible load failed")
            }
        }
    }

    fun showBannerCollapsible(activity: Activity, viewGroup: ViewGroup) {
//        if (RemoteConfigUtil.isShowBannerAd()) {
//            
//        }
        if (adView != null) {
            adView.removeSelf()
            viewGroup.addView(adView)
            viewGroup.visibility = View.VISIBLE
        } else {
            showBannerCollapsible(activity, viewGroup, COLLAPSIBLE_BANNER_BOTTOM)
        }
    }

    fun showBannerCollapsible(activity: Activity, viewGroup: ViewGroup, collapsible: String?) {
        val extras = Bundle()
        extras.putString(COLLAPSIBLE, collapsible)
        val adRequest: AdRequest
        adRequest = if (collapsible != null) {
            AdRequest.Builder().addNetworkExtrasBundle(AdMobAdapter::class.java, extras).build()
        } else {
            AdRequest.Builder().build()
        }
        var adUnit = BuildConfig.ADS_ADMOB_BANNER
        if (BuildConfig.DEBUG) {
            adUnit = ADS_BANNER_TEST
        }
        val adView = AdView(activity)
        adView.setAdSize(activity.getAdSizeFollowScreen())
        adView.adUnitId = adUnit
        adView.loadAd(adRequest)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.d(TAG, "onAdLoaded: banner collapsible loaded")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.e(TAG, "onAdLoaded: banner collapsible load failed")
            }
        }
        viewGroup.addView(adView)
        viewGroup.visibility = View.VISIBLE
    }

    fun showBannerNormal(
        activity: Activity, viewGroup: ViewGroup,
        onBannerShowSuccess: (() -> Unit)? = null,
        onBannerShowFailed: (() -> Unit)? = null
    ) {
        val adRequest: AdRequest = AdRequest.Builder().build()
        var adUnit = BuildConfig.ADS_ADMOB_BANNER
        if (BuildConfig.DEBUG) {
            adUnit = ADS_BANNER_TEST
        }
        val adView = AdView(activity)
        adView.apply {
            setAdSize(activity.getAdSizeFollowScreen())
            adUnitId = adUnit
            loadAd(adRequest)
            adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    Log.d(TAG, "onAdLoaded: banner loaded")
                    onBannerShowSuccess?.invoke()
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    Log.e(TAG, "onAdLoaded: banner load failed")
                    onBannerShowFailed?.invoke()
                }
            }
        }

        viewGroup.addView(adView)
        viewGroup.visibility = View.VISIBLE
    }

    fun initInter(context: Activity?) {
        if (mInterstitialAd != null) {
            return
        }
        InterstitialAd.load(context!!, BuildConfig.ADS_ADMOB_INTER_ID1, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Handle the error
                mInterstitialAd = null
                initInter2(context)
            }
        })
    }

    fun initInter2(context: Activity?) {
        //đổi key
        if (mInterstitialAd != null) {
            return
        }
        InterstitialAd.load(context!!, BuildConfig.ADS_ADMOB_INTER_ID2, adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                // The mInterstitialAd reference will be null until
                // an ad is loaded.
                mInterstitialAd = interstitialAd
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                // Handle the error
                mInterstitialAd = null
            }
        })
    }

    fun showInter(
        onDone: () -> Unit,
        showInterFailed: ((String?) -> Unit)? = null
    ) {
        if (mInterstitialAd != null) {
            dialogLoadingInter.show()
            Handler(Looper.getMainLooper()).postDelayed({
                mInterstitialAd?.show(context)
                mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                    override fun onAdDismissedFullScreenContent() {
                        dialogLoadingInter.hide()
                        onDone()
                        mInterstitialAd = null
                        initInter(context)
                        super.onAdDismissedFullScreenContent()
                    }

                    override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                        dialogLoadingInter.hide()
                        onDone()
                        mInterstitialAd = null
                        initInter(context)
                        super.onAdFailedToShowFullScreenContent(adError)
                    }

                    override fun onAdShowedFullScreenContent() {
                        dialogLoadingInter.hide()
                        super.onAdShowedFullScreenContent()
                    }
                }
            }, TIME_SHOW_DIALOG_INTER)
        } else {
            initInter(context)
        }
    }

    fun getmInterstitialAd(): InterstitialAd? {
        return mInterstitialAd
    }

    fun setmInterstitialAd(interstitialAd: InterstitialAd?) {
        mInterstitialAd = interstitialAd
    }

    fun resetmInterstitialAd() {
        mInterstitialAd = null
    }

    lateinit var dialogLoadingInter: DialogLoadingInter

    companion object {
        const val COLLAPSIBLE = "collapsible"
        const val COLLAPSIBLE_BANNER_BOTTOM = "bottom"
        private val TAG = Constant.TAG
        var adView: AdView? = null
        private var instance: AdmobUtils? = null
        private const val TIME_SHOW_DIALOG_INTER: Long = 1500
        fun getInstance(context: Activity): AdmobUtils {
            if (instance == null) {
                instance = AdmobUtils(context)
            }
            return instance!!
        }
    }
}