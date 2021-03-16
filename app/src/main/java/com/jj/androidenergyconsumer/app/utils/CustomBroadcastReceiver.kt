package com.jj.androidenergyconsumer.app.utils

import android.content.BroadcastReceiver

abstract class CustomBroadcastReceiver : BroadcastReceiver() {

    abstract fun register()
    abstract fun unregister()
}