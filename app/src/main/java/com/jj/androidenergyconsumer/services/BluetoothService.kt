package com.jj.androidenergyconsumer.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.lifecycle.MutableLiveData
import com.jj.androidenergyconsumer.bluetooth.BluetoothScanner
import com.jj.androidenergyconsumer.notification.NOTIFICATION_SERVICE_ID
import com.jj.androidenergyconsumer.notification.NotificationManagerBuilder
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag

class BluetoothService: BaseService() {

    private val notificationManagerBuilder = NotificationManagerBuilder(this)
    private lateinit var wakeLock: PowerManager.WakeLock
    private val bluetoothScanner = BluetoothScanner(this)

    val isScanning = MutableLiveData(false)

    companion object : ServiceStarter {
        private const val START_SCANNING_ACTION = "START_SCANNING_ACTION"
        private const val STOP_SCANNING_ACTION = "STOP_SCANNING_ACTION"

        override fun getServiceClass() = BluetoothService::class.java

        fun startScanning(context: Context) {
            val intent = getServiceIntent(context).apply {
                action = START_SCANNING_ACTION
            }
            start(context, intent)
        }

        fun stopCalculations(context: Context) = start(context, STOP_SCANNING_ACTION)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return MyBinder(this)
    }

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
            START_SCANNING_ACTION -> onStartScanningAction()
            STOP_SCANNING_ACTION -> onStopScanningAction()
        }
        return START_NOT_STICKY
    }

    @SuppressLint("WakelockTimeout")
    private fun onStartScanningAction() {
        isScanning.value = true
        wakeLock.acquire()
        bluetoothScanner.startScanning()
        logAndPingServer("After onStartScanningAction", tag)
    }

    private fun onStopScanningAction() {
        stopSelf()
    }

    override fun onDestroy() {
        logAndPingServer("onDestroy", tag)
        bluetoothScanner.stopScanning()
        notificationManagerBuilder.cancelServiceNotification(this)
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
        isScanning.value = false
        super.onDestroy()
    }
}