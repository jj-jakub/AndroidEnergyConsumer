package com.jj.androidenergyconsumer.internet

import android.util.Log
import com.jj.androidenergyconsumer.utils.tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

class FileDownloader {

    companion object {
        private const val DOWNLOAD_BUFFER_SIZE = 16384
    }

    private val downloadCancelled = AtomicBoolean(false)

    private var downloadStartTime: Long = 0
    private var averageDownloadSpeedKBs: Float = 0F
    private var progressPercentage: Int = 0

    fun cancelDownload() {
        downloadCancelled.set(true)
    }

    suspend fun downloadFile(destinationDirPath: File, fileForDownloadName: String, sourceUrl: String,
                             onDownloadProgressChanged: (progress: Int, averageDownloadSpeedKBs: Float, downloadFinished: Boolean) -> Unit) {
        withContext(Dispatchers.IO) {
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
                        onDownloadEnd(input, output, downloadCancelled, onDownloadProgressChanged)
                        return@withContext
                    }
                    totalBytesReceived += receivedBytes

                    output.write(buffer, 0, receivedBytes)
                    Thread {
                        averageDownloadSpeedKBs =
                            (totalBytesReceived / 1000F) / ((System.currentTimeMillis() - downloadStartTime) / 1000F)
                        progressPercentage = (totalBytesReceived * 100 / fileLength).toInt()
                        onProgressUpdate(progressPercentage, averageDownloadSpeedKBs, false, onDownloadProgressChanged)
                    }.start()
                }

            } catch (e: IOException) {
                e.printStackTrace()
                onProgressUpdate(100, averageDownloadSpeedKBs, true, onDownloadProgressChanged)
            }
        }
    }

    private fun onDownloadEnd(input: BufferedInputStream, output: FileOutputStream, downloadCancelled: Boolean,
                              onDownloadProgressChanged: (progress: Int, averageDownloadSpeedKBs: Float, downloadFinished: Boolean) -> Unit) {
        // close streams
        output.flush()
        output.close()
        input.close()

        if (!downloadCancelled) onProgressUpdate(100, averageDownloadSpeedKBs, true,
                onDownloadProgressChanged)
    }

    private fun onProgressUpdate(progressPercentage: Int,
                                 averageDownloadSpeedKBs: Float,
                                 downloadFinished: Boolean,
                                 onDownloadProgressChanged: (progress: Int, averageDownloadSpeedKBs: Float, downloadFinished: Boolean) -> Unit) {
        onDownloadProgressChanged(progressPercentage, averageDownloadSpeedKBs, downloadFinished)
    }
}