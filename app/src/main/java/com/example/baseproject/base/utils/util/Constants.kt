package com.example.baseproject.base.utils.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object Constants {
    const val READ_TIME_OUT: Long = 15
    const val CONNECT_TIME_OUT: Long = 15
    const val TAG = "doanvv"

    //-------------------alarm interval day-----------------------
    const val MILLISECOND_ONE_DAY = 86400000
    const val AM9 = "9AM"
    const val PM2 = "2PM"
    const val PM7 = "7PM"
    private const val H9AM = 9
    private const val H2PM = 14
    private const val H7PM = 19
    val mapAlarmValue = mutableMapOf(
        AM9 to H9AM, PM2 to H2PM, PM7 to H7PM
    )
    val actions = listOf(AM9, PM2, PM7)

    //------------------------------------------------------------
    const val NOTI_DATA = "data"
    const val CHANNEL_NOTIFY_ID = "app_name_notification"
    const val NOTI_1 = "NOTI_1"
    const val NOTI_2 = "NOTI_2"

    var isRequestPermission = false

    var lastTimeShowInterOpenAds = 0L
}

fun delayResetFlagPermission() {
    CoroutineScope(Dispatchers.IO).launch {
        delay(500)
        Constants.isRequestPermission = false
    }
}