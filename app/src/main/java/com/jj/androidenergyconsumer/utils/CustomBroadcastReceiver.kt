package com.jj.androidenergyconsumer.utils

import android.content.BroadcastReceiver
import com.jj.androidenergyconsumer.bluetooth.ScanningCallback

abstract class CustomBroadcastReceiver : BroadcastReceiver() {

    abstract fun register(callback: ScanningCallback)
    abstract fun unregister()
}