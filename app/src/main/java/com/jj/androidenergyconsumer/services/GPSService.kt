package com.jj.androidenergyconsumer.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationListener
import android.location.LocationManager
import android.os.HandlerThread
import android.os.IBinder
import android.util.Log
import com.jj.androidenergyconsumer.gps.MyLocationListener
import com.jj.androidenergyconsumer.notification.GPS_NOTIFICATION_ID
import com.jj.androidenergyconsumer.notification.NotificationType.GPS
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GPSService : BaseService() {

    private val gpsNotification = notificationContainer.getProperNotification(GPS)
    private var locationManager: LocationManager? = null
    private val locationListener: LocationListener = MyLocationListener(gpsNotification)

    override val wakelockTag = "AEC:GPSServiceWakeLock"

    private val isWorking = MutableStateFlow(false)

    companion object : ServiceStarter {
        private const val START_CONSTANT_UPDATES = "START_CONSTANT_UPDATES"
        private const val START_PERIODIC_UPDATES = "START_PERIODIC_UPDATES"
        private const val STOP_GPS_SERVICE = "STOP_SCANNING_SERVICE"
        private const val MINIMUM_PERIOD_MS_EXTRA = "MINIMUM_PERIOD_MS_EXTRA"

        override fun getServiceClass() = GPSService::class.java

        fun startConstantUpdates(context: Context) {
            startWithAction(context, START_CONSTANT_UPDATES)
        }

        fun startPeriodicUpdates(context: Context, minimumPeriodMs: Long) {
            startWithAction(context, START_PERIODIC_UPDATES, minimumPeriodMs)
        }

        private fun startWithAction(context: Context, intentAction: String, minimumPeriodMs: Long? = null) {
            val intent = getServiceIntent(context).apply {
                action = intentAction
                minimumPeriodMs?.let { period -> putExtra(MINIMUM_PERIOD_MS_EXTRA, period) }
            }
            start(context, intent)
        }

        fun stopGpsService(context: Context) = start(context, STOP_GPS_SERVICE)
    }

    fun observeIsWorking(): StateFlow<Boolean> = isWorking

    override fun onBind(intent: Intent?): IBinder = MyBinder(this)

    override fun onCreate() {
        logAndPingServer("onCreate", tag)
        super.onCreate()

        handlerThread.start()

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager?
        val notification = gpsNotification.get()
        startForeground(GPS_NOTIFICATION_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logAndPingServer("onStartCommand", tag)
        when (intent?.action) {
            START_CONSTANT_UPDATES -> startConstantUpdates()
            START_PERIODIC_UPDATES -> startPeriodicUpdates(intent)
            STOP_GPS_SERVICE -> stopService()
        }
        return START_NOT_STICKY
    }

    private val handlerThread: HandlerThread = HandlerThread("InternetThread")

    private fun startConstantUpdates() {
        logAndPingServer("startConstantUpdates", tag)
        requestLocationUpdates(0, 0F)
    }

    private fun startPeriodicUpdates(intent: Intent) {
        logAndPingServer("startPeriodicUpdates", tag)
        val minimumPeriodMs = intent.getLongExtra(MINIMUM_PERIOD_MS_EXTRA, 0)
        requestLocationUpdates(minimumPeriodMs, 0F)
    }

    @Suppress("SameParameterValue")
    @SuppressLint("WakelockTimeout")
    private fun requestLocationUpdates(minimumPeriodMs: Long, minimumDistanceM: Float) {
        try {
            logAndPingServer("requestLocationUpdates, locationManager: $locationManager", tag)
            locationManager?.let { manager ->
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minimumPeriodMs, minimumDistanceM,
                        locationListener)
                acquireWakeLock()
                isWorking.value = true
            }
        } catch (se: SecurityException) {
            Log.e(tag, "requestLocationUpdates Security exception", se)
        }
    }

    private fun stopService() {
        stopSelf()
    }

    override fun onDestroy() {
        logAndPingServer("onDestroy", tag)
        locationManager?.removeUpdates(locationListener)
        gpsNotification.cancel()
        releaseWakeLock()
        isWorking.value = false
        super.onDestroy()
    }
}