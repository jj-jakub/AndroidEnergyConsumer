package com.jj.androidenergyconsumer.internet

import com.jj.androidenergyconsumer.handlers.StoppableLoopedThread
import com.jj.androidenergyconsumer.rest.InternetPingCallManager
import com.jj.androidenergyconsumer.utils.BufferedMutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.ResponseBody
import retrofit2.Callback

data class CallResult(val result: String)

class InternetPingsCreator(private val internetPingCallManager: InternetPingCallManager,
                           private val stoppableLoopedThread: StoppableLoopedThread = StoppableLoopedThread()) {

    companion object {
        const val GOOGLE_URL = "https://google.com"
    }

    private val lastCallResultFlow = BufferedMutableSharedFlow<CallResult>()

    fun observeLastCallResult(): SharedFlow<CallResult> = lastCallResultFlow

    fun pingUrlWithPeriod(url: String, periodMs: Long) {
        restartThread()
        val callCallback = CallbackWithAction { result -> handleCallResult(result) }
        stoppableLoopedThread.executeInInfiniteLoop({ internetPingCallManager.ping(url, callCallback) }, periodMs)
    }

    fun startOneAfterAnotherPings(url: String) {
        restartThread()

        @Suppress("JoinDeclarationAndAssignment")
        lateinit var callCallback: CallbackWithAction
        callCallback = CallbackWithAction { result ->
            handleCallResult(result)
            innerStartOneAfterAnotherPings(url, callCallback)
        }

        innerStartOneAfterAnotherPings(url, callCallback)
    }

    private fun innerStartOneAfterAnotherPings(url: String, callCallback: CallbackWithAction) {
        ping(url, callCallback)
    }

    private fun restartThread() {
        stoppableLoopedThread.restartThread()
    }

    private fun ping(url: String, callback: Callback<ResponseBody>) {
        stoppableLoopedThread.post { internetPingCallManager.ping(url, callback) }
    }

    private fun handleCallResult(result: String) = lastCallResultFlow.tryEmit(CallResult(result))

    fun stopWorking() = stoppableLoopedThread.stopThread()
}