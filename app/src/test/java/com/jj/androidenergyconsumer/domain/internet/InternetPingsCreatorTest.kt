package com.jj.androidenergyconsumer.domain.internet

import com.jj.androidenergyconsumer.TestCoroutineScopeProvider
import com.jj.androidenergyconsumer.data.rest.InternetPingCallManager
import com.jj.androidenergyconsumer.data.rest.RetrofitClientFactory
import com.jj.androidenergyconsumer.data.rest.SampleInternetAPI
import com.jj.androidenergyconsumer.domain.coroutines.CoroutineJobContainerFactory
import com.jj.androidenergyconsumer.domain.multithreading.CoroutinesOrchestrator
import com.jj.androidenergyconsumer.domain.multithreading.ThreadsOrchestrator
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.ResponseBody
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

@ExperimentalCoroutinesApi
class InternetPingsCreatorTest {

    @Mock
    private lateinit var sampleInternetAPI: SampleInternetAPI

    @Mock
    private lateinit var retrofitClientFactory: RetrofitClientFactory

    @Mock
    private lateinit var retrofit: Retrofit

    @Mock
    private lateinit var call: Call<ResponseBody>

    @Mock
    private lateinit var response: Response<ResponseBody>

    @Captor
    private lateinit var callbackCaptor: ArgumentCaptor<Callback<ResponseBody>>

    private lateinit var testCoroutineScopeProvider: TestCoroutineScopeProvider

    private lateinit var internetPingsCreator: InternetPingsCreator

    private lateinit var coroutinesOrchestrator: ThreadsOrchestrator

    private lateinit var internetPingCallManagerSpy: InternetPingCallManager

    private val coroutineJobContainerFactory = CoroutineJobContainerFactory()

    private val url = "testurl"

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        testCoroutineScopeProvider = TestCoroutineScopeProvider()
        coroutinesOrchestrator = CoroutinesOrchestrator(testCoroutineScopeProvider, coroutineJobContainerFactory)
        whenever(sampleInternetAPI.ping(url)).thenReturn(call)
        whenever(retrofit.create(SampleInternetAPI::class.java)).thenReturn(sampleInternetAPI)
        whenever(retrofitClientFactory.createRetrofitForPings()).thenReturn(retrofit)

        internetPingCallManagerSpy = spy(InternetPingCallManager(retrofitClientFactory))
        internetPingsCreator = InternetPingsCreator(internetPingCallManagerSpy, testCoroutineScopeProvider,
                coroutinesOrchestrator)
    }

    @ParameterizedTest
    @MethodSource("positiveIntegersList")
    fun `startOneAfterAnotherPings should execute as many times as received responses`(responsesAmount: Int) {
        internetPingsCreator.startOneAfterAnotherPings(url)
        verify(internetPingCallManagerSpy).ping(eq(url), any())
        clearInvocations(internetPingCallManagerSpy)

        repeat(responsesAmount) {
            verify(call, times(it + 1)).enqueue(capture(callbackCaptor))
            callbackCaptor.lastValue.onResponse(call, response)
            verify(internetPingCallManagerSpy, times(it + 1)).ping(eq(url), any())
        }
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun positiveIntegersList() = IntRange(1, 20)
    }
}