package com.jj.androidenergyconsumer.services

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import com.jj.androidenergyconsumer.calculations.CalculationsCallback
import com.jj.androidenergyconsumer.calculations.CalculationsProvider
import com.jj.androidenergyconsumer.calculations.CalculationsProviderFactory
import com.jj.androidenergyconsumer.calculations.CalculationsType
import com.jj.androidenergyconsumer.handlers.HandlersOrchestrator
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

    private val handlersOrchestrator = HandlersOrchestrator()

    private val notificationManagerBuilder = NotificationManagerBuilder(this)
    private lateinit var wakeLock: PowerManager.WakeLock

    private val calculationsCallback = object : CalculationsCallback {
        override fun onThresholdAchieved(variable: Int, handlerId: Int) {
            notificationManagerBuilder.notifyServiceNotification("CalculationsService notification",
                    "handlerId: $handlerId ${Date()} - variable = $variable")
            logAndPingServer("handlerId: $handlerId, variable = $variable")
        }
    }

    companion object : ServiceStarter {
        private const val START_CALCULATIONS_ACTION = "START_CALCULATIONS_ACTION"
        private const val STOP_CALCULATIONS_ACTION = "STOP_CALCULATIONS_ACTION"
        private const val NUMBER_OF_HANDLERS_EXTRA = "NUMBER_OF_HANDLERS_EXTRA"
        const val DEFAULT_NUMBER_OF_HANDLERS = 4

        private const val CALCULATIONS_TYPE_EXTRA = "CALCULATIONS_TYPE"
        private val DEFAULT_CALCULATIONS_TYPE = CalculationsType.ADDITION

        private const val CALCULATIONS_FACTOR_EXTRA = "CALCULATIONS_FACTOR"
        const val DEFAULT_CALCULATIONS_FACTOR = 2

        override fun getServiceClass() = CalculationsService::class.java

        fun startCalculations(context: Context, type: CalculationsType, numberOfHandlers: Int, factor: Int) {
            val intent = getServiceIntent(context).apply {
                action = START_CALCULATIONS_ACTION
                putExtra(CALCULATIONS_TYPE_EXTRA, type)
                putExtra(NUMBER_OF_HANDLERS_EXTRA, numberOfHandlers)
                putExtra(CALCULATIONS_FACTOR_EXTRA, factor)
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
            START_CALCULATIONS_ACTION -> onStartCalculationsAction(intent)
            STOP_CALCULATIONS_ACTION -> onStopCalculationsAction()
        }
        return START_NOT_STICKY
    }

    private fun onStopCalculationsAction() {
        // TODO update fragment label
        stopSelf()
    }

    private fun onStartCalculationsAction(intent: Intent) {
        wakeLock.acquire()
        val amountOfHandlers = intent.getIntExtra(NUMBER_OF_HANDLERS_EXTRA, DEFAULT_NUMBER_OF_HANDLERS)
        val calculationsType =
            (intent.getSerializableExtra(CALCULATIONS_TYPE_EXTRA) ?: DEFAULT_CALCULATIONS_TYPE) as CalculationsType
        val factor = intent.getIntExtra(CALCULATIONS_FACTOR_EXTRA, DEFAULT_CALCULATIONS_FACTOR)
        val calculationsProvider =
            CalculationsProviderFactory.createCalculationsProvider(calculationsType, calculationsCallback, factor)
        restartCalculations(amountOfHandlers, calculationsProvider)
        // TODO update fragment label
    }

    private fun restartCalculations(amountOfHandlers: Int, calculationsProvider: CalculationsProvider) {
        disposeHandlers()
        initHandlers(amountOfHandlers)
        launchCalculations(calculationsProvider)
        logAndPingServer("After restartCalculations")
    }

    private fun disposeHandlers() {
        handlersOrchestrator.disposeHandlers()
        logAndPingServer("After disposeHandlers")
    }

    private fun initHandlers(amountOfHandlers: Int) {
        handlersOrchestrator.initHandlers(amountOfHandlers)
        logAndPingServer("After initHandlers")
    }

    private fun launchCalculations(calculationsProvider: CalculationsProvider) {
        handlersOrchestrator.launchInEveryHandlerInInfiniteLoop(calculationsProvider)
        logAndPingServer("After launchCalculationsOne")
    }

    private fun logAndPingServer(message: String) {
        Log.d(tag, message)
        CoroutineScope(Dispatchers.IO).launch {
            PingDataCall.postSensorsData(PingData(Date(), "${Build.MODEL} $message"), DefaultCallback())
        }
    }

    override fun onDestroy() {
        logAndPingServer("onDestroy")
        disposeHandlers()
        notificationManagerBuilder.cancelServiceNotification(this)
        wakeLock.release()
        super.onDestroy()
    }
}