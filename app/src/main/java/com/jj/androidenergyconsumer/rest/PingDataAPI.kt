package com.jj.androidenergyconsumer.rest

import com.jj.universallprotocollibrary.PingData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface PingDataAPI {

    @POST("sensors/pingdata")
    fun postSensorsData(@Body pingData: PingData): Call<ResponseBody>
}