package com.jj.androidenergyconsumer.app.notification

import android.content.Context
import com.jj.androidenergyconsumer.app.utils.SystemVersionChecker


class InternetNotification(context: Context, systemVersionChecker: SystemVersionChecker) :
    CustomNotification(context, INTERNET_NOTIFICATION_ID, INTERNET_NOTIFICATION_CHANNEL_ID,
            INTERNET_NOTIFICATION_CHANNEL_NAME, internetDefaultNotificationTitle, systemVersionChecker)