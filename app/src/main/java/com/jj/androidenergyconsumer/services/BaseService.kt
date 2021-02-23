package com.jj.androidenergyconsumer.services

import android.app.Service
import com.jj.androidenergyconsumer.wakelock.WakelockManager
import org.koin.android.ext.android.inject

abstract class BaseService : Service() {

    private val wakelockManager: WakelockManager by inject()
    abstract val wakelockTag: String

    protected fun acquireWakeLock() {
        wakelockManager.acquireWakelock(wakelockTag)
    }

    protected fun releaseWakeLock() {
        wakelockManager.releaseWakelock(wakelockTag)
    }
}