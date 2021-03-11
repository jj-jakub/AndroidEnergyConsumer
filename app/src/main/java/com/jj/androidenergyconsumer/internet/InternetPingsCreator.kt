package com.jj.androidenergyconsumer.internet

import android.os.HandlerThread
import com.jj.androidenergyconsumer.handlers.StoppableLoopedHandler
import com.jj.androidenergyconsumer.rest.InternetPingCallManager
import com.jj.androidenergyconsumer.utils.BufferedMutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.ResponseBody
import retrofit2.Callback

data class CallResult(val result: String)

class InternetPingsCreator {

    companion object {
        const val GOOGLE_URL = "https://google.com"
    }

    private var handlerThread: HandlerThread? = null
    private var stoppableHandler: StoppableLoopedHandler? = null
    private val internetPingCallManager = InternetPingCallManager()

    private val lastCallResultFlow = BufferedMutableSharedFlow<CallResult>()

    fun observeLastCallResult(): SharedFlow<CallResult> = lastCallResultFlow

    fun pingUrlWithPeriod(url: String, periodMs: Long) {
        restartThreads()
        val callCallback = CallbackWithAction {
            lastCallResultFlow.tryEmit(CallResult(it))
        }
        stoppableHandler?.executeInInfiniteLoop({
            internetPingCallManager.ping(url, callCallback)
        }, periodMs)
    }

    fun startOneAfterAnotherPings(url: String) {
        restartThreads()

        @Suppress("JoinDeclarationAndAssignment")
        lateinit var callCallback: CallbackWithAction
        callCallback = CallbackWithAction { result ->
            lastCallResultFlow.tryEmit(CallResult(result))
            innerStartOneAfterAnotherPings(url, callCallback)
        }

        innerStartOneAfterAnotherPings(url, callCallback)
    }

    private fun innerStartOneAfterAnotherPings(url: String, callCallback: CallbackWithAction) {
        ping(url, callCallback)
    }

    private fun restartThreads() {
        stopWorking()
        handlerThread = HandlerThread("InternetThread").apply {
            start()
            stoppableHandler = StoppableLoopedHandler(looper)
        }
    }

    private fun ping(url: String, callback: Callback<ResponseBody>) {
        stoppableHandler?.post { internetPingCallManager.ping(url, callback) }
    }

    fun stopWorking() {
        stoppableHandler?.quitHandler()
        stoppableHandler = null
        handlerThread?.apply {
            looper?.quit()
            quit()
        }
    }
}