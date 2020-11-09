package com.jj.androidenergyconsumer.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.*
import android.util.Log
import com.jj.androidenergyconsumer.handlers.StoppableHandler
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
    private val handlerThreadName = "HThread"
    private var handlerThreads: List<HandlerThread>? = null
    private var loopers: List<Looper>? = null
    private var stoppableHandlers: List<StoppableHandler>? = null

    private val notificationManagerBuilder = NotificationManagerBuilder(this)
    private lateinit var wakeLock: PowerManager.WakeLock

    companion object : ServiceStarter {
        private const val START_CALCULATIONS_ACTION = "START_CALCULATIONS_ACTION"
        private const val STOP_CALCULATIONS_ACTION = "STOP_CALCULATIONS_ACTION"
        private const val NUMBER_OF_HANDLERS_EXTRA = "NUMBER_OF_HANDLERS_EXTRA"
        const val DEFAULT_NUMBER_OF_HANDLERS = 4

        override fun getServiceClass() = CalculationsService::class.java

        fun startCalculations(context: Context, numberOfHandlers: Int = DEFAULT_NUMBER_OF_HANDLERS) {
            val intent = getServiceIntent(context).apply {
                action = START_CALCULATIONS_ACTION
                putExtra(NUMBER_OF_HANDLERS_EXTRA, numberOfHandlers)
            }
            start(context, intent)
        }

        fun stopCalculations(context: Context) = start(context, STOP_CALCULATIONS_ACTION)
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        logAndPingServer("onCreate")
        super.onCreate()
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AEC:MyWakeLock")
        val notification = notificationManagerBuilder.getServiceNotification("CalculationsService notification")
        startForeground(NOTIFICATION_SERVICE_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logAndPingServer("onStartCommand")
        when (intent?.action) {
            START_CALCULATIONS_ACTION -> {
                val amountOfHandlers = intent.getIntExtra(NUMBER_OF_HANDLERS_EXTRA, DEFAULT_NUMBER_OF_HANDLERS)
                restartCalculationsOne(amountOfHandlers)
            }
            STOP_CALCULATIONS_ACTION -> stopSelf()
        }
        return START_NOT_STICKY
    }

    private fun restartCalculationsOne(amountOfHandlers: Int) {
        disposeHandlers()
        initHandlers(amountOfHandlers)
        launchCalculationsOne()
        logAndPingServer("After restartCalculations")
    }

    private fun disposeHandlers() {
        synchronized(blockLock) {
            stoppableHandlers?.forEach { it.quitHandler() }
            stoppableHandlers = null
            loopers?.forEach { it.quit() }
            loopers = null
            handlerThreads?.forEach { it.quit() }
            handlerThreads = null
            logAndPingServer("After disposeHandler")
        }
    }

    private fun initHandlers(amountOfHandlers: Int) {
        synchronized(blockLock) {
            handlerThreads = createListOfHandlerThreads(amountOfHandlers)
            handlerThreads?.forEach { it.start() }
            stoppableHandlers = createListOfHandlers()
            logAndPingServer("After initHandler")
        }
    }

    override fun onDestroy() {
        logAndPingServer("onDestroy")
        disposeHandlers()
        notificationManagerBuilder.cancelServiceNotification(this)
        super.onDestroy()
    }

    private fun launchCalculationsOne() {
        synchronized(blockLock) {
            wakeLock.acquire()
            stoppableHandlers?.forEachIndexed { index, stoppableHandler ->
                stoppableHandler.executeInInfiniteLoop { handlerInfiniteAdditionLoop(index, stoppableHandler) }
            }
            logAndPingServer("After launchCalculationsOne")
        }
    }

    private fun handlerInfiniteAdditionLoop(handlerId: Int, stoppableHandler: StoppableHandler) {
        var a = 0
        while (true) {
            a += 2
            if (a % 100000000 == 0) {
                Log.d("ABAB", "handlerId: $handlerId variable: $a")
                if (stoppableHandler.isHandlerStopped().not()) {
                    onThresholdAchieved(a, handlerId)
                }
                break
            }
        }
    }

    private fun onThresholdAchieved(variable: Int, handlerId: Int) {
        notificationManagerBuilder.notifyServiceNotification("CalculationsService notification",
                "handlerId: $handlerId ${Date()} - variable = $variable")
        logAndPingServer("handlerId: $handlerId, variable = $variable")
    }

    private fun createListOfHandlerThreads(amountOfHandlers: Int): List<HandlerThread> {
        val handlersList = mutableListOf<HandlerThread>()
        for (i in 0 until amountOfHandlers) {
            handlersList.add(HandlerThread("$handlerThreadName $i"))
        }
        return handlersList.toList()
    }

    private fun createListOfHandlers(): List<StoppableHandler>? {
        val mutableListOfHandlers = mutableListOf<StoppableHandler>()
        handlerThreads?.forEach {
            it.looper?.let { looper -> mutableListOfHandlers.add(StoppableHandler(looper)) }
        }
        return mutableListOfHandlers.toList()
    }

    private fun logAndPingServer(message: String) {
        Log.d(tag, message)
        CoroutineScope(Dispatchers.IO).launch {
            PingDataCall.postSensorsData(PingData(Date(), "${Build.MODEL} $message"), DefaultCallback())
        }
    }
}