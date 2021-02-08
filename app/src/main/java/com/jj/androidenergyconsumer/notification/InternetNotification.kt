package com.jj.androidenergyconsumer.notification

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import com.jj.androidenergyconsumer.utils.ifIsEmpty

class InternetNotification(context: Context) : CustomNotification(context) {

    private val notificationBuilder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, INTERNET_NOTIFICATION_CHANNEL_ID)

    private var notification: Notification? = null

    override fun get(contentTitle: String, contentText: String, subText: String): Notification {
        return notification ?: createNotification(contentTitle, contentText, subText,
                INTERNET_NOTIFICATION_CHANNEL_ID,
                INTERNET_NOTIFICATION_CHANNEL_NAME, notificationBuilder).apply { notification = this }
    }

    override fun notify(contentTitle: String, contentText: String, subText: String) {
        val title = contentTitle ifIsEmpty "Internet Service is running in background"
        val calcServiceNotification = get(title, contentText, subText)
        notifyNotification(INTERNET_NOTIFICATION_ID, calcServiceNotification)
    }

    override fun cancel() = cancelNotification(INTERNET_NOTIFICATION_ID)
}