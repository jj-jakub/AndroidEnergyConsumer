package com.jj.androidenergyconsumer.rest

import okhttp3.ResponseBody
import retrofit2.Callback

class SampleInternetCallManager(private val url: String) {

    private val retrofitClient = RetrofitClientFactory().createRetrofitToUrl(url)

    private val sampleInternetAPI: SampleInternetAPI = retrofitClient.create(SampleInternetAPI::class.java)

    fun ping(callback: Callback<ResponseBody>) = sampleInternetAPI.ping(url).enqueue(callback)
}