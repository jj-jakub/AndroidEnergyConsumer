package com.jj.androidenergyconsumer.utils

import android.content.BroadcastReceiver

abstract class CustomBroadcastReceiver : BroadcastReceiver() {

    abstract fun register()
    abstract fun unregister()
}