package com.jj.androidenergyconsumer.app.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.IBinder
import android.util.Log
import com.jj.androidenergyconsumer.app.gps.CustomLocationListener
import com.jj.androidenergyconsumer.app.gps.LocationListenerResult
import com.jj.androidenergyconsumer.app.notification.GPS_NOTIFICATION_ID
import com.jj.androidenergyconsumer.app.notification.NotificationType.GPS
import com.jj.androidenergyconsumer.app.utils.logAndPingServer
import com.jj.androidenergyconsumer.domain.coroutines.CoroutineScopeProvider
import com.jj.androidenergyconsumer.domain.getDateStringWithMillis
import com.jj.androidenergyconsumer.domain.tag
import com.jj.androidenergyconsumer.utils.BufferedMutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class GPSService : BaseService() {

    private val coroutineScopeProvider: CoroutineScopeProvider by inject()

    private val gpsNotification = notificationContainer.getProperNotification(GPS)
    private var locationManager: LocationManager? = null
    private val locationListener: CustomLocationListener by inject()

    override val wakelockTag = "AEC:GPSServiceWakeLock"

    private val isWorking = MutableStateFlow(false)
    private val errorMessage = BufferedMutableSharedFlow<String?>()

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
    fun observeErrorMessage(): SharedFlow<String?> = errorMessage

    override fun onBind(intent: Intent?): IBinder = MyBinder(this)

    override fun onCreate() {
        logAndPingServer("onCreate", tag)
        super.onCreate()
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
        if (!isWorking.value) {
            resetErrorMessage()
            try {
                logAndPingServer("requestLocationUpdates, locationManager: $locationManager", tag)
                locationManager?.let { manager ->
                    manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minimumPeriodMs, minimumDistanceM,
                            locationListener)
                    isWorking.value = true
                    observeGPSResults()
                    acquireWakeLock()
                } ?: run {
                    errorMessage.tryEmit("Location manager is null")
                }
            } catch (se: SecurityException) {
                Log.e(tag, "requestLocationUpdates Security exception", se)
                errorMessage.tryEmit("No location permission")
            }
        } else onServiceAlreadyRunning()
    }

    private fun observeGPSResults() {
        coroutineScopeProvider.getIO().launch {
            locationListener.observeLocationInfoUpdates().collect { result ->
                when (result) {
                    is LocationListenerResult.LocationChanged -> onLocationChanged(result)
                    else -> {
                        /* no-op */
                    }
                }
            }
        }
    }

    private fun onLocationChanged(result: LocationListenerResult.LocationChanged) {
        gpsNotification.notify("GPSService notification",
                "${getDateStringWithMillis()} loc: " +
                        "lat: ${result.location.latitude} - lon: ${result.location.longitude}")
    }

    // TODO Extract common code from services
    private fun onServiceAlreadyRunning() {
        errorMessage.tryEmit("Service is already running")
    }

    private fun resetErrorMessage() {
        errorMessage.tryEmit(null)
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