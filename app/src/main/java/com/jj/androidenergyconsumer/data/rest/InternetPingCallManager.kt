package com.jj.androidenergyconsumer.data.rest

import okhttp3.ResponseBody
import retrofit2.Callback

class InternetPingCallManager {

    private val retrofitClient = RetrofitClientFactory().createRetrofitForPings()

    private val sampleInternetAPI: SampleInternetAPI = retrofitClient.create(SampleInternetAPI::class.java)

    fun ping(url: String, callback: Callback<ResponseBody>) = sampleInternetAPI.ping(url).enqueue(callback)
}