package com.jj.androidenergyconsumer.notification

import android.app.Notification
import android.content.Context
import androidx.core.app.NotificationCompat
import com.jj.androidenergyconsumer.utils.ifIsEmpty

class BluetoothNotification(context: Context) : CustomNotification(context) {

    private val notificationBuilder: NotificationCompat.Builder =
        NotificationCompat.Builder(context, BLUETOOTH_NOTIFICATION_CHANNEL_ID)

    private var notification: Notification? = null

    override fun get(contentTitle: String, contentText: String, subText: String): Notification {
        return notification ?: createNotification(contentTitle, contentText, subText, BLUETOOTH_NOTIFICATION_CHANNEL_ID,
                BLUETOOTH_NOTIFICATION_CHANNEL_NAME, notificationBuilder).apply { notification = this }
    }

    override fun notify(contentTitle: String, contentText: String, subText: String) {
        val title = contentTitle ifIsEmpty "Bluetooth Service is running in background"
        val btServiceNotification = get(title, contentText, subText)
        notifyNotification(BLUETOOTH_NOTIFICATION_ID, btServiceNotification)
    }

    override fun cancel() = cancelNotification(BLUETOOTH_NOTIFICATION_ID)
}