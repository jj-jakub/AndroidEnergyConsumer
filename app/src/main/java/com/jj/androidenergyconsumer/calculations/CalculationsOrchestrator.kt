package com.jj.androidenergyconsumer.calculations

import com.jj.androidenergyconsumer.handlers.HandlersOrchestrator
import com.jj.androidenergyconsumer.utils.BufferedMutableSharedFlow
import com.jj.androidenergyconsumer.utils.CoroutineScopeProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class CalculationsResult(val variable: Int, val handlerId: Int)

class CalculationsOrchestrator(private val calculationsProviderFactory: CalculationsProviderFactory,
                               private val coroutineScopeProvider: CoroutineScopeProvider) {

    private lateinit var currentCalculationsProvider: CalculationsProvider
    private var resultsCollectingJob: Job? = null

    private val handlersOrchestrator = HandlersOrchestrator()

    private val calculationsResultFlow = BufferedMutableSharedFlow<CalculationsResult>()
    fun observeCalculationsResult(): SharedFlow<CalculationsResult> = calculationsResultFlow

    fun startCalculations(calculationsType: CalculationsType, factor: Int, amountOfHandlers: Int) {
        abortCalculations()
        currentCalculationsProvider = calculationsProviderFactory.createCalculationsProvider(calculationsType, factor)

        resultsCollectingJob = coroutineScopeProvider.getIO().launch {
            currentCalculationsProvider.observeCalculationsResult().collect { calculationsResultFlow.tryEmit(it) }
        }

        handlersOrchestrator.launchInEveryHandlerInInfiniteLoop(amountOfHandlers, currentCalculationsProvider)
    }

    fun abortCalculations() {
        handlersOrchestrator.abortHandlers()
        resultsCollectingJob?.cancel()
    }
}