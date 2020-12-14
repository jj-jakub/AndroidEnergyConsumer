package com.jj.androidenergyconsumer.services

import android.app.Service
import com.jj.androidenergyconsumer.wakelock.WakelockManager

abstract class BaseService: Service() {

    // TODO Make it being created in service onCreate
    abstract val wakelockManager: WakelockManager
    abstract val wakelockTag: String

    protected fun acquireWakeLock() {
        wakelockManager.acquireWakelock(wakelockTag)
    }

    protected fun releaseWakeLock() {
        wakelockManager.releaseWakelock(wakelockTag)
    }
}