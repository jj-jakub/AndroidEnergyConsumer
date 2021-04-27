package com.jj.androidenergyconsumer.app.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.util.Log
import com.jj.androidenergyconsumer.domain.tag

fun logAndPingServer(message: String, tag: String) {
    Log.d(tag, message)
}

fun BroadcastReceiver.safelyUnregisterReceiver(context: Context) {
    try {
        context.unregisterReceiver(this)
    } catch (iae: IllegalArgumentException) {
        Log.e(tag, "Error when unregistering receiver", iae)
    }
}