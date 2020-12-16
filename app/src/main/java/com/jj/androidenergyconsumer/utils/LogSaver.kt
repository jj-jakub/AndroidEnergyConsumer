package com.jj.androidenergyconsumer.utils

import android.os.Environment
import android.util.Log
import java.io.*

object LogSaver {

    private const val LOGS_FILE_NAME = "LOGS_FILE.txt"

    private var bufferedFileWriter: BufferedWriter? = null
    private var currentFile: File? = null

    private fun createWriter() {
        try {
            val file = File(Environment.getExternalStorageDirectory(), LOGS_FILE_NAME)
            bufferedFileWriter = BufferedWriter(FileWriter(file, true))
        } catch (e: FileNotFoundException) {
            Log.e(tag, "Lack of permission", e)
        }
    }

    @Synchronized
    fun saveLog(tag: String?, message: String?) {
        if (currentFile?.exists() != true || bufferedFileWriter == null) createWriter()
        try {
            bufferedFileWriter?.append("${getDateStringWithMillis()} $tag: $message\n")
            bufferedFileWriter?.flush()
        } catch (ioe: IOException) {
            Log.e(this.tag, "Error while writing to file", ioe)
        }
    }
}