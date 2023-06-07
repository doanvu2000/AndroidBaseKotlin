package com.example.baseproject.base.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.baseproject.R
import com.example.baseproject.base.utils.Constant

//check setup in manifest
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || intent.action == null) {
            return
        }
        when (intent.action) {
            Constant.AM9 -> {
                createNotification(
                    context,
                    listOf(
                        context?.resources?.getString(R.string.notification_content_1) ?: "",
                        context?.resources?.getString(R.string.notification_content_2) ?: "",
                        context?.resources?.getString(R.string.notification_content_3) ?: ""
                    ),
                    Constant.NOTI_1
                )
            }
            Constant.PM2 -> {
                createNotification(
                    context,
                    listOf(
                        context?.resources?.getString(R.string.notification_content_4) ?: "",
                        context?.resources?.getString(R.string.notification_content_5) ?: "",
                        context?.resources?.getString(R.string.notification_content_6) ?: ""
                    ),
                    Constant.NOTI_2
                )
            }
            Constant.PM7 -> {
                createNotification(
                    context,
                    listOf(
                        context?.resources?.getString(R.string.notification_content_7) ?: "",
                        context?.resources?.getString(R.string.notification_content_8) ?: "",
                        context?.resources?.getString(R.string.notification_content_9) ?: ""
                    ),
                    Constant.NOTI_1
                )
            }
        }
    }

    private fun createNotification(context: Context?, listNotificationContent: List<String>, type: String) {
        val content = getContentRandom(listNotificationContent.toMutableList())
        TodayNotification.showNotify(
            context,
            context?.resources?.getString(R.string.app_name),
            content,
            type
        )
    }

    private fun getContentRandom(list: MutableList<String>): String {
        for (i in 0..5) {
            list.shuffle()
        }
        return list[0]
    }
}