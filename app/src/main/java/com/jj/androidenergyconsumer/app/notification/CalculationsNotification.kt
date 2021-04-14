package com.jj.androidenergyconsumer.app.notification

import android.content.Context
import com.jj.androidenergyconsumer.app.utils.SystemVersionChecker

class CalculationsNotification(context: Context, systemVersionChecker: SystemVersionChecker) :
    CustomNotification(context, CALCULATIONS_NOTIFICATION_ID, CALCULATIONS_NOTIFICATION_CHANNEL_ID,
            CALCULATIONS_NOTIFICATION_CHANNEL_NAME, calculationsDefaultNotificationTitle, systemVersionChecker)