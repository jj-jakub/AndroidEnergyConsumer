package com.jj.androidenergyconsumer.app.notification

import android.content.Context
import com.jj.androidenergyconsumer.app.utils.SystemVersionChecker

class BluetoothNotification(context: Context, systemVersionChecker: SystemVersionChecker) :
    CustomNotification(context, BLUETOOTH_NOTIFICATION_ID, BLUETOOTH_NOTIFICATION_CHANNEL_ID,
            BLUETOOTH_NOTIFICATION_CHANNEL_NAME, btDefaultNotificationTitle, systemVersionChecker)