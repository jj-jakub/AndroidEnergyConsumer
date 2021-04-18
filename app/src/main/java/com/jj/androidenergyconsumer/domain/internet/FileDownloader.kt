package com.jj.androidenergyconsumer.domain.internet

import android.util.Log
import com.jj.androidenergyconsumer.domain.coroutines.BufferedMutableSharedFlow
import com.jj.androidenergyconsumer.domain.coroutines.ICoroutineScopeProvider
import com.jj.androidenergyconsumer.domain.tag
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

    private val downloadProgressFlow = BufferedMutableSharedFlow<DownloadProgress>()
    private val downloadCancelled = AtomicBoolean(false)

    private var averageDownloadSpeedKBs: Float = 0F
    private var progressPercentage: Int = 0
    private var totalBytesReceived = 0L

    fun observeDownloadProgress(): SharedFlow<DownloadProgress> = downloadProgressFlow

    fun cancelDownload() {
        downloadCancelled.set(true)
    }

    suspend fun downloadFile(sourceUrl: String) {
        Log.d(tag, "downloadFile start")
        withContext(coroutineScopeProvider.getIODispatcher()) {
            totalBytesReceived = 0L
            try {
                val connection = makeConnection(sourceUrl)

                // this will be useful so that you can show a typical 0-100% progress bar
                val fileLength = connection.contentLength
                val input = BufferedInputStream(connection.getInputStream())
                val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
                var receivedBytesAmount: Int
                Log.d(tag, "File requested for download has size in bytes: ${fileLength}B")

                val downloadStartTime = System.currentTimeMillis()
                while (true) {
                    receivedBytesAmount = input.read(buffer)
                    val downloadCancelled = downloadCancelled.get()
                    if (receivedBytesAmount == -1 || downloadCancelled) {
                        onDownloadEnd(input, downloadCancelled)
                        return@withContext
                    }
                    totalBytesReceived += receivedBytesAmount

                    broadcastProgressUpdate(downloadStartTime, fileLength)
                }

            } catch (e: IOException) {
                e.printStackTrace()
                val downloadProgress = DownloadProgress(progressPercentage, averageDownloadSpeedKBs, true, e)
                onProgressUpdate(downloadProgress)
            }
        }
    }

    private fun makeConnection(sourceUrl: String): URLConnection {
        val url = URL(sourceUrl)
        return url.openConnection().apply { connect() }
    }

    private fun broadcastProgressUpdate(downloadStartTime: Long, fileLength: Int) {
        coroutineScopeProvider.getIO().launch {
            averageDownloadSpeedKBs =
                (totalBytesReceived / 1000F) / ((System.currentTimeMillis() - downloadStartTime) / 1000F)
            progressPercentage = (totalBytesReceived * 100 / fileLength).toInt()
            val downloadProgress = DownloadProgress(progressPercentage, averageDownloadSpeedKBs, false)
            onProgressUpdate(downloadProgress)
        }
    }

    private fun onDownloadEnd(input: BufferedInputStream, downloadCancelled: Boolean) {
        input.close()

        if (!downloadCancelled) {
            val downloadProgress = DownloadProgress(100, averageDownloadSpeedKBs, true)
            onProgressUpdate(downloadProgress)
        }
    }

    private fun onProgressUpdate(downloadProgress: DownloadProgress) {
        downloadProgressFlow.tryEmit(downloadProgress)
    }
}