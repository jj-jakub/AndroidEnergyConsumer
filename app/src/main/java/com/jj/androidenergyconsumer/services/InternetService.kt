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
import com.jj.androidenergyconsumer.utils.getDateStringWithMillis
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag
import com.jj.androidenergyconsumer.wakelock.WakelockManager

class InternetService : BaseService() {

    private val notificationManagerBuilder = NotificationManagerBuilder(this)
    private var latestInternetCallCreator: InternetCallCreator? = null

    override val wakelockManager by lazy { WakelockManager(this) }
    override val wakelockTag = "AEC:InternetServiceWakeLock"

    val isWorking = MutableLiveData(false)
    val inputErrorMessage = MutableLiveData<String?>(null)
    val callResponse = MutableLiveData<String?>(null)

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
        val notification = notificationManagerBuilder.getServiceNotification("InternetService notification")
        startForeground(NOTIFICATION_SERVICE_ID, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logAndPingServer("onStartCommand", tag)
        resetValues()
        when (intent?.action) {
            START_PERIODIC_PINGS -> setupInternetCallCreator(intent)?.let {
                startPeriodicPingsToUrl(it, intent)
            }
            START_ONE_AFTER_ANOTHER_PINGS -> setupInternetCallCreator(intent)?.let {
                startOneAfterAnotherPings(it)
            }
            STOP_INTERNET_SERVICE -> stopService()
        }
        return START_NOT_STICKY
    }

    private fun setupInternetCallCreator(intent: Intent): InternetCallCreator? {
        logAndPingServer("setupInternetCallCreator", tag)
        val urlToPing = intent.getStringExtra(URL_TO_PING_EXTRA)
        return if (urlToPing != null) {
            createInternetCallCreator(urlToPing)
        } else {
            onUrlExtraNull()
            null
        }
    }

    private fun createInternetCallCreator(urlToPing: String): InternetCallCreator? {
        return try {
            stopInternetCallCreator()
            InternetCallCreator(urlToPing).apply { latestInternetCallCreator = this }
        } catch (iae: IllegalArgumentException) {
            Log.e(tag, "Exception while creating InternetCallCreator", iae)
            onInputError(iae.message)
            null
        }
    }

    private fun stopInternetCallCreator() {
        latestInternetCallCreator?.stopWorking()
        latestInternetCallCreator = null
    }

    private fun onInputError(message: String?) {
        inputErrorMessage.value = message
    }

    private fun onUrlExtraNull() {
        onInputError("Url extra is null")
    }

    private fun startPeriodicPingsToUrl(internetCallCreator: InternetCallCreator, intent: Intent) {
        logAndPingServer("startPeriodicPingsToUrl", tag)
        val periodBetweenPingsMs = intent.getLongExtra(PERIOD_MS_BETWEEN_PINGS_EXTRA, 1000)
        internetCallCreator.pingGoogleWithPeriod(periodBetweenPingsMs, onCallFinished)
        acquireWakeLock()
        isWorking.value = true
    }

    private fun startOneAfterAnotherPings(internetCallCreator: InternetCallCreator) {
        internetCallCreator.startOneAfterAnotherPings(onCallFinished)
        acquireWakeLock()
        isWorking.value = true
    }

    private val onCallFinished: (result: String) -> Unit = { result ->
        if (isWorking.value == true) {
            notifyNotification("${getDateStringWithMillis()} $result")
            callResponse.value = result
        }
    }

    private fun notifyNotification(content: String) =
        notificationManagerBuilder.notifyServiceNotification("InternetService notification", content)

    private fun resetValues() {
        inputErrorMessage.value = null
        callResponse.value = null
    }

    private fun stopService() {
        stopSelf()
    }

    override fun onDestroy() {
        logAndPingServer("onDestroy", tag)
        resetValues()
        stopInternetCallCreator()
        releaseWakeLock()
        notificationManagerBuilder.cancelServiceNotification(this)
        isWorking.value = false
        super.onDestroy()
    }
}