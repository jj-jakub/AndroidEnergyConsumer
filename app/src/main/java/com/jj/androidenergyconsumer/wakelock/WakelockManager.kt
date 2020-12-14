package com.jj.androidenergyconsumer.wakelock

import android.annotation.SuppressLint
import android.content.Context
import android.os.PowerManager

class WakelockManager(context: Context) {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
    private val activeWakelocks: HashMap<String, PowerManager.WakeLock> = hashMapOf()

    /**
     * @param tag unique tag for wakeLock
     * @return true if wakeLock has been successfully acquired or has already been acquired
     *         false if failed to create new wakeLock
     */
    @SuppressLint("WakelockTimeout")
    @Synchronized
    fun acquireWakelock(tag: String): Boolean {
        if (activeWakelocks.containsKey(tag)) return true
        val newWakelock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, tag) ?: return false
        newWakelock.acquire()
        activeWakelocks[tag] = newWakelock
        return true
    }

    /**
     * @return true if wakeLock has been successfully acquired or has already been acquired
     *         false if there was no acquired wakeLock with given tag
     */
    @Synchronized
    fun releaseWakelock(tag: String): Boolean {
        return if (activeWakelocks.containsKey(tag)) {
            activeWakelocks[tag]?.release()
            activeWakelocks.remove(tag)
            true
        } else false
    }
}