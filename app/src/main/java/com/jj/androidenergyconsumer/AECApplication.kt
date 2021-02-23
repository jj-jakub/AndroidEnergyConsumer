package com.jj.androidenergyconsumer

import android.app.Application
import com.jj.androidenergyconsumer.koin.aecMainModule
import com.jj.androidenergyconsumer.notification.NotificationContainer
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

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
        launchKoin()
        notificationContainer = NotificationContainer(applicationContext)
        FlipperLauncher.enableFlipper(this)
    }

    private fun launchKoin() {
        startKoin {
            androidLogger()
            androidContext(this@AECApplication)
            modules(aecMainModule)
        }
    }
}