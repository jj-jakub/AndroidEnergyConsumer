package com.jj.androidenergyconsumer.app.services

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.jj.androidenergyconsumer.app.bluetooth.BluetoothBroadcastResult
import com.jj.androidenergyconsumer.app.bluetooth.BluetoothScanner
import com.jj.androidenergyconsumer.app.notification.BLUETOOTH_NOTIFICATION_ID
import com.jj.androidenergyconsumer.app.notification.NotificationType.BLUETOOTH
import com.jj.androidenergyconsumer.app.utils.logAndPingServer
import com.jj.androidenergyconsumer.domain.coroutines.BufferedMutableSharedFlow
import com.jj.androidenergyconsumer.domain.coroutines.ICoroutineScopeProvider
import com.jj.androidenergyconsumer.domain.getDateStringWithMillis
import com.jj.androidenergyconsumer.domain.tag
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import java.util.concurrent.atomic.AtomicBoolean

class BluetoothService : BaseService() {

    private val coroutineScopeProvider: ICoroutineScopeProvider by inject()

    private val bluetoothNotification = notificationContainer.getProperNotification(BLUETOOTH)
    private val bluetoothScanner: BluetoothScanner by inject()

    private val shouldRestartScanning = AtomicBoolean(true)

    override val wakelockTag = "AEC:BluetoothServiceWakeLock"

    private val isScanningFlag = AtomicBoolean(false)
    private val isScanningState = MutableStateFlow(false)
    private val errorMessage = BufferedMutableSharedFlow<String?>()
    private var resultsObservingJob: Job? = null

    companion object : ServiceStarter {
        private const val START_SCANNING_ACTION = "START_SCANNING_ACTION"
        private const val STOP_SCANNING_SERVICE = "STOP_SCANNING_SERVICE"

        override fun getServiceClass() = BluetoothService::class.java

        fun startScanning(context: Context) {
            val intent = getServiceIntent(context).apply {
                action = START_SCANNING_ACTION
            }
            start(context, intent)
        }

        fun stopScanning(context: Context) = start(context, STOP_SCANNING_SERVICE)
    }

    fun observeIsScanning(): StateFlow<Boolean> = isScanningState
    fun observeErrorMessage(): SharedFlow<String?> = errorMessage

    override fun onBind(intent: Intent?): IBinder = MyBinder(this)

    override fun onCreate() {
        logAndPingServer("onCreate", tag)
        super.onCreate()
        val notification = bluetoothNotification.get()
        startForeground(BLUETOOTH_NOTIFICATION_ID, notification)
        observeBluetoothResults()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logAndPingServer("onStartCommand", tag)
        when (intent?.action) {
            START_SCANNING_ACTION -> startScanning()
            STOP_SCANNING_SERVICE -> stopService()
        }
        return START_NOT_STICKY
    }

    @SuppressLint("WakelockTimeout")
    private fun startScanning() {
        if (isScanningFlag.compareAndSet(false, true)) {
            resetErrorMessage()
            val startedScanning = bluetoothScanner.startScanning()
            if (startedScanning) {
                isScanningState.value = true
                shouldRestartScanning.set(true)
                acquireWakeLock()
            } else onStartScanningError()
        } else onServiceAlreadyRunning()
        logAndPingServer("After startScanning", tag)
    }

    private fun observeBluetoothResults() {
        cancelResultsObservingJob()
        resultsObservingJob = coroutineScopeProvider.getIO().launch {
            bluetoothScanner.observeBluetoothResults().collect { result ->
                when (result) {
                    is BluetoothBroadcastResult.DiscoveryFinished -> onScanningFinished()
                    is BluetoothBroadcastResult.FoundDevice -> onDeviceFound(result.device)
                }
            }
        }
    }

    private fun onDeviceFound(device: BluetoothDevice) {
        bluetoothNotification.notify("BluetoothService notification",
                "${getDateStringWithMillis()} device: ${device.name} - ${device.bluetoothClass?.deviceClass}")
        logAndPingServer("device: ${device.name} - ${device.bluetoothClass?.deviceClass}", tag)
    }

    private fun onServiceAlreadyRunning() {
        errorMessage.tryEmit("Service is already running")
    }

    private fun onStartScanningError() {
        isScanningFlag.set(false)
        errorMessage.tryEmit("Start scanning error. Check if bluetooth is turned on.")
    }

    private fun resetErrorMessage() {
        errorMessage.tryEmit(null)
    }

    private fun stopService() {
        stopSelf()
    }

    private fun onScanningFinished() {
        isScanningState.value = false
        isScanningFlag.set(false)
        logAndPingServer("After onScanningFinished", tag)

        if (shouldRestartScanning.get()) {
            startScanning()
        } else {
            stopSelf()
        }
    }

    private fun cancelResultsObservingJob() {
        resultsObservingJob?.cancel()
    }

    override fun onDestroy() {
        logAndPingServer("onDestroy", tag)
        cancelResultsObservingJob()
        shouldRestartScanning.set(false)
        bluetoothScanner.stopScanning()
        bluetoothNotification.cancel()
        releaseWakeLock()
        isScanningState.value = false
        super.onDestroy()
    }
}