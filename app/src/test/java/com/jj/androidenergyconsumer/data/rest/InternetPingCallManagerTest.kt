package com.jj.androidenergyconsumer.data.rest

import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import okhttp3.ResponseBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Retrofit

class InternetPingCallManagerTest {

    @Mock
    private lateinit var retrofitClientFactory: RetrofitClientFactory

    @Mock
    private lateinit var retrofitClient: Retrofit

    @Mock
    private lateinit var sampleInternetAPI: SampleInternetAPI

    @Mock
    private lateinit var call: Call<ResponseBody>

    @Mock
    private lateinit var callback: Callback<ResponseBody>

    private lateinit var internetPingCallManager: InternetPingCallManager

    private val testUrl = "testUrl"

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)

        whenever(sampleInternetAPI.ping(testUrl)).thenReturn(call)
        whenever(retrofitClient.create(SampleInternetAPI::class.java)).thenReturn(sampleInternetAPI)
        whenever(retrofitClientFactory.createRetrofitForPings()).thenReturn(retrofitClient)
        internetPingCallManager = InternetPingCallManager(retrofitClientFactory)
    }

    @Test
    fun `calling ping method should call ping with proper url on API`() {
        internetPingCallManager.ping(testUrl, callback)
        verify(sampleInternetAPI).ping(testUrl)
    }

    @Test
    fun `calling ping method should enqueue callback on call from ping`() {
        internetPingCallManager.ping(testUrl, callback)
        verify(call).enqueue(callback)
    }
}