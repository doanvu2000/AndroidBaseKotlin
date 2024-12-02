package com.example.baseproject.base.ui.ads

import android.util.Log
import com.example.baseproject.base.utils.util.Constants
import com.example.baseproject.base.utils.util.RemoteConfigUtil

private const val TAG = Constants.TAG

fun isShowAds() = RemoteConfigUtil.isShowAds

fun isInterReady(): Boolean {
    if (Constants.lastTimeShowInterOpenAds == 0L) {
        return true
    }
    return isPassedCappingTime()
}

fun isPassedCappingTime(): Boolean {
    val diffTime = System.currentTimeMillis() - Constants.lastTimeShowInterOpenAds
    val cappingTime = RemoteConfigUtil.interAdCappingTime * 1000L
    if (diffTime >= cappingTime) {
        Log.d(TAG, "isPassedCappingTime: passed")
    } else {
        Log.w(TAG, "isPassedCappingTime: wait ${cappingTime - diffTime} ms")
    }
    return diffTime >= cappingTime
}
