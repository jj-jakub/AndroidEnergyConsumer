package com.jj.androidenergyconsumer.internet

import android.os.HandlerThread
import com.jj.androidenergyconsumer.handlers.StoppableLoopedHandler
import com.jj.androidenergyconsumer.rest.InternetPingCallManager
import okhttp3.ResponseBody
import retrofit2.Callback

class InternetPingsCreator {

    companion object {
        const val GOOGLE_URL = "https://google.com"
    }

    private var handlerThread: HandlerThread? = null
    private var stoppableHandler: StoppableLoopedHandler? = null
    private val internetPingCallManager = InternetPingCallManager()

    fun pingUrlWithPeriod(url: String, periodMs: Long, onCallFinished: (result: String) -> Unit) {
        restartThreads()
        stoppableHandler?.executeInInfiniteLoop({
            internetPingCallManager.ping(url, CallbackWithAction(onCallFinished))
        }, periodMs)
    }

    fun startOneAfterAnotherPings(url: String, onCallFinished: (result: String) -> Unit) {
        restartThreads()
        innerStartOneAfterAnotherPings(url, onCallFinished)
    }

    private fun innerStartOneAfterAnotherPings(url: String, onCallFinished: (result: String) -> Unit) {
        val callCallback = CallbackWithAction { result ->
            onCallFinished(result)
            innerStartOneAfterAnotherPings(url, onCallFinished)
        }
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