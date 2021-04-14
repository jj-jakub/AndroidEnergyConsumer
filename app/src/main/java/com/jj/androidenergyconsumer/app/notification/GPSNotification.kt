package com.jj.androidenergyconsumer.app.notification

import android.content.Context
import com.jj.androidenergyconsumer.app.utils.SystemVersionChecker

class GPSNotification(context: Context, systemVersionChecker: SystemVersionChecker) :
    CustomNotification(context, GPS_NOTIFICATION_ID, GPS_NOTIFICATION_CHANNEL_ID, GPS_NOTIFICATION_CHANNEL_NAME,
            gpsDefaultNotificationTitle, systemVersionChecker)