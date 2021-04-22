package com.jj.androidenergyconsumer.domain.multithreading

interface ThreadsOrchestrator {
    fun launchOnceInThreads(threadsAmount: Int = 1, task: (index: Int) -> Unit)
    fun launchInThreadsInInfiniteLoop(threadsAmount: Int = 1, task: (index: Int) -> Unit)
    fun abortThreads()
}