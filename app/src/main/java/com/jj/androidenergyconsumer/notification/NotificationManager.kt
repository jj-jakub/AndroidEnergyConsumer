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

const val UNIVERSAL_SERVICE_NOTIFICATION_ID = 101
const val UNIVERSAL_SERVICE_NOTIFICATION_CHANNEL_ID = "UNIVERSAL_SERVICE_NOTIFICATION_CHANNEL_ID"
const val UNIVERSAL_SERVICE_NOTIFICATION_CHANNEL_NAME = "Universal Service notification"

const val BLUETOOTH_SERVICE_NOTIFICATION_ID = 102
private const val BLUETOOTH_SERVICE_NOTIFICATION_CHANNEL_ID = "BLUETOOTH_SERVICE_NOTIFICATION_CHANNEL_ID"
private const val BLUETOOTH_SERVICE_NOTIFICATION_CHANNEL_NAME = "Bluetooth Service notification"

const val CALCULATIONS_SERVICE_NOTIFICATION_ID = 103
private const val CALCULATIONS_SERVICE_NOTIFICATION_CHANNEL_ID = "CALCULATIONS_SERVICE_NOTIFICATION_CHANNEL_ID"
private const val CALCULATIONS_SERVICE_NOTIFICATION_CHANNEL_NAME = "Calculations Service notification"

const val GPS_SERVICE_NOTIFICATION_ID = 104
private const val GPS_SERVICE_NOTIFICATION_CHANNEL_ID = "GPS_SERVICE_NOTIFICATION_CHANNEL_ID"
private const val GPS_SERVICE_NOTIFICATION_CHANNEL_NAME = "GPS Service notification"

const val INTERNET_SERVICE_NOTIFICATION_ID = 105
private const val INTERNET_SERVICE_NOTIFICATION_CHANNEL_ID = "INTERNET_SERVICE_NOTIFICATION_CHANNEL_ID"
private const val INTERNET_SERVICE_NOTIFICATION_CHANNEL_NAME = "Internet Service notification"

class NotificationManager(private val context: Context) {

    private val universalServiceNotificationBuilder =
        NotificationCompat.Builder(context, UNIVERSAL_SERVICE_NOTIFICATION_CHANNEL_ID)
    private val btServiceNotificationBuilder =
        NotificationCompat.Builder(context, BLUETOOTH_SERVICE_NOTIFICATION_CHANNEL_ID)
    private val calculationsServiceNotificationBuilder =
        NotificationCompat.Builder(context, CALCULATIONS_SERVICE_NOTIFICATION_CHANNEL_ID)
    private val gpsServiceNotificationBuilder =
        NotificationCompat.Builder(context, GPS_SERVICE_NOTIFICATION_CHANNEL_ID)
    private val internetServiceNotificationBuilder =
        NotificationCompat.Builder(context, INTERNET_SERVICE_NOTIFICATION_CHANNEL_ID)

    /** Universal Service */
    fun getUniversalServiceNotification(contentTitle: String = "", contentText: String = "", subText: String = "") =
        createUniversalServiceNotification(contentTitle, contentText, subText)

    private fun createUniversalServiceNotification(contentTitle: String, contentText: String,
                                                   subText: String): Notification =
        setupNotificationFromBuilder(contentTitle, contentText, subText, UNIVERSAL_SERVICE_NOTIFICATION_CHANNEL_ID,
                UNIVERSAL_SERVICE_NOTIFICATION_CHANNEL_NAME, universalServiceNotificationBuilder)

    fun notifyUniversalServiceNotification(contentTitle: String = "Service is running in background",
                                           contentText: String = "", subText: String = "") {
        val universalServiceNotification = getUniversalServiceNotification(contentTitle, contentText, subText)
        notifyNotification(UNIVERSAL_SERVICE_NOTIFICATION_ID, universalServiceNotification)
    }

    fun cancelUniversalServiceNotification() = cancelNotification(UNIVERSAL_SERVICE_NOTIFICATION_ID)

    /** Bluetooth Service */
    fun getBtServiceNotification(contentTitle: String = "", contentText: String = "", subText: String = "") =
        createBtServiceNotification(contentTitle, contentText, subText)

    private fun createBtServiceNotification(contentTitle: String, contentText: String, subText: String): Notification =
        setupNotificationFromBuilder(contentTitle, contentText, subText, BLUETOOTH_SERVICE_NOTIFICATION_CHANNEL_ID,
                BLUETOOTH_SERVICE_NOTIFICATION_CHANNEL_NAME, btServiceNotificationBuilder)

    fun notifyBtServiceNotification(contentTitle: String = "Bluetooth Service is running in background",
                                    contentText: String = "", subText: String = "") {
        val btServiceNotification = getBtServiceNotification(contentTitle, contentText, subText)
        notifyNotification(BLUETOOTH_SERVICE_NOTIFICATION_ID, btServiceNotification)
    }

