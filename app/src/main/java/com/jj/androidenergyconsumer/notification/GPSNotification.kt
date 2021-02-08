package com.jj.androidenergyconsumer.notification

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import com.jj.androidenergyconsumer.utils.ifIsEmpty

class GPSNotification(context: Context) : CustomNotification(context) {

    private val notificationBuilder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, GPS_NOTIFICATION_CHANNEL_ID)

    private var notification: Notification? = null

    override fun get(contentTitle: String, contentText: String, subText: String): Notification {
        return notification ?: createNotification(contentTitle, contentText, subText, GPS_NOTIFICATION_CHANNEL_ID,
                GPS_NOTIFICATION_CHANNEL_NAME, notificationBuilder).apply { notification = this }
    }

    override fun notify(contentTitle: String, contentText: String, subText: String) {
        val title = contentTitle ifIsEmpty "GPS Service is running in background"
        val btServiceNotification = get(title, contentText, subText)
        notifyNotification(GPS_NOTIFICATION_ID, btServiceNotification)
    }

    override fun cancel() = cancelNotification(GPS_NOTIFICATION_ID)
}