package com.jj.androidenergyconsumer.internet

import android.os.HandlerThread
import com.jj.androidenergyconsumer.handlers.StoppableLoopedHandler
import com.jj.androidenergyconsumer.rest.InternetPingCallManager
import okhttp3.ResponseBody
import retrofit2.Callback

class InternetCallCreator(url: String) {

    companion object {
        const val GOOGLE_URL = "https://google.com"
    }

    private val handlerThread: HandlerThread = HandlerThread("InternetThread")
    private val stoppableHandler: StoppableLoopedHandler
    private val sampleInternetCallManager = InternetPingCallManager(url)

    init {
        handlerThread.start()
        stoppableHandler = StoppableLoopedHandler(handlerThread.looper)
    }

    fun pingGoogleWithPeriod(periodMs: Long, onCallFinished: (result: String) -> Unit) {
        stoppableHandler.executeInInfiniteLoop({
            sampleInternetCallManager.ping(CallbackWithAction(onCallFinished))
        }, periodMs)
    }

    fun startOneAfterAnotherPings(onCallFinished: (result: String) -> Unit) {
        val callCallback = CallbackWithAction { result ->
            onCallFinished(result)
            startOneAfterAnotherPings(onCallFinished)
        }
        ping(callCallback)
    }

    private fun ping(callback: Callback<ResponseBody>) {
        stoppableHandler.post {
            sampleInternetCallManager.ping(callback)
        }
    }

    fun stopWorking() {
        stoppableHandler.quitHandler()
        handlerThread.apply {
            handlerThread.looper.quit()
            handlerThread.quit()
        }
    }
}