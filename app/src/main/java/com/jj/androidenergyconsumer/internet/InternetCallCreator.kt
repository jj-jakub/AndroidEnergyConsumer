package com.jj.androidenergyconsumer.internet

import android.os.HandlerThread
import com.jj.androidenergyconsumer.handlers.StoppableLoopedHandler
import com.jj.androidenergyconsumer.rest.SampleInternetCallManager
import com.jj.androidenergyconsumer.services.InternetService
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class InternetCallCreator(url: String) {

    companion object {
        const val GOOGLE_URL = "https://google.com"
    }

    private val handlerThread: HandlerThread = HandlerThread("InternetThread")
    private val stoppableHandler: StoppableLoopedHandler
    private val sampleInternetCallManager = SampleInternetCallManager(url)

    init {
        handlerThread.start()
        stoppableHandler = StoppableLoopedHandler(handlerThread.looper)
    }

    fun pingGoogleWithPeriod(periodMs: Long) {
        stoppableHandler.executeInInfiniteLoop({
            sampleInternetCallManager.ping(DefaultCallback())
        }, periodMs)
    }

    fun startOneAfterAnotherPings(onCallFinished: (result: String) -> Unit) {
        val callCallback = getLoopCallCallback(onCallFinished)
        ping(callCallback)
    }

    private fun ping(callback: Callback<ResponseBody> = DefaultCallback()) {
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

    private fun getLoopCallCallback(onCallFinished: (result: String) -> Unit): Callback<ResponseBody> =
        object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                logAndPingServer("onFailure", InternetService.tag)
                onCallFinished("onFailure")
                startOneAfterAnotherPings(onCallFinished)
            }

            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                logAndPingServer("onResponse", InternetService.tag)
                onCallFinished("onResponse")
                startOneAfterAnotherPings(onCallFinished)
            }
        }
}