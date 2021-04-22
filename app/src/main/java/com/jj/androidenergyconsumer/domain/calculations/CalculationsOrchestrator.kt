package com.jj.androidenergyconsumer.domain.calculations

import com.jj.androidenergyconsumer.domain.coroutines.BufferedMutableSharedFlow
import com.jj.androidenergyconsumer.domain.coroutines.CoroutineJobContainer
import com.jj.androidenergyconsumer.domain.coroutines.ICoroutineScopeProvider
import com.jj.androidenergyconsumer.domain.multithreading.ThreadsOrchestrator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CalculationsOrchestrator(
        private val calculationsProviderFactory: CalculationsProviderFactory,
        private val coroutineScopeProvider: ICoroutineScopeProvider,
        private val resultsCollectingJobContainer: CoroutineJobContainer = CoroutineJobContainer(),
        private val coroutinesOrchestrator: ThreadsOrchestrator,
) {

    private var currentlyUsedCalculationsProvider: CalculationsProvider? = null

    private val calculationsResultFlow = BufferedMutableSharedFlow<CalculationsResult>()
    fun observeCalculationsResult(): SharedFlow<CalculationsResult> = calculationsResultFlow

    fun startCalculations(calculationsType: CalculationsType, factor: Int, threadsAmount: Int) {
        abortCalculations()

        /** One provider is used in many threads - race condition,
         * but purpose is just to load CPU, not to have synchronized results */
        val calculationsProvider = calculationsProviderFactory.createCalculationsProvider(calculationsType, factor)
        currentlyUsedCalculationsProvider = calculationsProvider

        setObserveResultsJob(calculationsProvider)

        coroutinesOrchestrator.launchInThreadsInInfiniteLoop(threadsAmount) { id ->
            calculationsProvider.startCalculationsTask(id)
        }
    }

    private fun setObserveResultsJob(calculationsProvider: CalculationsProvider) {
        val resultsCollectingJob = coroutineScopeProvider.getIO().launch {
            calculationsProvider.observeCalculationsResult().collect { calculationsResultFlow.tryEmit(it) }
        }
        resultsCollectingJobContainer.setCurrentJob(resultsCollectingJob)
    }

    fun abortCalculations() {
        currentlyUsedCalculationsProvider?.abortCalculationsTask()
        currentlyUsedCalculationsProvider = null

        coroutinesOrchestrator.abortThreads()
        resultsCollectingJobContainer.cancelJob()
    }
}