package com.jj.androidenergyconsumer.domain.internet

import com.jj.androidenergyconsumer.data.rest.InternetPingCallManager
import com.jj.androidenergyconsumer.domain.coroutines.BufferedMutableSharedFlow
import com.jj.androidenergyconsumer.domain.coroutines.ICoroutineScopeProvider
import com.jj.androidenergyconsumer.domain.multithreading.ThreadsOrchestrator
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Callback
import java.util.concurrent.atomic.AtomicBoolean

data class CallResult(val result: String)

class InternetPingsCreator(
        private val internetPingCallManager: InternetPingCallManager,
        private val coroutineScopeProvider: ICoroutineScopeProvider,
        private val coroutinesOrchestrator: ThreadsOrchestrator,
) {

    companion object {
        const val GOOGLE_URL = "https://google.com"
    }

    private val isWorking = AtomicBoolean(false)
    private var periodicPingJob: Job? = null
    private val lastCallResultFlow = BufferedMutableSharedFlow<CallResult>()

    fun observeLastCallResult(): SharedFlow<CallResult> = lastCallResultFlow

    fun pingUrlWithPeriod(url: String, periodMs: Long) {
        isWorking.set(true)
        periodicPingJob?.cancel()
        val callCallback = CallbackWithAction { result -> handleCallResult(result) }
        periodicPingJob = runPeriodicJob(periodMs) { internetPingCallManager.ping(url, callCallback) }
    }

    private fun runPeriodicJob(periodMs: Long, task: () -> Unit) = coroutineScopeProvider.getIO().launch {
        while (isActive) {
            coroutinesOrchestrator.launchOnceInThreads { task() }
            delay(periodMs)
        }
    }

    fun startOneAfterAnotherPings(url: String) {
        isWorking.set(true)
        @Suppress("JoinDeclarationAndAssignment")
        lateinit var callCallback: CallbackWithAction
        callCallback = CallbackWithAction { result ->
            // TODO Callback may be called after pings creator is once again restarted, then code below will be successfully called
            //  Find other way to cancel this callback
            if (isWorking.get()) {
                handleCallResult(result)
                innerStartOneAfterAnotherPings(url, callCallback)
            }
        }

        innerStartOneAfterAnotherPings(url, callCallback)
    }

    private fun innerStartOneAfterAnotherPings(url: String, callCallback: CallbackWithAction) {
        ping(url, callCallback)
    }

    private fun ping(url: String, callback: Callback<ResponseBody>) {
        coroutinesOrchestrator.launchOnceInThreads { internetPingCallManager.ping(url, callback) }
    }

    private fun handleCallResult(result: String) {
        if (isWorking.get()) lastCallResultFlow.tryEmit(CallResult(result))
    }

    fun stopWorking() {
        isWorking.set(false)
        periodicPingJob?.cancel()
        coroutinesOrchestrator.abortThreads()
    }
}