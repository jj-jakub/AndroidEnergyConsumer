package com.jj.androidenergyconsumer.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.jj.androidenergyconsumer.rest.DefaultCallback
import com.jj.androidenergyconsumer.rest.PingDataCall
import com.jj.universallprotocollibrary.PingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

val Any.tag: String get() = this.javaClass.simpleName

fun startService(context: Context, intent: Intent) {
    when {
        isAndroid8OrHigher() -> context.startForegroundService(intent)
        else -> context.startService(intent)
    }
}

fun isAndroid8OrHigher() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

infix fun String.ifIsEmpty(value: String): String = if (isEmpty()) value else this

infix fun String.ifNotEmpty(value: () -> Unit) {
    if (this.isNotEmpty()) value.invoke()
}

fun logAndPingServer(message: String, tag: String) {
    Log.d(tag, message)
    CoroutineScope(Dispatchers.IO).launch {
        PingDataCall.postSensorsData(PingData(Date(), "${Build.MODEL} $message"), DefaultCallback())
    }
}