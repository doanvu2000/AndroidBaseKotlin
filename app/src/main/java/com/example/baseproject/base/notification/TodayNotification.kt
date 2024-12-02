package com.example.baseproject.base.notification

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.baseproject.R
import com.example.baseproject.base.ui.main.MainActivity
import com.example.baseproject.base.utils.extension.PERMISSION_GRANTED
import com.example.baseproject.base.utils.extension.POST_NOTIFICATION
import com.example.baseproject.base.utils.extension.getFlagPendingIntent
import com.example.baseproject.base.utils.util.Constants

object TodayNotification {
    private const val TODAY_NOTIFY_ID = 20122023
    fun showNotify(context: Context?, today: String?, message: String?, from: String?) {
        val notificationManager = NotificationManagerCompat.from(context!!)
        val intent = Intent(context, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra(Constants.NOTI_DATA, from)
        val flag = PendingIntent.FLAG_UPDATE_CURRENT.getFlagPendingIntent()
        val resultPendingIntent = PendingIntent.getActivity(context, 0, intent, flag)
        val builder = NotificationCompat.Builder(context, Constants.CHANNEL_NOTIFY_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(resultPendingIntent)
            .setContentTitle(today)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        // notificationId is a unique int for each notification that you must define
        Log.e("dddd", "showNotify: ")

        if (ActivityCompat.checkSelfPermission(context, POST_NOTIFICATION) != PERMISSION_GRANTED) {
            return
        }
        notificationManager.notify(TODAY_NOTIFY_ID, builder.build())
    }
}