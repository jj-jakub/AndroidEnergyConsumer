package com.jj.androidenergyconsumer.workrequests

import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.jj.androidenergyconsumer.utils.LogSaver
import com.jj.androidenergyconsumer.utils.tag

class WorkScheduler(private val context: Context) {
    fun schedule() {
        val request = OneTimeWorkRequest.from(SimpleLogWorker::class.java)
        WorkManager.getInstance(context).enqueue(request)
    }
}

class SimpleLogWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        Log.d(tag, "doWork")
        LogSaver.saveLog(tag, "doWork")
        WorkScheduler(context).schedule()
        return Result.success()
    }
}