package com.jj.androidenergyconsumer.internet

import android.os.HandlerThread
import com.jj.androidenergyconsumer.handlers.StoppableLoopedHandler
import com.jj.androidenergyconsumer.rest.SampleInternetCallManager
import okhttp3.ResponseBody
import retrofit2.Callback

class InternetCallCreator(url: String) {

    companion object {
        const val GOOGLE_URL = "google.com"
    }

    private val handlerThread = HandlerThread("InternetThread")
    private val stoppableHandler = StoppableLoopedHandler(handlerThread.looper)
    private val sampleInternetCallManager = SampleInternetCallManager(url)

    fun pingGoogleWithPeriod(periodMs: Long) {
        stoppableHandler.executeInInfiniteLoop({
            sampleInternetCallManager.ping(DefaultCallback())
        }, periodMs)
    }

    fun ping(callback: Callback<ResponseBody> = DefaultCallback()) {
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