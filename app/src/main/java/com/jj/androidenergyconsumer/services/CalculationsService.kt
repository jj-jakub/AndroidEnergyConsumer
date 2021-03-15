package com.jj.androidenergyconsumer.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.jj.androidenergyconsumer.calculations.CalculationsOrchestrator
import com.jj.androidenergyconsumer.calculations.CalculationsResult
import com.jj.androidenergyconsumer.calculations.CalculationsType
import com.jj.androidenergyconsumer.notification.CALCULATIONS_NOTIFICATION_ID
import com.jj.androidenergyconsumer.notification.NotificationType.CALCULATIONS
import com.jj.androidenergyconsumer.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class CalculationsService : BaseService() {

    private val calculationsOrchestrator: CalculationsOrchestrator by inject()
    private val coroutineScopeProvider: CoroutineScopeProvider by inject()

    private val calculationsNotification = notificationContainer.getProperNotification(CALCULATIONS)

    override val wakelockTag = "AEC:CalculationsServiceWakeLock"

    private val calculationsRunning = MutableStateFlow(false)

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
        observeCalculationsResult()
    }

    private fun observeCalculationsResult() {
        coroutineScopeProvider.getIO().launch {
            calculationsOrchestrator.observeCalculationsResult().collect { onCalculationResultReceived(it) }
        }
    }

    private fun onCalculationResultReceived(result: CalculationsResult) {
        calculationsNotification.notify("CalculationsService notification",
                "handlerId: ${result.handlerId} ${getDateStringWithMillis()} - variable = ${result.variable}")
        logAndPingServer("handlerId: ${result.handlerId}, variable = ${result.variable}", tag)
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
            setupCalculationsOrchestrator(intent)
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

    private fun setupCalculationsOrchestrator(intent: Intent) {
        val amountOfHandlers = getAmountOfHandlers(intent)
        val calculationsType = getCalculationsType(intent)
        val factor = getCalculationsFactor(intent)
        calculationsOrchestrator.startCalculations(calculationsType, factor, amountOfHandlers)
    }

    private fun getCalculationsType(intent: Intent): CalculationsType =
        (intent.getSerializableExtra(CALCULATIONS_TYPE_EXTRA) ?: DEFAULT_CALCULATIONS_TYPE) as CalculationsType

    private fun getCalculationsFactor(intent: Intent): Int =
        intent.getIntExtra(CALCULATIONS_FACTOR_EXTRA, DEFAULT_CALCULATIONS_FACTOR)

    override fun onDestroy() {
        logAndPingServer("onDestroy", tag)
        calculationsOrchestrator.abortCalculations()
        calculationsNotification.cancel()
        releaseWakeLock()
        calculationsRunning.value = false
        super.onDestroy()
    }
}