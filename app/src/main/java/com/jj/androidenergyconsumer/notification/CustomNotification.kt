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
import com.jj.androidenergyconsumer.utils.isAndroid8OrHigher

abstract class CustomNotification(private val context: Context, private val notificationId: Int,
                                  private val notificationChannelId: String,
                                  private val notificationChannelName: String,
                                  private val defaultNotificationTitle: String) {

    private var notificationBuilder: NotificationCompat.Builder? = null

    private var notification: Notification? = null

    fun get(): Notification = notification ?: prepareNotification()

    fun notify(contentTitle: String = "", contentText: String = "", subText: String = "") {
        val title = contentTitle ifIsEmpty defaultNotificationTitle
        prepareNotification(title, contentText, subText)
    }

    fun cancel() = cancelNotification(notificationId)

    private fun prepareNotification(title: String = defaultNotificationTitle, contentText: String = "",
                                    subText: String = "") =
        createNotification(title, contentText, subText, notificationBuilder ?: createBuilder()).apply {
            notification = this
            notifyNotification(notificationId, this)
        }

    private fun notifyNotification(notificationId: Int, notification: Notification) =
        NotificationManagerCompat.from(context).notify(notificationId, notification)

    private fun cancelNotification(notificationId: Int) =
        NotificationManagerCompat.from(context).cancel(notificationId)


    private fun createBuilder(): NotificationCompat.Builder = prepareDefaultBuilderAndChannel(notificationChannelId,
            notificationChannelName).apply { notificationBuilder = this }

    @Suppress("SameParameterValue")
    private fun createNotification(contentTitle: String = "", contentText: String = "", subText: String = "",
                                   builder: NotificationCompat.Builder): Notification =
        builder.apply {
            setContentTitle(contentTitle)
            setContentText(contentText)
            setSubText(subText)
        }.build()

    private fun prepareDefaultBuilderAndChannel(channelId: String, channelName: String): NotificationCompat.Builder {
        createNotificationChannel(context, channelId, channelName)
        return NotificationCompat.Builder(context, channelId).apply {
            val intent = createMainActivityIntent(context)
            setOnlyAlertOnce(true)
            setOngoing(true)
            priority = NotificationCompat.PRIORITY_MAX
            setSmallIcon(R.mipmap.ic_launcher)
            setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            setChannelId(channelId)
            setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
        }
    }

    @Suppress("SameParameterValue")
    private fun createNotificationChannel(context: Context, channelId: String, channelName: String) {
        if (isAndroid8OrHigher()) {
            val channel = NotificationChannel(channelId, channelName, IMPORTANCE_HIGH)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun createMainActivityIntent(context: Context) = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
}