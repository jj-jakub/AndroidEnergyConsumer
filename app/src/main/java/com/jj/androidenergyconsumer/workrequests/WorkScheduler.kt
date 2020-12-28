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
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag

class WorkScheduler(private val context: Context) {

    companion object {
        private const val JOB_ID = 1
        private const val JOB_DELAY = 15 * 60 * 1000L
    }

    fun scheduleOneTimeWorkRequest() {
        logAndPingServer(tag, "scheduleOneTimeWorkRequest")
        val request = OneTimeWorkRequest.from(SimpleLogWorker::class.java)
        WorkManager.getInstance(context).enqueue(request)
    }

    fun startSimpleLogJob() {
        logAndPingServer(tag, "startSimpleLogJob")
        val jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler?
        jobScheduler?.schedule(
                JobInfo.Builder(JOB_ID, ComponentName(context, SimpleLogJob::class.java)).setPeriodic(JOB_DELAY)
                    .setPersisted(true).build())

    }
}