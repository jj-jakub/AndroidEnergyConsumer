package com.jj.androidenergyconsumer.domain.coroutines

import kotlinx.coroutines.Job

class CoroutineJobContainer {

    private var currentJob: Job? = null

    fun setCurrentJob(job: Job) {
        currentJob = job
    }

    fun cancelJob() {
        currentJob?.cancel()
    }
}