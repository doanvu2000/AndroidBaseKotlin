package com.example.baseproject.base.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.baseproject.R
import com.example.baseproject.base.utils.Constant
import com.example.baseproject.user.MainActivity

object TodayNotification {
    private const val TODAY_NOTIFY_ID = 20122023
    fun showNotify(context: Context?, today: String?, message: String?, from: String?) {
        val notificationManager = NotificationManagerCompat.from(context!!)
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(Constant.NOTI_DATA, from)
        val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val resultPendingIntent = PendingIntent.getActivity(context, 0, intent, flag)
        val builder = NotificationCompat.Builder(context, Constant.CHANNEL_NOTIFY_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(resultPendingIntent)
            .setContentTitle(today)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // notificationId is a unique int for each notification that you must define
        Log.e("dddd", "showNotify: ")
        notificationManager.notify(TODAY_NOTIFY_ID, builder.build())
    }
}