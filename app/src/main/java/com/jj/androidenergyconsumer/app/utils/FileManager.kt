package com.jj.androidenergyconsumer.app.utils

import android.os.Environment
import java.io.File

class FileManager {

    companion object {
        val downloadDestinationDir: File? = Environment.getExternalStorageDirectory()
        const val FILE_FOR_DOWNLOAD_NAME = "AECDownloadedFile.xyz"
    }

    fun deleteFile(destinationDir: File?, fileName: String): Boolean {
        val file = if (destinationDir != null) File(destinationDir, fileName) else File(fileName)
        return file.delete()
    }
}