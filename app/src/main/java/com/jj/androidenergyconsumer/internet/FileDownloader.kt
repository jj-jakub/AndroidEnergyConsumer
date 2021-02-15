package com.jj.androidenergyconsumer.internet

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL
import java.util.concurrent.atomic.AtomicBoolean

class FileDownloader {

    private val downloadCancelled = AtomicBoolean(false)

    fun cancelDownload() {
        downloadCancelled.set(true)
    }

    suspend fun downloadFile(destinationDirPath: File, fileForDownloadName: String, sourceUrl: String,
                             onDownloadProgressChanged: (progress: Int) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(sourceUrl)
                val connection = url.openConnection()
                connection.connect()

                // this will be useful so that you can show a typical 0-100% progress bar
                val fileLength = connection.contentLength

                // download the file
                val input = BufferedInputStream(connection.getInputStream())

                val fileToSave = File(destinationDirPath, fileForDownloadName)
                val output = FileOutputStream(fileToSave)

                val buffer = ByteArray(8192)
                var totalBytesReceived = 0L
                var receivedBytes: Int

                while (true) {
                    receivedBytes = input.read(buffer)
                    if (receivedBytes == -1 || downloadCancelled.get()) break
                    totalBytesReceived += receivedBytes

                    output.write(buffer, 0, receivedBytes)
                    onProgressUpdate((totalBytesReceived * 100 / fileLength).toInt(), onDownloadProgressChanged)
                }

                // close streams
                output.flush()
                output.close()
                input.close()

            } catch (e: IOException) {
                e.printStackTrace()
            }
            onProgressUpdate(100, onDownloadProgressChanged)
        }
    }

    private fun onProgressUpdate(progressPercentage: Int, onDownloadProgressChanged: (progress: Int) -> Unit) {
        //TODO update ui on main thread
        Log.d("ABAB", "onProgressUpdate, $progressPercentage%")
        onDownloadProgressChanged(progressPercentage)
    }
}