package com.jj.androidenergyconsumer.services

import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.jj.androidenergyconsumer.internet.DownloadProgress
import com.jj.androidenergyconsumer.internet.FileDownloader
import com.jj.androidenergyconsumer.internet.InternetCallCreator
import com.jj.androidenergyconsumer.notification.INTERNET_NOTIFICATION_ID
import com.jj.androidenergyconsumer.notification.NotificationType.INTERNET
import com.jj.androidenergyconsumer.utils.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class InternetService : BaseService() {

    private val coroutineScopeProvider: CoroutineScopeProvider by inject()

    private val internetNotification = notificationContainer.getProperNotification(INTERNET)
    private val internetCallCreator: InternetCallCreator by inject()
    private val fileDownloader: FileDownloader by inject()
    private val fileManager: FileManager by inject()

    private var lastKnownSourceUrl: String? = null

    override val wakelockTag = "AEC:InternetServiceWakeLock"

    private val isWorking = MutableStateFlow(false)
    private val inputErrorMessage = BufferedMutableSharedFlow<String?>()
    private val callResponse = BufferedMutableSharedFlow<String?>()

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
    fun observeCallResponse(): SharedFlow<String?> = callResponse

    override fun onBind(intent: Intent?): IBinder = MyBinder(this)

    override fun onCreate() {
        logAndPingServer("onCreate", tag)
        super.onCreate()
        val notification = internetNotification.get()
        startForeground(INTERNET_NOTIFICATION_ID, notification)
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
            startFileDownload(url)
        } ?: onUrlExtraNull()
    }

    private fun getUrlFromIntent(intent: Intent): String? {
        logAndPingServer("getUrlFromIntent", tag)
        val url = intent.getStringExtra(URL_FOR_WORK_EXTRA)
        if (url == null) onUrlExtraNull()
        return url
    }

    private fun startFileDownload(url: String) {
        val destinationDir = FileManager.downloadDestinationDir
        if (destinationDir == null) {
            onDestinationDirNull()
            return
        }

        acquireWakeLock()
        isWorking.value = true
        coroutineScopeProvider.getIO().launch {
            fileDownloader.observeDownloadProgress().collect { onDownloadProgressChanged(it) }
            fileDownloader.downloadFile(destinationDir, FileManager.FILE_FOR_DOWNLOAD_NAME, url)
        }
    }

    private fun onDownloadProgressChanged(downloadProgress: DownloadProgress) {
        if (downloadProgress.exception != null) onDownloadException(downloadProgress.exception)
        else if (downloadProgress.downloadFinished) onFileDownloadingCompleted()
    }

    private fun onFileDownloadingCompleted() {
        Log.d(tag, "onFileDownloadingCompleted")
        val successfullyDeleted = deleteDownloadedFile()
        if (!successfullyDeleted) onFileDeleteFailed()
        else restartFileDownload()
    }

    private fun deleteDownloadedFile() =
        fileManager.deleteFile(FileManager.downloadDestinationDir, FileManager.FILE_FOR_DOWNLOAD_NAME)

    private fun restartFileDownload() {
        lastKnownSourceUrl?.let { startFileDownload(it) } ?: stopSelf()
    }

    private fun onProcessingError(message: String?) {
        Log.e(tag, "onProcessingError: $message")
        inputErrorMessage.tryEmit(message)
    }

    private fun onDownloadException(exception: Exception?) {
        onProcessingError(exception?.message ?: "Exception while downloading file")
        stopWorking()
        deleteDownloadedFile()
    }

    private fun onUrlExtraNull() {
        onProcessingError("Url extra is null")
    }

    private fun onDestinationDirNull() {
        onProcessingError("Fatal error - destination dir is null")
    }

    private fun onFileDeleteFailed() {
        onProcessingError("Failed to remove downloaded file")
    }

    private fun startPeriodicPingsToUrl(url: String, intent: Intent) {
        isWorking.value = true
        logAndPingServer("startPeriodicPingsToUrl", tag)
        val periodBetweenPingsMs = intent.getLongExtra(PERIOD_MS_BETWEEN_PINGS_EXTRA, 1000)
        internetCallCreator.pingUrlWithPeriod(url, periodBetweenPingsMs, onCallFinished)
        acquireWakeLock()
    }

    private fun startOneAfterAnotherPings(url: String) {
        isWorking.value = true
        internetCallCreator.startOneAfterAnotherPings(url, onCallFinished)
        acquireWakeLock()
    }

    private val onCallFinished: (result: String) -> Unit = { result ->
        if (isWorking.value) {
            notifyNotification("${getDateStringWithMillis()} $result")
            callResponse.tryEmit(result)
        }
    }

    private fun notifyNotification(content: String) =
        internetNotification.notify("InternetService notification", content)

    private fun resetValues() {
        inputErrorMessage.tryEmit(null)
        callResponse.tryEmit(null)
    }

    override fun onDestroy() {
        logAndPingServer("onDestroy", tag)
        resetValues()
        internetCallCreator.stopWorking()
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