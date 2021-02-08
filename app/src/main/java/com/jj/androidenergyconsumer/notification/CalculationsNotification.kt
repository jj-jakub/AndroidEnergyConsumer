package com.jj.androidenergyconsumer.notification

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import com.jj.androidenergyconsumer.utils.ifIsEmpty

class CalculationsNotification(context: Context) : CustomNotification(context) {

    private val notificationBuilder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, CALCULATIONS_NOTIFICATION_CHANNEL_ID)

    private var notification: Notification? = null

    //TODO Do we have to create it every get?
    override fun get(contentTitle: String, contentText: String, subText: String): Notification {
        return createNotification(contentTitle, contentText, subText,
                CALCULATIONS_NOTIFICATION_CHANNEL_ID,
                CALCULATIONS_NOTIFICATION_CHANNEL_NAME, notificationBuilder).apply { notification = this }
    }

    override fun notify(contentTitle: String, contentText: String, subText: String) {
        val title = contentTitle ifIsEmpty "Calculations Service is running in background"
        val calcServiceNotification = get(title, contentText, subText)
        notifyNotification(CALCULATIONS_NOTIFICATION_ID, calcServiceNotification)
    }

    override fun cancel() = cancelNotification(CALCULATIONS_NOTIFICATION_ID)
}