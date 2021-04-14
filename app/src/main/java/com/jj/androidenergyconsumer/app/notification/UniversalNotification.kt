package com.jj.androidenergyconsumer.app.notification

import android.content.Context
import com.jj.androidenergyconsumer.app.utils.SystemVersionChecker

class UniversalNotification(context: Context, systemVersionChecker: SystemVersionChecker) :
    CustomNotification(context, UNIVERSAL_NOTIFICATION_ID, UNIVERSAL_NOTIFICATION_CHANNEL_ID,
            UNIVERSAL_NOTIFICATION_CHANNEL_NAME, universalDefaultNotificationTitle, systemVersionChecker)