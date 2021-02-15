package com.jj.androidenergyconsumer.utils

import android.os.Build
import android.util.Log
import android.widget.Toast
import com.jj.androidenergyconsumer.AECApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

val Any.tag: String get() = this.javaClass.simpleName ifIsEmpty "DefaultTag"

fun isAndroid6OrHigher() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
fun isAndroid8OrHigher() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

infix fun String.ifIsEmpty(value: String): String = if (isEmpty()) value else this

infix fun String.ifNotEmpty(value: () -> Unit) {
    if (this.isNotEmpty()) value.invoke()
}

fun logAndPingServer(message: String, tag: String) {
    Log.d(tag, message)
    LogSaver.saveLog(tag, message)
    CoroutineScope(Dispatchers.IO).launch {
//        PingDataCall.postSensorsData(PingData(Date(), "${Build.MODEL} $message"), DefaultCallback())
    }
}

fun getDateStringWithMillis(): String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT).format(Date())

fun showShortToast(message: String) =
    Toast.makeText(AECApplication.instance.applicationContext, message, Toast.LENGTH_SHORT).show()

fun Float.roundAsString(decimals: Int = 2): String = "%.${decimals}f".format(Locale.US, this)