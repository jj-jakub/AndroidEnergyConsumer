package com.jj.androidenergyconsumer.data.rest

import okhttp3.ResponseBody
import retrofit2.Callback

class InternetPingCallManager(retrofitClientFactory: RetrofitClientFactory) {

    private val sampleInternetAPI: SampleInternetAPI =
        retrofitClientFactory.createRetrofitForPings().create(SampleInternetAPI::class.java)

    fun ping(url: String, callback: Callback<ResponseBody>) = sampleInternetAPI.ping(url).enqueue(callback)
}