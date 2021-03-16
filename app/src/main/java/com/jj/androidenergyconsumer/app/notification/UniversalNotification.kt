package com.jj.androidenergyconsumer.app.notification

import android.content.Context

class UniversalNotification(context: Context) : CustomNotification(context, UNIVERSAL_NOTIFICATION_ID,
        UNIVERSAL_NOTIFICATION_CHANNEL_ID, UNIVERSAL_NOTIFICATION_CHANNEL_NAME, universalDefaultNotificationTitle)