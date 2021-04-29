package com.jj.androidenergyconsumer.app.services

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.jj.androidenergyconsumer.app.notification.INTERNET_NOTIFICATION_ID
import com.jj.androidenergyconsumer.app.notification.NotificationType.INTERNET
import com.jj.androidenergyconsumer.app.utils.logAndPingServer
import com.jj.androidenergyconsumer.data.internet.DownloadProgress
import com.jj.androidenergyconsumer.data.internet.FileDownloader
import com.jj.androidenergyconsumer.data.internet.InternetPingsCreator
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

class InternetService : BaseService() {

    private val coroutineScopeProvider: ICoroutineScopeProvider by inject()

    private val internetNotification = notificationContainer.getProperNotification(INTERNET)
    private val internetPingsCreator: InternetPingsCreator by inject()
    private val fileDownloader: FileDownloader by inject()

    private var fileDownloadsCount = 0
    private var lastKnownSourceUrl: String? = null

    override val wakelockTag = "AEC:InternetServiceWakeLock"

    private val isWorking = MutableStateFlow(false)
    private val inputErrorMessage = BufferedMutableSharedFlow<String?>()
    private val fileDownloadsCountFlow = BufferedMutableSharedFlow<Int>()
    private var observingDownloadProgressJob: Job? = null

    companion object : ServiceStarter {
        private const val START_PERIODIC_PINGS = "START_PERIODIC_PINGS"
        private const val START_ONE_AFTER_ANOTHER_PINGS = "START_ONE_AFTER_ANOTHER_PINGS"
        private const val DOWNLOAD_FILE_ACTION = "DOWNLOAD_FILE_ACTION"
        private const val STOP_INTERNET_SERVICE = "STOP_INTERNET_SERVICE"
        private const val PERIOD_MS_BETWEEN_PINGS_EXTRA = "PERIOD_MS_BETWEEN_PINGS_EXTRA"
        private const val URL_FOR_WORK_EXTRA = "URL_FOR_WORK_EXTRA"

        override fun getServiceClass() = InternetService::class.java

        fun startPeriodicPings(context: Context, periodBetweenPings: Long, urlToPing: String) {
            startWithAction(context, START_PERIODIC_PINGS, urlToPing, periodBetweenPings)
        }

        fun startOneAfterAnotherPings(context: Context, urlToPing: String) {
            startWithAction(context, START_ONE_AFTER_ANOTHER_PINGS, urlToPing)
        }

        fun startFileDownload(context: Context, urlToDownloadFrom: String) {
            startWithAction(context, DOWNLOAD_FILE_ACTION, urlToDownloadFrom)
        }

        private fun startWithAction(context: Context, intentAction: String, urlForWork: String,
                                    periodBetweenPings: Long? = null) {
            val intent = getServiceIntent(context).apply {
                action = intentAction
                periodBetweenPings?.let { period -> putExtra(PERIOD_MS_BETWEEN_PINGS_EXTRA, period) }
                putExtra(URL_FOR_WORK_EXTRA, urlForWork)
            }
            start(context, intent)
        }

        fun stopInternetService(context: Context) = start(context, STOP_INTERNET_SERVICE)
    }

    fun observeIsWorking(): StateFlow<Boolean> = isWorking
    fun observeInputErrorMessage(): SharedFlow<String?> = inputErrorMessage
    fun observeDownloadsCount(): SharedFlow<Int> = fileDownloadsCountFlow

    override fun onBind(intent: Intent?): IBinder = MyBinder(this)

    override fun onCreate() {
        logAndPingServer("onCreate", tag)
        super.onCreate()
        val notification = internetNotification.get()
        startForeground(INTERNET_NOTIFICATION_ID, notification)
        observeLastCallResults()
        observingDownloadProgressJob = observeDownloadProgress()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        logAndPingServer("onStartCommand", tag)
        when (intent?.action) {
            STOP_INTERNET_SERVICE -> stopSelf()
            else -> onWorkCommandReceived(intent)
        }
        return START_NOT_STICKY
    }

