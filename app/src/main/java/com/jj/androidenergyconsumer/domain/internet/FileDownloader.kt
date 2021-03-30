package com.jj.androidenergyconsumer.domain.internet

import android.util.Log
import com.jj.androidenergyconsumer.domain.coroutines.BufferedMutableSharedFlow
import com.jj.androidenergyconsumer.domain.coroutines.ICoroutineScopeProvider
import com.jj.androidenergyconsumer.domain.tag
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

data class DownloadProgress(val progressPercentage: Int, val averageDownloadSpeedKBs: Float,
                            val downloadFinished: Boolean, val exception: Exception? = null)

class FileDownloader(private val coroutineScopeProvider: ICoroutineScopeProvider) {

    companion object {
        private const val DOWNLOAD_BUFFER_SIZE = 16384
    }

    private val downloadProgressFlow = BufferedMutableSharedFlow<DownloadProgress>()
    private val downloadCancelled = AtomicBoolean(false)

    private var downloadStartTime: Long = 0
    private var averageDownloadSpeedKBs: Float = 0F
    private var progressPercentage: Int = 0

    fun observeDownloadProgress(): SharedFlow<DownloadProgress> = downloadProgressFlow

    fun cancelDownload() {
        downloadCancelled.set(true)
    }

    suspend fun downloadFile(destinationDirPath: File, fileForDownloadName: String, sourceUrl: String) {
        withContext(coroutineScopeProvider.getIODispatcher()) {
            var totalBytesReceived = 0L
            try {
                val url = URL(sourceUrl)
                val connection = url.openConnection()
                connection.connect()

                // this will be useful so that you can show a typical 0-100% progress bar
                val fileLength = connection.contentLength
                Log.d(tag, "File requested for download has size in bytes: ${fileLength}B")

                // download the file
                val input = BufferedInputStream(connection.getInputStream())

                val fileToSave = File(destinationDirPath, fileForDownloadName)
                val output = FileOutputStream(fileToSave)

                val buffer = ByteArray(DOWNLOAD_BUFFER_SIZE)
                var receivedBytes: Int

                downloadStartTime = System.currentTimeMillis()
                while (true) {
                    receivedBytes = input.read(buffer)
                    val downloadCancelled = downloadCancelled.get()
                    if (receivedBytes == -1 || downloadCancelled) {
                        onDownloadEnd(input, output, downloadCancelled)
                        return@withContext
                    }
                    totalBytesReceived += receivedBytes

                    output.write(buffer, 0, receivedBytes)
                    Thread {
                        averageDownloadSpeedKBs =
                            (totalBytesReceived / 1000F) / ((System.currentTimeMillis() - downloadStartTime) / 1000F)
                        progressPercentage = (totalBytesReceived * 100 / fileLength).toInt()
                        val downloadProgress = DownloadProgress(progressPercentage, averageDownloadSpeedKBs, false)
                        onProgressUpdate(downloadProgress)
                    }.start()
                }

            } catch (e: IOException) {
                e.printStackTrace()
                val downloadProgress = DownloadProgress(progressPercentage, averageDownloadSpeedKBs, true, e)
                onProgressUpdate(downloadProgress)
            }
        }
    }

    private fun onDownloadEnd(input: BufferedInputStream, output: FileOutputStream, downloadCancelled: Boolean) {
        // close streams
        output.flush()
        output.close()
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