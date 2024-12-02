package com.example.baseproject.base.ui.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import com.example.baseproject.BuildConfig
import com.example.baseproject.base.ui.main.MyApplication
import com.example.baseproject.base.utils.util.Constants
import com.example.baseproject.base.utils.util.delayResetFlagPermission
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import java.util.Date

class AppOpenAdManager {
    companion object {
        const val AD_UNIT_ADMOB_TEST = BuildConfig.ADS_ADMOB_OPEN_APP_TEST
        const val TAG = Constants.TAG + "-AppOpenAds"
    }

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    var isShowingAd = false

    /** Keep track of the time an app open ad is loaded to ensure you don't show an expired ad. */
    private var loadTime: Long = 0

    fun loadAd(context: Context) {
        if (!isShowAds()) {
            return
        }
        // Do not load ad if there is an unused ad or one is already loading.
        if (isLoadingAd || isAdAvailable()) {
            return
        }
        loadAd1(context)
    }

    private fun loadAd1(context: Context) {
        var adUnit = BuildConfig.ADS_ADMOB_OPEN_APP_ID1.trim()
        if (BuildConfig.DEBUG) {
            adUnit = AD_UNIT_ADMOB_TEST.trim()
        }
        isLoadingAd = true
        Log.d(TAG, "-------------------loading OpenAds (id1)-------------------")
        val request = AdRequest.Builder().build()
        AppOpenAd.load(context, adUnit, request, object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                isLoadingAd = false
                loadTime = Date().time
                Log.d(TAG, "openAds onAdLoaded. - id1")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e(TAG, "openAds - onAdFailedToLoad: id1" + loadAdError.message)
                loadAd2(context)
            }
        })
    }

    private fun loadAd2(context: Context) {
        var adUnit = BuildConfig.ADS_ADMOB_OPEN_APP_ID2.trim()
        if (BuildConfig.DEBUG) {
            adUnit = AD_UNIT_ADMOB_TEST.trim()
        }
        Log.d(TAG, "-------------------loading OpenAds (id2)-------------------")
        val request = AdRequest.Builder().build()
        AppOpenAd.load(context, adUnit, request, object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) {
                appOpenAd = ad
                isLoadingAd = false
                loadTime = Date().time
                Log.d(TAG, "openAds onAdLoaded. - id2")
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                isLoadingAd = false
                Log.e(TAG, "openAds - onAdFailedToLoad: id2" + loadAdError.message)
            }
        })
    }

    /** Check if ad was loaded more than n hours ago. */
    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    /** Check if ad exists and can be shown. */
    private fun isAdAvailable(): Boolean {
        // Ad references in the app open beta will time out after four hours, but this time limit
        // may change in future beta versions. For details, see:
        // https://support.google.com/admob/answer/9341964?hl=en
//        return maxAppOpenAd != null || (appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4))
        return (appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4))
    }

    /**
     * Show the ad if one isn't already showing.
     *
     * @param activity the activity that shows the app open ad
     */
    fun showAdIfAvailable(
        activity: Activity, onShowComplete: () -> Unit, onShowFailed: () -> Unit
    ) {
        showAdIfAvailable(activity, object : OnShowAdCompleteListener {
            override fun onShowAdComplete() {
                // Empty because the user will go back to the activity that shows the ad.
                onShowComplete.invoke()
            }

            override fun onShowFailed() {
                onShowFailed.invoke()
            }
        })
    }

    /**
     * Show the ad if one isn't already showing.
     *
     * @param activity the activity that shows the app open ad
     * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
     */
    fun showAdIfAvailable(activity: Activity, onShowAdCompleteListener: OnShowAdCompleteListener) {
        if (!isShowAds()) {
            onShowAdCompleteListener.onShowFailed()
            return
        }
        if (Constants.isRequestPermission) {
            delayResetFlagPermission()
            onShowAdCompleteListener.onShowFailed()
            return
        }
        if (!isInterReady()) {
            onShowAdCompleteListener.onShowFailed()
            return
        }
        // If the app open ad is already showing, do not show the ad again.
        Log.i(TAG, "isShowing OpenAds: $isShowingAd")
        if (isShowingAd) {
            Log.d(TAG, "The app open ad is already showing.")
            return
        }

        // If the app open ad is not available yet, invoke the callback then load the ad.
        if (!isAdAvailable()) {
            Log.d(TAG, "The app open ad is not ready yet.")
            onShowAdCompleteListener.onShowFailed()
            loadAd(activity)
            return
        }

        Log.d(TAG, "Will show openAds.")
        if (appOpenAd != null) {
            appOpenAd!!.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    // Set the reference to null so isAdAvailable() returns false.
                    appOpenAd = null
                    isShowingAd = false
                    MyApplication.CHECK_INTER_SHOW = false
                    Log.d(TAG, "openAds onAdDismissedFullScreenContent.")
                    Constants.lastTimeShowInterOpenAds = System.currentTimeMillis()
                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                /** Called when fullscreen content failed to show. */
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isShowingAd = false
                    MyApplication.CHECK_INTER_SHOW = false
                    Log.e(TAG, "openAds onAdFailedToShowFullScreenContent: $adError")
                    onShowAdCompleteListener.onShowFailed()
                    loadAd(activity)
                }

                /** Called when fullscreen content is shown. */
                override fun onAdShowedFullScreenContent() {
                    MyApplication.CHECK_INTER_SHOW = true
                    isShowingAd = true
                    Log.i(TAG, "openAds onAdShowedFullScreenContent.")
                }
            }
            appOpenAd!!.show(activity)
        }
    }
}