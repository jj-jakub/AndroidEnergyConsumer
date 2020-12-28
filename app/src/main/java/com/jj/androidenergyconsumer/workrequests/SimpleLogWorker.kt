package com.jj.androidenergyconsumer.workrequests

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag

class SimpleLogWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        logAndPingServer(tag, "doWork")
        WorkScheduler(context).scheduleOneTimeWorkRequest()
        return Result.success()
    }
}