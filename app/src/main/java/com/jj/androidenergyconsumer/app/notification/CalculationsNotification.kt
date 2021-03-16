package com.jj.androidenergyconsumer.app.notification

import android.content.Context

class CalculationsNotification(context: Context) : CustomNotification(context, CALCULATIONS_NOTIFICATION_ID,
        CALCULATIONS_NOTIFICATION_CHANNEL_ID, CALCULATIONS_NOTIFICATION_CHANNEL_NAME,
        calculationsDefaultNotificationTitle)