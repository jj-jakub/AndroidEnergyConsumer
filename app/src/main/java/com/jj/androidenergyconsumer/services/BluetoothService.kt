package com.jj.androidenergyconsumer.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.IBinder
import com.jj.androidenergyconsumer.bluetooth.BluetoothScanner
import com.jj.androidenergyconsumer.bluetooth.BluetoothServiceScanningCallback
import com.jj.androidenergyconsumer.notification.BLUETOOTH_NOTIFICATION_ID
import com.jj.androidenergyconsumer.notification.NotificationType.BLUETOOTH
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicBoolean

class BluetoothService : BaseService() {

    private val bluetoothNotification = notificationContainer.getProperNotification(BLUETOOTH)
    private val bluetoothScanner = BluetoothScanner(this)
    private val shouldRestartScanning = AtomicBoolean(true)

    override val wakelockTag = "AEC:BluetoothServiceWakeLock"

    private val isScanning = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val scanningCallback = BluetoothServiceScanningCallback(bluetoothNotification) { onScanningFinished() }

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

    fun observeIsScanning(): StateFlow<Boolean> = isScanning
    fun observeErrorMessage(): StateFlow<String?> = errorMessage

    override fun onBind(intent: Intent?): IBinder = MyBinder(this)

    override fun onCreate() {
        logAndPingServer("onCreate", tag)
        super.onCreate()
        val notification = bluetoothNotification.get()
        startForeground(BLUETOOTH_NOTIFICATION_ID, notification)
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
        resetErrorMessage()
        val startedScanning = bluetoothScanner.startScanning(scanningCallback)
        if (startedScanning) {
            shouldRestartScanning.set(true)
            isScanning.value = true
            acquireWakeLock()
        } else {
            onStartScanningError()
        }
        logAndPingServer("After startScanning", tag)
    }

    private fun onStartScanningError() {
        errorMessage.value = "Start scanning error. Check if bluetooth is turned on."
    }

    private fun resetErrorMessage() {
        errorMessage.value = null
    }

    private fun stopService() {
        stopSelf()
    }

    private fun onScanningFinished() {
        releaseWakeLock()
        isScanning.value = false
        logAndPingServer("After onScanningFinished", tag)

        if (shouldRestartScanning.get()) {
            startScanning()
        }
    }

    override fun onDestroy() {
        logAndPingServer("onDestroy", tag)
        shouldRestartScanning.set(false)
        bluetoothScanner.stopScanning()
        bluetoothNotification.cancel()
        releaseWakeLock()
        isScanning.value = false
        super.onDestroy()
    }
}