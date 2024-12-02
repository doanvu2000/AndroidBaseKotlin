package com.example.baseproject.base.ui.main

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.baseproject.base.ui.ads.AppOpenAdManager
import com.example.baseproject.base.utils.util.Constants
import com.example.baseproject.base.utils.util.RemoteConfigUtil
import com.example.baseproject.base.utils.util.SharePrefUtils
import com.google.firebase.crashlytics.ktx.crashlytics
import com.google.firebase.ktx.Firebase

class MyApplication : Application(), Application.ActivityLifecycleCallbacks, LifecycleObserver {
    companion object {
        const val TAG = Constants.TAG + "-Application"

        @JvmField
        var CHECK_INTER_SHOW = false
    }

    private lateinit var appOpenAdManager: AppOpenAdManager
    private var currentActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        SharePrefUtils.init(this)
        RemoteConfigUtil.init(this)
//        Firebase.crashlytics.isCrashlyticsCollectionEnabled = !isDebugMode()
        Firebase.crashlytics.isCrashlyticsCollectionEnabled = true
        appOpenAdManager = AppOpenAdManager()
    }

    fun initAds() {
        appOpenAdManager.loadAd(this)
//        loadNativeAds(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {

    }

    override fun onActivityStarted(activity: Activity) {
        if (this::appOpenAdManager.isInitialized) {
            if (!appOpenAdManager.isShowingAd) {
                currentActivity = activity
            }
        }
    }

    override fun onActivityResumed(activity: Activity) {

    }

    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStopped(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityDestroyed(activity: Activity) {

    }
}