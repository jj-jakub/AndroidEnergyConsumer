package com.jj.androidenergyconsumer.services

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.jj.androidenergyconsumer.internet.InternetCallCreator
import com.jj.androidenergyconsumer.notification.NOTIFICATION_SERVICE_ID
import com.jj.androidenergyconsumer.notification.NotificationManagerBuilder
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag
import java.util.*

class InternetService : BaseService() {

    private val notificationManagerBuilder = NotificationManagerBuilder(this)
    private lateinit var wakeLock: PowerManager.WakeLock
    private var internetCallCreator: InternetCallCreator? = null

    val isWorking = MutableLiveData(false)
    val errorMessage = MutableLiveData<String>(null)

    companion object : ServiceStarter {
        private const val START_PERIODIC_PINGS = "START_PERIODIC_PINGS"
        private const val START_ONE_AFTER_ANOTHER_PINGS = "START_ONE_AFTER_ANOTHER_PINGS"
        private const val STOP_INTERNET_SERVICE = "STOP_INTERNET_SERVICE"
        private const val PERIOD_MS_BETWEEN_PINGS_EXTRA = "PERIOD_MS_BETWEEN_PINGS_EXTRA"
        private const val URL_TO_PING_EXTRA = "PERIOD_MS_BETWEEN_PINGS_EXTRA"

        override fun getServiceClass() = InternetService::class.java

        fun startPeriodicPings(context: Context, periodBetweenPings: Long, urlToPing: String) {
            startWithAction(context, START_PERIODIC_PINGS, periodBetweenPings, urlToPing)
        }

        fun startOneAfterAnotherPings(context: Context, urlToPing: String) {
            startWithAction(context, START_ONE_AFTER_ANOTHER_PINGS, urlToPing = urlToPing)
        }

        private fun startWithAction(context: Context, intentAction: String, periodBetweenPings: Long? = null,
                                    urlToPing: String) {
            val intent = getServiceIntent(context).apply {
                action = intentAction
                periodBetweenPings?.let { period -> putExtra(PERIOD_MS_BETWEEN_PINGS_EXTRA, period) }
                putExtra(URL_TO_PING_EXTRA, urlToPing)
            }
            start(context, intent)
        }

        fun stopInternetService(context: Context) = start(context, STOP_INTERNET_SERVICE)
    }

    override fun onBind(intent: Intent?): IBinder = MyBinder(this)

    override fun onCreate() {
        logAndPingServer("onCreate", tag)
        super.onCreate()
        wakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "AEC:InternetServiceWakeLock")
        val notification = notificationManagerBuilder.getServiceNotification("InternetService notification")
        startForeground(NOTIFICATION_SERVICE_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logAndPingServer("onStartCommand", tag)
        when (intent?.action) {
            START_PERIODIC_PINGS -> onStartPeriodicPingsCommand(intent)
            START_ONE_AFTER_ANOTHER_PINGS -> onStartOneAfterAnotherPingsCommand(intent)
            STOP_INTERNET_SERVICE -> stopService()
        }
        return START_NOT_STICKY
    }

    private fun onStartPeriodicPingsCommand(intent: Intent) {
        val urlToPing = intent.getStringExtra(URL_TO_PING_EXTRA)
        val periodBetweenPingsMs = intent.getLongExtra(PERIOD_MS_BETWEEN_PINGS_EXTRA, 1000)
        if (urlToPing != null) {
            createInternetCallCreator(urlToPing)
            startPeriodicPingsToUrl(periodBetweenPingsMs)
        } else {
            onUrlExtraNull()
        }
    }

    private fun startPeriodicPingsToUrl(periodBetweenPingsMs: Long) {
        logAndPingServer("startPeriodicPings", tag)
        internetCallCreator?.let { callCreator ->
            callCreator.pingGoogleWithPeriod(periodBetweenPingsMs, onCallFinished)
            isWorking.value = true
        }
    }

    private fun onUrlExtraNull() {
        onError("Url extra is null")
    }

    private fun onStartOneAfterAnotherPingsCommand(intent: Intent) {
        logAndPingServer("startOneAfterAnotherPings", tag)
        val urlToPing = intent.getStringExtra(URL_TO_PING_EXTRA)
        if (urlToPing != null) {
            createInternetCallCreator(urlToPing)
            startOneAfterAnotherPings()
        } else {
            onUrlExtraNull()
        }
    }

    private fun startOneAfterAnotherPings() {
        internetCallCreator?.let { callCreator ->
            callCreator.startOneAfterAnotherPings(onCallFinished)
            isWorking.value = true
        }
    }

    private val onCallFinished: (result: String) -> Unit = { result ->
        notificationManagerBuilder.notifyServiceNotification("InternetService notification",
                "${Date()} $result")
    }

    private fun createInternetCallCreator(urlToPing: String) {
        try {
            errorMessage.value = null
            internetCallCreator?.stopWorking()
            internetCallCreator = InternetCallCreator(urlToPing)
        } catch (iae: IllegalArgumentException) {
            Log.e(tag, "Exception while creating InternetCallCreator", iae)
            onError(iae.message)
        }
    }

    private fun onError(message: String?) {
        errorMessage.value = message
    }

    private fun stopService() {
        stopSelf()
    }

    override fun onDestroy() {
        logAndPingServer("onDestroy", tag)
        internetCallCreator?.stopWorking()
        notificationManagerBuilder.cancelServiceNotification(this)
        releaseWakeLock()
        isWorking.value = false
        super.onDestroy()
    }

    private fun releaseWakeLock() {
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}