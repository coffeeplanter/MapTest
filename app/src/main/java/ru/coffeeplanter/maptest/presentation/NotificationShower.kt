package ru.coffeeplanter.maptest.presentation

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import android.widget.Toast
import ru.coffeeplanter.maptest.R
import ru.coffeeplanter.maptest.platform.App

object NotificationShower {

    private const val POINTS_DOWNLOAD_COMPLETED_NOTIFICATION_ID = 364
    private const val POINTS_DOWNLOAD_COMPLETED_PENDING_INTENT_ID = 457
    private const val POINTS_DOWNLOAD_COMPLETED_NOTIFICATION_CHANNEL_ID = "download_completed_notification_channel"

    @SuppressLint("NewApi")
    fun showNotification() {

        val context = App.app

        val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                    POINTS_DOWNLOAD_COMPLETED_NOTIFICATION_CHANNEL_ID,
                    context.getString(R.string.main_notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(mChannel)
        }

        val notificationBuilder = NotificationCompat.Builder(context, POINTS_DOWNLOAD_COMPLETED_NOTIFICATION_CHANNEL_ID)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle(context.getString(R.string.points_download_completed_notification_title))
                .setContentText(context.getString(R.string.points_download_completed_notification_body))
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setContentIntent(contentIntent(context))
                .setAutoCancel(true)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            notificationBuilder.priority = NotificationCompat.PRIORITY_DEFAULT
        }

        notificationManager.notify(POINTS_DOWNLOAD_COMPLETED_NOTIFICATION_ID, notificationBuilder.build())

        Toast.makeText(context, context.getString(R.string.points_download_completed_notification_title), Toast.LENGTH_LONG).show()

    }

    private fun contentIntent(context: Context): PendingIntent {
        val startActivityIntent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
                context,
                POINTS_DOWNLOAD_COMPLETED_PENDING_INTENT_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

}
