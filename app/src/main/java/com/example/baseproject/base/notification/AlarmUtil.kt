package com.example.baseproject.base.notification

import android.app.AlarmManager
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.baseproject.R
import com.example.baseproject.base.utils.extension.getFlagPendingIntent
import com.example.baseproject.base.utils.extension.isSdk26
import com.example.baseproject.base.utils.util.Constants
import java.util.Calendar

object AlarmUtil {
    /**
     * in application, call AlarmUtil.setIntervalAlarm(this) on onCreate() to set schedule alarm
     * */
    fun setIntervalAlarm(context: Context) {
        createNotificationChannel(context)
        Constants.actions.forEach { action ->
            setAlarm(context, action)
        }
    }

    private fun createNotificationChannel(context: Context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (isSdk26()) {
            val name: CharSequence = context.getString(R.string.channel_name)
            val description = context.getString(R.string.channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(Constants.CHANNEL_NOTIFY_ID, name, importance)
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun setAlarm(context: Context, action: String) {
        cancelAlarms(context, action)
        val alarmIntentToday = Intent(context, AlarmReceiver::class.java)
        alarmIntentToday.action = action
        val flag: Int = PendingIntent.FLAG_UPDATE_CURRENT.getFlagPendingIntent()
        val pendingIntentToday = PendingIntent.getBroadcast(context, 0, alarmIntentToday, flag)
        val managerToday = context.getSystemService(Application.ALARM_SERVICE) as AlarmManager
        val todayCal = Calendar.getInstance()
        todayCal.apply {
            set(Calendar.HOUR_OF_DAY, Constants.mapAlarmValue[action] ?: 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (timeInMillis < System.currentTimeMillis()) {
                timeInMillis += Constants.MILLISECOND_ONE_DAY
            }
        }
        managerToday.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            todayCal.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntentToday
        )
    }

    private fun cancelAlarms(context: Context, action: String) {
        val alarmIntentToday = Intent(context.applicationContext, AlarmReceiver::class.java)
        alarmIntentToday.action = action
        val flag: Int = PendingIntent.FLAG_NO_CREATE.getFlagPendingIntent()
        val pendingIntentToday = PendingIntent.getBroadcast(
            context.applicationContext,
            0,
            alarmIntentToday,
            flag
        )
        val alarmManager = context.getSystemService(Application.ALARM_SERVICE) as AlarmManager
        if (pendingIntentToday != null) {
            alarmManager.cancel(pendingIntentToday)
        }
    }

}