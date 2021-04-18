package com.jj.androidenergyconsumer.domain.internet

import android.util.Log
import com.jj.androidenergyconsumer.domain.coroutines.BufferedMutableSharedFlow
import com.jj.androidenergyconsumer.domain.coroutines.ICoroutineScopeProvider
import com.jj.androidenergyconsumer.domain.tag
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.IOException
import java.net.URL
import java.net.URLConnection
import java.util.concurrent.atomic.AtomicBoolean

data class DownloadProgress(val progressPercentage: Int, val averageDownloadSpeedKBs: Float,
                            val downloadFinished: Boolean, val exception: Exception? = null)

class FileDownloader(private val coroutineScopeProvider: ICoroutineScopeProvider) {

    companion object {
        private const val DOWNLOAD_BUFFER_SIZE = 16384
    }

    private val downloadProgressFlow =
        BufferedMutableSharedFlow<DownloadProgress>(0, onBufferOverflow = BufferOverflow.SUSPEND)
    private val downloadCancelled = AtomicBoolean(false)

    private var avgDownloadSpeedKBs: Float = 0F
    private var progressPercentage: Int = 0
    private var totalBytesReceived = 0L
    private var fileLength = 0

    fun observeDownloadProgress(): SharedFlow<DownloadProgress> = downloadProgressFlow

    fun cancelDownload() {
        downloadCancelled.set(true)
    }

    suspend fun downloadFile(sourceUrl: String) {
        Log.d(tag, "downloadFile start")
        var input: BufferedInputStream? = null
        val connection: URLConnection
        downloadCancelled.set(false)
        totalBytesReceived = 0L
        withContext(coroutineScopeProvider.getIODispatcher()) {
            try {
                connection = makeConnection(sourceUrl)
                fileLength = connection.contentLength
                input = BufferedInputStream(connection.getInputStream())
                Log.d(tag, "File requested for download has size in bytes: ${fileLength}B")

                readBytesInLoop(input)

            } catch (e: IOException) {
                e.printStackTrace()
                val downloadProgress = DownloadProgress(progressPercentage, avgDownloadSpeedKBs, true, e)
                input?.close()

                onProgressUpdate(downloadProgress)
            }
        }
    }

    private fun readBytesInLoop(input: BufferedInputStream?) {
        val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
        var receivedBytesAmount: Int
        val downloadStartTime = System.currentTimeMillis()

        while (true) {
            receivedBytesAmount = input?.read(buffer) ?: -1
            totalBytesReceived += receivedBytesAmount
            val downloadCancelled = downloadCancelled.get()
            if (receivedBytesAmount == -1 || downloadCancelled) {
                onDownloadEnd(input, downloadCancelled)
                return
            }

            broadcastProgressUpdate(downloadStartTime)
        }
    }

    private fun makeConnection(sourceUrl: String): URLConnection {
        val url = URL(sourceUrl)
        return url.openConnection().apply { connect() }
    }

    private fun broadcastProgressUpdate(downloadStartTime: Long) {
        coroutineScopeProvider.getIO().launch {
            calculateDownloadInfo(downloadStartTime)
            val downloadProgress = DownloadProgress(progressPercentage, avgDownloadSpeedKBs, false)
            onProgressUpdate(downloadProgress)
        }
    }

    private fun calculateDownloadInfo(downloadStartTime: Long) {
        avgDownloadSpeedKBs = (totalBytesReceived / 1000F) / ((System.currentTimeMillis() - downloadStartTime) / 1000F)
        progressPercentage = (totalBytesReceived * 100 / fileLength).toInt()
    }

    private fun onDownloadEnd(input: BufferedInputStream?, downloadCancelled: Boolean) {
        input?.close()

        if (!downloadCancelled) {
            val downloadProgress = DownloadProgress(100, avgDownloadSpeedKBs, true)
            coroutineScopeProvider.getIO().launch {
                onProgressUpdate(downloadProgress)
            }
        }
    }

    private suspend fun onProgressUpdate(downloadProgress: DownloadProgress) {
        downloadProgressFlow.emit(downloadProgress)
    }
}