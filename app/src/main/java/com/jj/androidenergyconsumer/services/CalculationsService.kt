package com.jj.androidenergyconsumer.services

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.jj.androidenergyconsumer.notification.NOTIFICATION_SERVICE_ID
import com.jj.androidenergyconsumer.notification.NotificationManagerBuilder
import com.jj.androidenergyconsumer.rest.DefaultCallback
import com.jj.androidenergyconsumer.rest.PingDataCall
import com.jj.androidenergyconsumer.utils.tag
import com.jj.universallprotocollibrary.PingData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CalculationsService : Service() {

    private val blockLock = Any()
    private val handlerThreadName = "name"
    private var handlerThread: HandlerThread? = null
    private var looper: Looper? = null
    private var handler: Handler? = null

    private val notificationManagerBuilder = NotificationManagerBuilder(this)

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        logAndPingServer("onCreate")
        super.onCreate()
        val notification = notificationManagerBuilder.getServiceNotification("CalculationsService notification")
        startForeground(NOTIFICATION_SERVICE_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logAndPingServer("onStartCommand")
        restartCalculations()
        return START_STICKY
    }

    private fun restartCalculations() {
        disposeHandler()
        initHandler()
        launchCalculationsOne()
        logAndPingServer("After restartCalculations")
    }

    private fun disposeHandler() {
        synchronized(blockLock) {
            handlerThread?.quit()
            handlerThread = null
            looper = null
            handler = null
            logAndPingServer("After disposeHandler")
        }
    }

    private fun initHandler() {
        synchronized(blockLock) {
            handlerThread = HandlerThread(handlerThreadName)
            handlerThread?.start()
            handlerThread?.looper?.let { handler = Handler(it) }
            logAndPingServer("After initHandler")
        }
    }

    private fun launchCalculationsOne() {
        synchronized(blockLock) {
            handler?.post(infiniteAdditionLoop)
            logAndPingServer("After launchCalculationsOne")
        }
    }

    override fun onDestroy() {
        logAndPingServer("onDestroy")
        abortCalculationsOne()
        disposeHandler()
        super.onDestroy()
    }

    private fun abortCalculationsOne() {
        synchronized(blockLock) {
            handlerThread?.quit()
            logAndPingServer("After abortCalculationsOne")
        }
    }

    private val infiniteAdditionLoop = {
        var a = 0
        while (true) {
            a += 2
            if (a % 1000000000 == 0) {
                notificationManagerBuilder.notifyServiceNotification("CalculationsService notification",
                        "${Date()} - a = $a")
                logAndPingServer("a = $a")
            }
        }
    }

    private fun logAndPingServer(message: String) {
        Log.d(tag, message)
        CoroutineScope(Dispatchers.IO).launch {
            PingDataCall.postSensorsData(PingData(Date(), message), DefaultCallback())
        }
    }
}