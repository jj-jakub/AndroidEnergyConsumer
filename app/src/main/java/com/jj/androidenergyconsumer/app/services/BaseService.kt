package com.jj.androidenergyconsumer.app.services

import android.app.Service
import com.jj.androidenergyconsumer.app.notification.NotificationContainer
import com.jj.androidenergyconsumer.app.wakelock.WakelockManager
import org.koin.android.ext.android.inject

abstract class BaseService : Service() {

    protected val notificationContainer: NotificationContainer by inject()

    private val wakelockManager: WakelockManager by inject()
    abstract val wakelockTag: String

    protected fun acquireWakeLock() {
        wakelockManager.acquireWakelock(wakelockTag)
    }

    protected fun releaseWakeLock() {
        wakelockManager.releaseWakelock(wakelockTag)
    }
}