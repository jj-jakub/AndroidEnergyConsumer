package com.jj.androidenergyconsumer

import android.app.Application
import com.jj.androidenergyconsumer.notification.NotificationContainer

class AECApplication : Application() {

    companion object {
        lateinit var instance: AECApplication
            private set

        lateinit var notificationContainer: NotificationContainer
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        notificationContainer = NotificationContainer(applicationContext)
        FlipperLauncher.enableFlipper(this)
    }
}