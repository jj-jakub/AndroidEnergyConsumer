package com.jj.androidenergyconsumer.domain.multithreading

import com.jj.androidenergyconsumer.domain.coroutines.CoroutineJobContainer
import com.jj.androidenergyconsumer.domain.coroutines.CoroutineJobContainerFactory
import com.jj.androidenergyconsumer.domain.coroutines.ICoroutineScopeProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class CoroutinesOrchestrator(
        private val coroutineScopeProvider: ICoroutineScopeProvider,
        private val coroutineJobContainerFactory: CoroutineJobContainerFactory,
) : ThreadsOrchestrator {

    private val listOfCoroutines = mutableListOf<CoroutineJobContainer>()

    override fun launchOnceInThreads(threadsAmount: Int, task: (index: Int) -> Unit) {
        abortThreads()
        repeat(threadsAmount) { id -> setupCoroutineJobs(id, task, false) }
    }

    override fun launchInThreadsInInfiniteLoop(threadsAmount: Int, task: (index: Int) -> Unit) {
        abortThreads()
        repeat(threadsAmount) { id -> setupCoroutineJobs(id, task, true) }
    }

    private fun setupCoroutineJobs(id: Int, task: (index: Int) -> Unit, looped: Boolean) {
        val jobContainer = coroutineJobContainerFactory.create().apply {
            val coroutineJob = createCoroutineJob(id, task, looped)
            setCurrentJob(coroutineJob)
        }
        listOfCoroutines.add(jobContainer)
    }

    private fun createCoroutineJob(id: Int, task: (index: Int) -> Unit, looped: Boolean) =
        if (looped) createLoopedCoroutineJob(id, task)
        else createSingleRunCoroutineJob(id, task)

    private fun createLoopedCoroutineJob(id: Int, task: (index: Int) -> Unit): Job =
        coroutineScopeProvider.getIO().launch { while (isActive) task(id) }

    private fun createSingleRunCoroutineJob(id: Int, task: (index: Int) -> Unit): Job =
        coroutineScopeProvider.getIO().launch { task(id) }

    override fun abortThreads() {
        listOfCoroutines.forEach { it.cancelJob() }
        listOfCoroutines.clear()
    }
}