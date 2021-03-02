package com.jj.androidenergyconsumer.rest

import okhttp3.ResponseBody
import retrofit2.Callback

class LedBrightnessCallManager(url: String) {

    private val retrofitClient = RetrofitClientFactory().createRetrofitToUrl(url)

    private val ledBrightnessAPI: LedBrightnessAPI = retrofitClient.create(LedBrightnessAPI::class.java)

    fun sendBrightness(brightness: Int, callback: Callback<ResponseBody>) =
        ledBrightnessAPI.postBrightness(BrightnessData(brightness)).enqueue(callback)
}