    fun cancelBtServiceNotification() = cancelNotification(BLUETOOTH_SERVICE_NOTIFICATION_ID)

    /** Calculations Service */
    fun getCalculationsServiceNotification(contentTitle: String = "", contentText: String = "", subText: String = "") =
        createCalculationsServiceNotification(contentTitle, contentText, subText)

    private fun createCalculationsServiceNotification(contentTitle: String, contentText: String,
                                                      subText: String): Notification =
        setupNotificationFromBuilder(contentTitle, contentText, subText, CALCULATIONS_SERVICE_NOTIFICATION_CHANNEL_ID,
                CALCULATIONS_SERVICE_NOTIFICATION_CHANNEL_NAME, calculationsServiceNotificationBuilder)

    fun notifyCalculationsServiceNotification(contentTitle: String = "Calculations Service is running in background",
                                              contentText: String = "", subText: String = "") {
        val calculationsServiceNotification = getCalculationsServiceNotification(contentTitle, contentText, subText)
        notifyNotification(CALCULATIONS_SERVICE_NOTIFICATION_ID, calculationsServiceNotification)
    }

    fun cancelCalculationsServiceNotification() = cancelNotification(CALCULATIONS_SERVICE_NOTIFICATION_ID)

    /** GPS Service */
    fun getGPSServiceNotification(contentTitle: String = "", contentText: String = "", subText: String = "") =
        createGPSServiceNotification(contentTitle, contentText, subText)

    private fun createGPSServiceNotification(contentTitle: String, contentText: String, subText: String): Notification =
        setupNotificationFromBuilder(contentTitle, contentText, subText, GPS_SERVICE_NOTIFICATION_CHANNEL_ID,
                GPS_SERVICE_NOTIFICATION_CHANNEL_NAME, gpsServiceNotificationBuilder)

    fun notifyGPSServiceNotification(contentTitle: String = "GPS Service is running in background",
                                     contentText: String = "", subText: String = "") {
        val gpsServiceNotification = getGPSServiceNotification(contentTitle, contentText, subText)
        notifyNotification(GPS_SERVICE_NOTIFICATION_ID, gpsServiceNotification)
    }

    fun cancelGPSServiceNotification() = cancelNotification(GPS_SERVICE_NOTIFICATION_ID)

    /** Internet Service */
    fun getInternetServiceNotification(contentTitle: String = "", contentText: String = "", subText: String = "") =
        createInternetServiceNotification(contentTitle, contentText, subText)

    private fun createInternetServiceNotification(contentTitle: String, contentText: String,
                                                  subText: String): Notification =
        setupNotificationFromBuilder(contentTitle, contentText, subText, INTERNET_SERVICE_NOTIFICATION_CHANNEL_ID,
                INTERNET_SERVICE_NOTIFICATION_CHANNEL_NAME, internetServiceNotificationBuilder)

    fun notifyInternetServiceNotification(contentTitle: String = "Internet Service is running in background",
                                          contentText: String = "", subText: String = "") {
        val internetServiceNotification = getInternetServiceNotification(contentTitle, contentText, subText)
        notifyNotification(INTERNET_SERVICE_NOTIFICATION_ID, internetServiceNotification)
    }

    fun cancelInternetServiceNotification() = cancelNotification(INTERNET_SERVICE_NOTIFICATION_ID)

    /** Common methods */
    private fun notifyNotification(notificationId: Int, notification: Notification) =
        NotificationManagerCompat.from(context).notify(notificationId, notification)

    private fun cancelNotification(notificationId: Int) = NotificationManagerCompat.from(context).cancel(notificationId)

    private fun setupNotificationFromBuilder(contentTitle: String, contentText: String, subText: String,
                                             channelId: String, channelName: String,
                                             builder: NotificationCompat.Builder): Notification {
        createNotificationChannel(channelId, channelName)
        val intent = createMainActivityIntent(context)
        builder.setOnlyAlertOnce(true)
        builder.priority = NotificationCompat.PRIORITY_MAX
        return createNotification(intent, channelId, contentTitle, contentText, subText, builder)
    }

    @Suppress("SameParameterValue")
    private fun createNotification(intent: Intent, channelId: String, contentTitle: String, contentText: String,
                                   subText: String, builder: NotificationCompat.Builder) =
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
    private fun createNotificationChannel(notificationChannelId: String, notificationChannelName: String) {
        if (isAndroid8OrHigher()) {
            val notificationChannel = NotificationChannel(notificationChannelId,
                    notificationChannelName, NotificationManager.IMPORTANCE_HIGH)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(notificationChannel)
        }
    }

    private fun createMainActivityIntent(context: Context) = Intent(context, MainActivity::class.java).apply {
        action = Intent.ACTION_MAIN
        addCategory(Intent.CATEGORY_LAUNCHER)
    }
}