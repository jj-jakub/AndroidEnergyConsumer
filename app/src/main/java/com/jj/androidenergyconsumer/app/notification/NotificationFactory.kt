package com.jj.androidenergyconsumer.app.notification

import android.content.Context
import com.jj.androidenergyconsumer.app.utils.SystemVersionChecker

enum class NotificationType {
    BLUETOOTH,
    CALCULATIONS,
    GPS,
    INTERNET,
    UNIVERSAL
}

class NotificationContainer(context: Context, systemVersionChecker: SystemVersionChecker) {

    private val bluetoothNotification = BluetoothNotification(context, systemVersionChecker)
    private val calculationsNotification = CalculationsNotification(context, systemVersionChecker)
    private val gpsNotification = GPSNotification(context, systemVersionChecker)
    private val internetNotification = InternetNotification(context, systemVersionChecker)
    private val universalNotification = UniversalNotification(context, systemVersionChecker)

    fun getProperNotification(notificationType: NotificationType): CustomNotification =
        when (notificationType) {
            NotificationType.BLUETOOTH -> bluetoothNotification
            NotificationType.CALCULATIONS -> calculationsNotification
            NotificationType.GPS -> gpsNotification
            NotificationType.INTERNET -> internetNotification
            NotificationType.UNIVERSAL -> universalNotification
        }
}