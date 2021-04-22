package com.jj.androidenergyconsumer.app.handlers

interface ThreadsOrchestrator {
    fun launchInThreadsInInfiniteLoop(threadsAmount: Int, task: (index: Int) -> Unit)
    fun abortThreads()
}