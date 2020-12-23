package com.jj.androidenergyconsumer.workrequests

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.jj.androidenergyconsumer.services.GPSService
import com.jj.androidenergyconsumer.utils.LogSaver
import com.jj.androidenergyconsumer.utils.tag

class WorkScheduler(private val context: Context) {

    companion object {
        private const val JOB_ID = 1
        private const val JOB_DELAY = 15 * 60 * 1000L
    }

    fun scheduleOneTimeWorkRequest() {
        printAndSaveLog(tag, "scheduleOneTimeWorkRequest")
        val request = OneTimeWorkRequest.from(SimpleLogWorker::class.java)
        WorkManager.getInstance(context).enqueue(request)
    }

    fun startSimpleLogJob() {
        printAndSaveLog(tag, "startSimpleLogJob")
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler?
        jobScheduler?.schedule(
                JobInfo.Builder(JOB_ID, ComponentName(context, SimpleLogJob::class.java)).setPeriodic(JOB_DELAY)
                    .setPersisted(true).build())

    }
}

class SimpleLogWorker(private val context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        printAndSaveLog(tag, "doWork")
        WorkScheduler(context).scheduleOneTimeWorkRequest()
        return Result.success()
    }
}

class SimpleLogJob : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        printAndSaveLog(tag, "onStartJob, thread: ${Thread.currentThread().name}")
        GPSService.pingFromOutside(this)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        printAndSaveLog(tag, "onStopJob")
        return true
    }
}

fun printAndSaveLog(tag: String, message: String) {
    Log.d(tag, message)
    LogSaver.saveLog(tag, message)
}