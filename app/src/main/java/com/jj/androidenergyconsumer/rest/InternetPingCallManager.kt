package com.jj.androidenergyconsumer.rest

import okhttp3.ResponseBody
import retrofit2.Callback

class InternetPingCallManager(private val url: String) {

    private val retrofitClient = RetrofitClientFactory().createRetrofitForPings()

    private val sampleInternetAPI: SampleInternetAPI = retrofitClient.create(SampleInternetAPI::class.java)

    fun ping(callback: Callback<ResponseBody>) = sampleInternetAPI.ping(url).enqueue(callback)
}