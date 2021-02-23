package com.jj.androidenergyconsumer.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.jj.androidenergyconsumer.calculations.CalculationsCallback
import com.jj.androidenergyconsumer.calculations.CalculationsProviderFactory
import com.jj.androidenergyconsumer.calculations.CalculationsType
import com.jj.androidenergyconsumer.handlers.HandlersOrchestrator
import com.jj.androidenergyconsumer.notification.CALCULATIONS_NOTIFICATION_ID
import com.jj.androidenergyconsumer.notification.NotificationContainer
import com.jj.androidenergyconsumer.notification.NotificationType.CALCULATIONS
import com.jj.androidenergyconsumer.utils.getDateStringWithMillis
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.showShortToast
import com.jj.androidenergyconsumer.utils.tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.android.ext.android.inject

class CalculationsService : BaseService() {

    private val handlersOrchestrator = HandlersOrchestrator()

    private val notificationContainer: NotificationContainer by inject()
    private val calculationsNotification = notificationContainer.getProperNotification(CALCULATIONS)

    override val wakelockTag = "AEC:CalculationsServiceWakeLock"

    private val calculationsRunning = MutableStateFlow(false)

    private val calculationsCallback = object : CalculationsCallback {
        override fun onThresholdAchieved(variable: Int, handlerId: Int) {
            calculationsNotification.notify("CalculationsService notification",
                    "handlerId: $handlerId ${getDateStringWithMillis()} - variable = $variable")
            logAndPingServer("handlerId: $handlerId, variable = $variable", tag)
        }
    }

    companion object : ServiceStarter {
        private const val START_CALCULATIONS_ACTION = "START_CALCULATIONS_ACTION"
        private const val STOP_CALCULATIONS_ACTION = "STOP_CALCULATIONS_ACTION"
        const val NUMBER_OF_HANDLERS_EXTRA = "NUMBER_OF_HANDLERS_EXTRA"
        const val DEFAULT_NUMBER_OF_HANDLERS = 4

        const val CALCULATIONS_TYPE_EXTRA = "CALCULATIONS_TYPE"
        val DEFAULT_CALCULATIONS_TYPE = CalculationsType.ADDITION

        const val CALCULATIONS_FACTOR_EXTRA = "CALCULATIONS_FACTOR"
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

    fun observeCalculationsRunning(): StateFlow<Boolean> = calculationsRunning

    override fun onBind(intent: Intent?): IBinder = MyBinder(this)

    override fun onCreate() {
        logAndPingServer("onCreate", tag)
        super.onCreate()
        startForeground(CALCULATIONS_NOTIFICATION_ID, calculationsNotification.get())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logAndPingServer("onStartCommand", tag)
        when (intent?.action) {
            START_CALCULATIONS_ACTION -> onStartCalculationsAction(intent)
            STOP_CALCULATIONS_ACTION -> onStopCalculationsAction()
        }
        return START_NOT_STICKY
    }

    private fun onStopCalculationsAction() {
        stopSelf()
    }

    @SuppressLint("WakelockTimeout")
    private fun onStartCalculationsAction(intent: Intent) {
        try {
            val amountOfHandlers = getAmountOfHandlers(intent)
            val calculationsProvider =
                CalculationsProviderFactory.createCalculationsProvider(intent, calculationsCallback)
            handlersOrchestrator.launchInEveryHandlerInInfiniteLoop(amountOfHandlers, calculationsProvider)
            calculationsRunning.value = true
            acquireWakeLock()
        } catch (iae: IllegalArgumentException) {
            Log.e(tag, "Exception when starting calculations", iae)
            showShortToast("Exception: ${iae.message}") // TODO Unify - create error label in fragment
        }
        logAndPingServer("After onStartCalculationsAction", tag)
    }

    private fun getAmountOfHandlers(intent: Intent): Int =
        intent.getIntExtra(NUMBER_OF_HANDLERS_EXTRA, DEFAULT_NUMBER_OF_HANDLERS)

    override fun onDestroy() {
        logAndPingServer("onDestroy", tag)
        handlersOrchestrator.abortHandlers()
        calculationsNotification.cancel()
        releaseWakeLock()
        calculationsRunning.value = false
        super.onDestroy()
    }
}