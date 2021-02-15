package com.jj.androidenergyconsumer.rest

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.jj.androidenergyconsumer.BuildConfig
import com.jj.androidenergyconsumer.FlipperLauncher
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

private val serverUrl = BuildConfig.SERVER_URL

class RetrofitClientFactory {

    fun createRetrofit(): Retrofit = Retrofit.Builder().baseUrl(serverUrl).client(createHttpClient())
        .addConverterFactory(JacksonConverterFactory.create(createMapper())).build()

    fun createRetrofitToUrl(url: String): Retrofit = Retrofit.Builder().baseUrl(url).client(createHttpClient())
        .addConverterFactory(JacksonConverterFactory.create(createMapper())).build()

    private fun createHttpClient() = OkHttpClient.Builder().apply {
        retryOnConnectionFailure(true)
        readTimeout(1, TimeUnit.MINUTES)
        connectTimeout(1, TimeUnit.MINUTES)
        FlipperLauncher.addFlipperNetworkInterceptor(this)
    }.build()

    private fun createMapper() = ObjectMapper().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}