    @Synchronized
    private fun onWorkCommandReceived(intent: Intent?) {
        if (isWorking.value) {
            onProcessingError("Service is currently doing work")
            return
        }

        resetValues()

        when (intent?.action) {
            START_PERIODIC_PINGS -> getUrlFromIntent(intent)?.let {
                startPeriodicPingsToUrl(it, intent)
            }
            START_ONE_AFTER_ANOTHER_PINGS -> getUrlFromIntent(intent)?.let {
                startOneAfterAnotherPings(it)
            }
            DOWNLOAD_FILE_ACTION -> onDownloadFileRequested(intent)
        }
    }

    private fun onDownloadFileRequested(intent: Intent) {
        intent.getStringExtra(URL_FOR_WORK_EXTRA)?.let { url ->
            lastKnownSourceUrl = url
            resetFileDownloadsCount()
            startFileDownload(url)
        } ?: onUrlExtraNull()
    }

    private fun resetFileDownloadsCount() {
        fileDownloadsCount = 0
        fileDownloadsCountFlow.tryEmit(fileDownloadsCount)
    }

    private fun getUrlFromIntent(intent: Intent): String? {
        logAndPingServer("getUrlFromIntent", tag)
        val url = intent.getStringExtra(URL_FOR_WORK_EXTRA)
        if (url == null) onUrlExtraNull()
        return url
    }

    private fun startFileDownload(url: String) {
        Log.d(tag, "startFileDownload")

        acquireWakeLock()
        isWorking.value = true
        coroutineScopeProvider.getIO().launch {
            fileDownloader.downloadFile(url)
        }
    }

    private fun observeDownloadProgress(): Job {
        observingDownloadProgressJob?.cancel()
        return coroutineScopeProvider.getIO().launch {
            fileDownloader.observeDownloadProgress().collect { onDownloadProgressChanged(it) }
        }
    }

    private fun onDownloadProgressChanged(downloadProgress: DownloadProgress) {
        Log.d(tag, "downloadProgress: $downloadProgress")
        if (downloadProgress.exception != null) onDownloadException(downloadProgress.exception)
        if (downloadProgress.downloadFinished) onFileDownloadingCompleted()
    }

    private fun onFileDownloadingCompleted() {
        Log.d(tag, "onFileDownloadingCompleted")
        incrementFileDownloadsCount()
        restartFileDownload()
    }

    private fun incrementFileDownloadsCount() {
        fileDownloadsCount++
        fileDownloadsCountFlow.tryEmit(fileDownloadsCount)
    }

    private fun restartFileDownload() {
        lastKnownSourceUrl?.let { startFileDownload(it) } ?: stopSelf()
    }

    private fun onProcessingError(message: String?) {
        Log.e(tag, "onProcessingError: $message")
        inputErrorMessage.tryEmit(message)
    }

    private fun onDownloadException(exception: Exception?) {
        onProcessingError(exception?.message ?: "Exception while downloading file")
    }

    private fun onUrlExtraNull() {
        onProcessingError("Url extra is null")
    }

    private fun startPeriodicPingsToUrl(url: String, intent: Intent) {
        isWorking.value = true
        logAndPingServer("startPeriodicPingsToUrl", tag)
        val periodBetweenPingsMs = intent.getLongExtra(PERIOD_MS_BETWEEN_PINGS_EXTRA, 1000)
        internetPingsCreator.pingUrlWithPeriod(url, periodBetweenPingsMs)
        acquireWakeLock()
    }

    private fun startOneAfterAnotherPings(url: String) {
        isWorking.value = true
        internetPingsCreator.startOneAfterAnotherPings(url)
        acquireWakeLock()
    }

    private fun observeLastCallResults() {
        coroutineScopeProvider.getIO().launch {
            internetPingsCreator.observeLastCallResult().collect {
                if (isWorking.value) {
                    notifyNotification("${getDateStringWithMillis()} ${it.result}")
                }
            }
        }
    }

    private fun notifyNotification(content: String) =
        internetNotification.notify("InternetService notification", content)

    private fun resetValues() {
        inputErrorMessage.tryEmit(null)
    }

    override fun onDestroy() {
        logAndPingServer("onDestroy", tag)
        observingDownloadProgressJob?.cancel()
        resetValues()
        internetPingsCreator.stopWorking()
        stopWorking()
        fileDownloader.cancelDownload()
        internetNotification.cancel()
        super.onDestroy()
    }

    private fun stopWorking() {
        releaseWakeLock()
        isWorking.value = false
    }
}