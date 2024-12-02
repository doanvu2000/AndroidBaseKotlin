package com.example.baseproject.base.notification

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import androidx.core.app.NotificationCompat
import com.example.baseproject.R
import com.example.baseproject.base.utils.extension.getFlagPendingIntent
import com.example.baseproject.base.utils.extension.isSdk26
import com.example.baseproject.base.utils.extension.isSdkS
import com.example.baseproject.base.utils.util.Constants

class AlarmReceiverV2 : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "Bean id"
        const val NAME_NOTIFICATION_CHANNEL = "Bean Channel"
        const val NOTIFICATION_ID = 5062023
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(Constants.TAG, "onReceive: ")
        val title = intent.getStringExtra("title")
        val content = intent.getStringExtra("content")
        createNotification(context, title ?: "", content ?: "")
    }

    private fun createNotification(context: Context, title: String, content: String) {
        val contentIntent =
            context.packageManager.getLaunchIntentForPackage(context.packageName)?.setPackage(null)
                ?.setFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
                            or Intent.FLAG_ACTIVITY_SINGLE_TOP
                )
        val flags = PendingIntent.FLAG_UPDATE_CURRENT.getFlagPendingIntent()
        val pendingIntent = PendingIntent.getActivity(context, 0, contentIntent, flags)
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val builder =
            NotificationCompat.Builder(context, CHANNEL_ID).setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle(title)
                .setContentText(content)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)

        if (isSdk26()) {
            builder.setChannelId(CHANNEL_ID)
            val channel = NotificationChannel(
                CHANNEL_ID,
                NAME_NOTIFICATION_CHANNEL,
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.enableVibration(true)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build())
    }
}

fun setReminder(context: Context, title: String, content: String, time: Long) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiverV2::class.java)
    intent.putExtra("title", title)
    intent.putExtra("content", content)
    val flags = PendingIntent.FLAG_UPDATE_CURRENT.getFlagPendingIntent()
    val pendingIntent =
        PendingIntent.getBroadcast(context, AlarmReceiverV2.NOTIFICATION_ID, intent, flags)
    val setExactAlarm = {
        AlarmManagerCompat.setExactAndAllowWhileIdle(
            alarmManager, AlarmManager.RTC_WAKEUP, time, pendingIntent
        )
    }
    if (isSdkS()) {
        if (alarmManager.canScheduleExactAlarms()) {
            setExactAlarm()
        } else {
            //request open setting schedule alarm page
            /**
             * @see ActivityEx.kt: openExactAlarmSettingPage()
             * */
        }
    } else {
        setExactAlarm()
    }
}

fun cancelReminder(context: Context?) {
    val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiverV2::class.java)
    val flags = PendingIntent.FLAG_UPDATE_CURRENT.getFlagPendingIntent()
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        AlarmReceiverV2.NOTIFICATION_ID, intent, flags
    )
    alarmManager.cancel(pendingIntent)
}