package com.jj.androidenergyconsumer

import android.app.Application
import com.jj.androidenergyconsumer.koin.aecMainModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

// TODO change StateFlow to SharedFlow
class AECApplication : Application() {

    companion object {
        lateinit var instance: AECApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        launchKoin()
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