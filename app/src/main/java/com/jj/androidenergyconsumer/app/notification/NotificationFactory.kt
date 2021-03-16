package com.jj.androidenergyconsumer.app.notification

import android.content.Context

enum class NotificationType {
    BLUETOOTH,
    CALCULATIONS,
    GPS,
    INTERNET,
    UNIVERSAL
}

class NotificationContainer(context: Context) {

    private val bluetoothNotification = BluetoothNotification(context)
    private val calculationsNotification = CalculationsNotification(context)
    private val gpsNotification = GPSNotification(context)
    private val internetNotification = InternetNotification(context)
    private val universalNotification = UniversalNotification(context)

    fun getProperNotification(notificationType: NotificationType): CustomNotification =
        when (notificationType) {
            NotificationType.BLUETOOTH -> bluetoothNotification
            NotificationType.CALCULATIONS -> calculationsNotification
            NotificationType.GPS -> gpsNotification
            NotificationType.INTERNET -> internetNotification
            NotificationType.UNIVERSAL -> universalNotification
        }
}