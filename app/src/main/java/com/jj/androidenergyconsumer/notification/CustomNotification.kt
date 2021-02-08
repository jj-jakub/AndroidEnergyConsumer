package com.jj.androidenergyconsumer.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.activities.MainActivity
import com.jj.androidenergyconsumer.utils.ifIsEmpty
import com.jj.androidenergyconsumer.utils.ifNotEmpty
import com.jj.androidenergyconsumer.utils.isAndroid8OrHigher

abstract class CustomNotification(private val context: Context) {

    abstract fun get(contentTitle: String = "", contentText: String = "", subText: String = ""): Notification

    abstract fun notify(contentTitle: String = "Service is running in background", contentText: String = "",
                        subText: String = "")

    abstract fun cancel()

    protected fun notifyNotification(notificationId: Int, notification: Notification) =
        NotificationManagerCompat.from(context).notify(notificationId, notification)

    protected fun cancelNotification(notificationId: Int) =
        NotificationManagerCompat.from(context).cancel(notificationId)

    protected fun createNotification(contentTitle: String, contentText: String, subText: String,
                           channelId: String, channelName: String, builder: NotificationCompat.Builder): Notification {
        createNotificationChannel(context, channelId, channelName)
        val intent = createMainActivityIntent(context)
        builder.setOnlyAlertOnce(true)
        builder.priority = NotificationCompat.PRIORITY_MAX
        return createNotification(intent, channelId, contentTitle, contentText, subText, builder)
    }

    @Suppress("SameParameterValue")
    private fun createNotificationChannel(context: Context, notificationChannelId: String,
                                          notificationChannelName: String) {
        if (isAndroid8OrHigher()) {
            val notificationChannel = NotificationChannel(notificationChannelId,
                    notificationChannelName, IMPORTANCE_HIGH)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(notificationChannel)
        }
    }

    private fun createMainActivityIntent(context: Context) = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
    }

    @Suppress("SameParameterValue")
    private fun createNotification(intent: Intent, channelId: String, contentTitle: String,
                                   contentText: String,
                                   subText: String, builder: NotificationCompat.Builder): Notification =
        builder.apply {
            setContentTitle(contentTitle ifIsEmpty "Notification default title")
            contentText ifNotEmpty { setContentText(contentText) }
            subText ifNotEmpty { setSubText(subText) }
            setOngoing(true)
            priority = NotificationCompat.PRIORITY_MAX
            setSmallIcon(R.mipmap.ic_launcher)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setChannelId(channelId)
            setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
        }.build()
}