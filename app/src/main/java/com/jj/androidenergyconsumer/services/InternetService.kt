package com.jj.androidenergyconsumer.services

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.lifecycle.MutableLiveData
import com.jj.androidenergyconsumer.internet.InternetCallCreator
import com.jj.androidenergyconsumer.notification.NOTIFICATION_SERVICE_ID
import com.jj.androidenergyconsumer.notification.NotificationManagerBuilder
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InternetService : BaseService() {

    private val notificationManagerBuilder = NotificationManagerBuilder(this)
    private lateinit var wakeLock: PowerManager.WakeLock
    private val internetCallCreator = InternetCallCreator(InternetCallCreator.GOOGLE_URL)

    val isWorking = MutableLiveData(false)

    companion object : ServiceStarter {
        private const val START_PERIODIC_PINGS = "START_PERIODIC_PINGS"
        private const val START_ONE_AFTER_ANOTHER_PINGS = "START_ONE_AFTER_ANOTHER_PINGS"
        private const val STOP_INTERNET_SERVICE = "STOP_INTERNET_SERVICE"
        private const val PERIOD_MS_BETWEEN_PINGS_EXTRA = "PERIOD_MS_BETWEEN_PINGS_EXTRA"

        override fun getServiceClass() = InternetService::class.java

        fun startPeriodicPings(context: Context, periodBetweenPings: Long) {
            startWithAction(context, START_PERIODIC_PINGS, periodBetweenPings)
        }

        fun startOneAfterAnotherPings(context: Context) {
            startWithAction(context, START_ONE_AFTER_ANOTHER_PINGS)
        }

        private fun startWithAction(context: Context, intentAction: String, periodBetweenPings: Long? = null) {
            val intent = getServiceIntent(context).apply {
                action = intentAction
                periodBetweenPings?.let { period -> putExtra(PERIOD_MS_BETWEEN_PINGS_EXTRA, period) }
            }
            start(context, intent)
        }

        fun stopInternetService(context: Context) = start(context, STOP_INTERNET_SERVICE)
    }

    override fun onBind(intent: Intent?): IBinder? = MyBinder(this)

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
            START_PERIODIC_PINGS -> startPeriodicPings(intent)
            START_ONE_AFTER_ANOTHER_PINGS -> startOneAfterAnotherPings()
            STOP_INTERNET_SERVICE -> stopService()
        }
        return START_NOT_STICKY
    }

    private fun startPeriodicPings(intent: Intent) {
        isWorking.value = true
        logAndPingServer("startPeriodicPings", tag)
        val periodBetweenPingsMs = intent.getLongExtra(PERIOD_MS_BETWEEN_PINGS_EXTRA, 1000)
        internetCallCreator.pingGoogleWithPeriod(periodBetweenPingsMs)
    }

    private fun startOneAfterAnotherPings() {
        isWorking.value = true
        logAndPingServer("startOneAfterAnotherPings", tag)
        internetCallCreator.ping(loopCallCallback)
    }

    private val loopCallCallback: Callback<ResponseBody> = object : Callback<ResponseBody> {
        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            logAndPingServer("onFailure", tag)
            startOneAfterAnotherPings()
        }

        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            logAndPingServer("onResponse", tag)
            startOneAfterAnotherPings()
        }
    }

    private fun stopService() {
        stopSelf()
    }

    override fun onDestroy() {
        logAndPingServer("onDestroy", tag)
        internetCallCreator.stopWorking()
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