package com.jj.androidenergyconsumer.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.lifecycle.MutableLiveData
import com.jj.androidenergyconsumer.bluetooth.BluetoothScanner
import com.jj.androidenergyconsumer.bluetooth.BluetoothServiceScanningCallback
import com.jj.androidenergyconsumer.notification.NOTIFICATION_SERVICE_ID
import com.jj.androidenergyconsumer.notification.NotificationManagerBuilder
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag
import java.util.concurrent.atomic.AtomicBoolean

class BluetoothService : BaseService() {

    private val notificationManagerBuilder = NotificationManagerBuilder(this)
    private lateinit var wakeLock: PowerManager.WakeLock
    private val bluetoothScanner = BluetoothScanner(this)
    private val shouldRestartScanning = AtomicBoolean(true)

    val isScanning = MutableLiveData(false)
    val errorMessage = MutableLiveData<String?>(null)

    private val scanningCallback =
        BluetoothServiceScanningCallback(notificationManagerBuilder) { onScanningFinished() }

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

    override fun onBind(intent: Intent?): IBinder = MyBinder(this)

    override fun onCreate() {
        logAndPingServer("onCreate", tag)
        super.onCreate()
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AEC:MyWakeLock")
        val notification = notificationManagerBuilder.getServiceNotification("BluetoothService notification")
        startForeground(NOTIFICATION_SERVICE_ID, notification)
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
            wakeLock.acquire()
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
        notificationManagerBuilder.cancelServiceNotification(this)
        releaseWakeLock()
        isScanning.value = false
        super.onDestroy()
    }

    private fun releaseWakeLock() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}