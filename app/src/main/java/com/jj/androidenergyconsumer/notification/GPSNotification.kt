package com.jj.androidenergyconsumer.notification

import android.content.Context

class GPSNotification(context: Context) : CustomNotification(context, GPS_NOTIFICATION_ID,
        GPS_NOTIFICATION_CHANNEL_ID, GPS_NOTIFICATION_CHANNEL_NAME, gpsDefaultNotificationTitle)