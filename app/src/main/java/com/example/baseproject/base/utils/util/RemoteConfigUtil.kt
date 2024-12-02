package com.example.baseproject.base.utils.util

import android.content.Context
import android.util.Log
import com.example.baseproject.R
import com.google.firebase.FirebaseApp
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

object RemoteConfigUtil {
    private const val INTER_AD_CAPPING_TIME = "inter_ad_capping_time"
    private const val OPEN_AD_CAPPING_TIME = "open_ad_capping_time"
    private const val TIME_LOADING_SPLASH = "time_loading_splash"

    private const val IS_SHOW_ADS = "is_show_ads"

    fun isInitialize(context: Context): Boolean {
        val isEmpty = FirebaseApp.getApps(context).isEmpty()
        Log.d(Constants.TAG, "FirebaseApp-isInitialize: ${!isEmpty}")
        return !isEmpty
    }

    @JvmStatic
    fun init(context: Context) {
        FirebaseApp.initializeApp(context)
        val remoteConfig: FirebaseRemoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 45
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()
    }

    private fun getBooleanValue(key: String): Boolean {
        return FirebaseRemoteConfig.getInstance().getBoolean(key)
    }

    private fun getIntValue(key: String): Int {
        return FirebaseRemoteConfig.getInstance().getLong(key).toInt()
    }

    private fun getStringValue(key: String): String {
        return FirebaseRemoteConfig.getInstance().getString(key)
    }

    @JvmStatic
    val interAdCappingTime: Int
        get() = getIntValue(INTER_AD_CAPPING_TIME)

    //get() = 12
    @JvmStatic
    val openAdCappingTime: Int
        get() = getIntValue(OPEN_AD_CAPPING_TIME)
//        get() = 5

    @JvmStatic
    val isShowAds: Boolean
        get() = getBooleanValue(IS_SHOW_ADS)
//        get() = false

    val timeLoadingSplash: Int
        get() = getIntValue(TIME_LOADING_SPLASH)
    //        get() = 6

}