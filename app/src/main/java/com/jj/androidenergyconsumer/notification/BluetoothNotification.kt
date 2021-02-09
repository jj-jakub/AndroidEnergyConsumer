package com.jj.androidenergyconsumer.notification

import android.content.Context

class BluetoothNotification(context: Context) : CustomNotification(context, BLUETOOTH_NOTIFICATION_ID,
        BLUETOOTH_NOTIFICATION_CHANNEL_ID, BLUETOOTH_NOTIFICATION_CHANNEL_NAME, btDefaultNotificationTitle)