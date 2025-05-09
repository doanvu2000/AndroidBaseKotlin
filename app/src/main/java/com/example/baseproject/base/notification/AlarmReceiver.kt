package com.example.baseproject.base.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.baseproject.R
import com.example.baseproject.base.utils.util.Constants

//check setup in manifest
class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent == null || intent.action == null) {
            return
        }
        when (intent.action) {
            Constants.AM9 -> {
                createNotification(
                    context,
                    listOf(
                        context?.resources?.getString(R.string.notification_content_1) ?: "",
                        context?.resources?.getString(R.string.notification_content_2) ?: "",
                        context?.resources?.getString(R.string.notification_content_3) ?: ""
                    ),
                    Constants.NOTI_1
                )
            }

            Constants.PM2 -> {
                createNotification(
                    context,
                    listOf(
                        context?.resources?.getString(R.string.notification_content_4) ?: "",
                        context?.resources?.getString(R.string.notification_content_5) ?: "",
                        context?.resources?.getString(R.string.notification_content_6) ?: ""
                    ),
                    Constants.NOTI_2
                )
            }

            Constants.PM7 -> {
                createNotification(
                    context,
                    listOf(
                        context?.resources?.getString(R.string.notification_content_7) ?: "",
                        context?.resources?.getString(R.string.notification_content_8) ?: "",
                        context?.resources?.getString(R.string.notification_content_9) ?: ""
                    ),
                    Constants.NOTI_1
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
        repeat(5) {
            list.shuffle()
        }
        return list[0]
    }
}