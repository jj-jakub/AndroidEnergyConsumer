package com.jj.androidenergyconsumer.rest

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url

interface SampleInternetAPI {

    @GET
    fun ping(@Url url: String): Call<ResponseBody>
}