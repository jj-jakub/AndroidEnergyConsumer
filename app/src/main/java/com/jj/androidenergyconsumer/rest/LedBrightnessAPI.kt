package com.jj.androidenergyconsumer.rest

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class BrightnessData(val brightness: Int)

interface LedBrightnessAPI {

    @POST("brightness")
    fun postBrightness(@Body brightnessData: BrightnessData): Call<ResponseBody>
}