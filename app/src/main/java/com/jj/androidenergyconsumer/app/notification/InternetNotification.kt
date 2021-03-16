package com.jj.androidenergyconsumer.app.notification

import android.content.Context


class InternetNotification(context: Context) : CustomNotification(context, INTERNET_NOTIFICATION_ID,
        INTERNET_NOTIFICATION_CHANNEL_ID, INTERNET_NOTIFICATION_CHANNEL_NAME, internetDefaultNotificationTitle)