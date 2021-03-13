package com.jj.androidenergyconsumer.rest

import okhttp3.ResponseBody
import retrofit2.Callback

class LedControlCallManager(url: String) {

    private val retrofitClient = RetrofitClientFactory().createRetrofitToUrl(url)

    private val ledControlAPI: LedControlAPI = retrofitClient.create(LedControlAPI::class.java)

    fun sendBrightness(brightness: Int, callback: Callback<ResponseBody>) =
        ledControlAPI.postBrightness(BrightnessData(brightness)).enqueue(callback)

    fun sendRainbowSpeed(rainbowSpeed: Int, callback: Callback<ResponseBody>) =
        ledControlAPI.postRainbowSpeed(RainbowSpeedData(rainbowSpeed)).enqueue(callback)
}