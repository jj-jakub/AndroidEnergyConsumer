package com.jj.androidenergyconsumer.app.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.jj.androidenergyconsumer.app.AECApplication
import com.jj.androidenergyconsumer.domain.coroutines.CoroutineScopeProvider
import com.jj.androidenergyconsumer.domain.coroutines.ICoroutineScopeProvider
import com.jj.androidenergyconsumer.domain.tag
import kotlinx.coroutines.launch


fun isAndroid6OrHigher() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
fun isAndroid8OrHigher() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

fun logAndPingServer(message: String, tag: String,
                     coroutineScopeProvider: ICoroutineScopeProvider = CoroutineScopeProvider()) {
    Log.d(tag, message)
    LogSaver.saveLog(tag, message)
    coroutineScopeProvider.getIO().launch {
//        PingDataCall.postSensorsData(PingData(Date(), "${Build.MODEL} $message"), DefaultCallback())
    }
}

fun showShortToast(message: String) =
    Toast.makeText(AECApplication.instance.applicationContext, message, Toast.LENGTH_SHORT).show()

fun BroadcastReceiver.safelyUnregisterReceiver(context: Context) {
    try {
        context.unregisterReceiver(this)
    } catch (iae: IllegalArgumentException) {
        Log.e(tag, "Error when unregistering receiver", iae)
    }
}