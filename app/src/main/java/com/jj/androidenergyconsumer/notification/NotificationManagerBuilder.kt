package com.jj.androidenergyconsumer.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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

const val NOTIFICATION_SERVICE_ID = 2
const val NOTIFICATION_SERVICE_CHANNEL_ID = "NOTIFICATION_SERVICE_CHANNEL"
const val NOTIFICATION_SERVICE_CHANNEL_NAME = "Service notification"

class NotificationManagerBuilder(private val context: Context) {

    private val serviceNotificationBuilder = NotificationCompat.Builder(context, NOTIFICATION_SERVICE_CHANNEL_ID)


    fun getServiceNotification(contentTitle: String = "",
                               contentText: String = "", subText: String = "") =
        createCollectingServiceNotification(contentTitle, contentText, subText)

    fun notifyServiceNotification(contentTitle: String = "Service is running in background",
                                  contentText: String = "", subText: String = "") {
        val serviceNotification = getServiceNotification(contentTitle, contentText, subText)
        NotificationManagerCompat.from(context).notify(NOTIFICATION_SERVICE_ID, serviceNotification)
    }

    fun cancelServiceNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_SERVICE_ID)
    }

    private fun createCollectingServiceNotification(contentTitle: String,
                                                    contentText: String, subText: String): Notification {
        val channelId = NOTIFICATION_SERVICE_CHANNEL_ID
        createNotificationChannel(NOTIFICATION_SERVICE_CHANNEL_ID, NOTIFICATION_SERVICE_CHANNEL_NAME)
        val intent = createMainActivityIntent(context)
        serviceNotificationBuilder.setOnlyAlertOnce(true)
        serviceNotificationBuilder.priority = NotificationCompat.PRIORITY_MAX
        return createNotification(intent, channelId, contentTitle, contentText, subText, serviceNotificationBuilder)
    }

    @Suppress("SameParameterValue")
    private fun createNotification(intent: Intent, channelId: String,
                                   contentTitle: String, contentText: String, subText: String,
                                   builder: NotificationCompat.Builder) =
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

    @Suppress("SameParameterValue")
    private fun createNotificationChannel(notificationChannelId: String,
                                          notificationChannelName: String) {
        if (isAndroid8OrHigher()) {
            val notificationChannel =
                NotificationChannel(notificationChannelId, notificationChannelName, NotificationManager.IMPORTANCE_MAX)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(notificationChannel)
        }
    }

    private fun createMainActivityIntent(context: Context) = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
}