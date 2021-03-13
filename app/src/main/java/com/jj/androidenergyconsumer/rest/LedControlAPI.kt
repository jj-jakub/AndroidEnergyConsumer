package com.jj.androidenergyconsumer.rest

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class BrightnessData(val brightness: Int)
data class RainbowSpeedData(val rainbowSpeed: Int)

interface LedControlAPI {

    @POST("brightness")
    fun postBrightness(@Body brightnessData: BrightnessData): Call<ResponseBody>

    @POST("rainbowSpeed")
    fun postRainbowSpeed(@Body rainbowSpeedData: RainbowSpeedData): Call<ResponseBody>
}