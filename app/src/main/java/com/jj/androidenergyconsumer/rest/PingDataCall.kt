package com.jj.androidenergyconsumer.rest

import com.jj.universallprotocollibrary.PingData
import okhttp3.ResponseBody
import retrofit2.Callback

object PingDataCall {

    private val retrofitClient = RetrofitClientFactory().createRetrofit()

    private val PING_DATA_API: PingDataAPI = retrofitClient.create(PingDataAPI::class.java)

    fun postSensorsData(pingData: PingData, callback: Callback<ResponseBody>) =
        PING_DATA_API.postSensorsData(pingData).enqueue(callback)
}