package com.jj.androidenergyconsumer.domain.calculations

import com.jj.androidenergyconsumer.app.handlers.HandlersOrchestrator
import com.jj.androidenergyconsumer.domain.coroutines.BufferedMutableSharedFlow
import com.jj.androidenergyconsumer.domain.coroutines.CoroutineJobContainer
import com.jj.androidenergyconsumer.domain.coroutines.ICoroutineScopeProvider
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class CalculationsOrchestrator(private val calculationsProviderFactory: CalculationsProviderFactory,
                               private val coroutineScopeProvider: ICoroutineScopeProvider,
                               private val coroutineJobContainer: CoroutineJobContainer = CoroutineJobContainer(),
                               private val handlersOrchestrator: HandlersOrchestrator = HandlersOrchestrator()) {

    private val calculationsResultFlow = BufferedMutableSharedFlow<CalculationsResult>()
    fun observeCalculationsResult(): SharedFlow<CalculationsResult> = calculationsResultFlow

    fun startCalculations(calculationsType: CalculationsType, factor: Int, amountOfHandlers: Int) {
        abortCalculations()
        val calculationsProvider = calculationsProviderFactory.createCalculationsProvider(calculationsType, factor)

        val resultsCollectingJob = coroutineScopeProvider.getIO().launch {
            calculationsProvider.observeCalculationsResult().collect { calculationsResultFlow.tryEmit(it) }
        }

        coroutineJobContainer.setCurrentJob(resultsCollectingJob)

        handlersOrchestrator.launchInThreadsInInfiniteLoop(amountOfHandlers) { id ->
            calculationsProvider.calculationsTask(id)
        }
    }

    fun abortCalculations() {
        handlersOrchestrator.abortThreads()
        coroutineJobContainer.cancelJob()
    }
}