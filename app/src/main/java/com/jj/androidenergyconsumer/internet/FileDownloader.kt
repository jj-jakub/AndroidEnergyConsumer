package com.jj.androidenergyconsumer.internet

import android.os.Environment
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL

class FileDownloader {
    //TODO make it cancellable
    suspend fun downloadFile(urlToDownload: String, onDownloadProgressChanged: (progress: Int) -> Unit) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(urlToDownload)
                val connection = url.openConnection()
                connection.connect()

                // this will be useful so that you can show a typical 0-100% progress bar
                val fileLength = connection.contentLength

                // download the file
                val input = BufferedInputStream(connection.getInputStream())

                val fileToSave = File(Environment.getExternalStorageDirectory(), "AECDownloadedFile.xyz")
                val output = FileOutputStream(fileToSave)

                val data = ByteArray(8192)
                var total = 0L
                var count: Int

                while (true) {
                    count = input.read(data)
                    if (count == -1) break
                    total += count

                    output.write(data, 0, count)
                    onProgressUpdate((total * 100 / fileLength).toInt(), onDownloadProgressChanged)
                }

                // close streams
                output.flush()
                output.close()
                input.close()

            } catch (e: IOException) {
                e.printStackTrace()
                // return
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