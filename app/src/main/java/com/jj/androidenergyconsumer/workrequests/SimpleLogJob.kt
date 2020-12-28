package com.jj.androidenergyconsumer.workrequests

import android.app.job.JobParameters
import android.app.job.JobService
import com.jj.androidenergyconsumer.services.GPSService
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag

class SimpleLogJob : JobService() {
    override fun onStartJob(params: JobParameters?): Boolean {
        logAndPingServer(tag, "onStartJob, thread: ${Thread.currentThread().name}")
        GPSService.pingFromOutside(this)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        logAndPingServer(tag, "onStopJob")
        return true
    }
}