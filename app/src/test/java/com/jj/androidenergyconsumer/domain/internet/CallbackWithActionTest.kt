package com.jj.androidenergyconsumer.domain.internet

import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import retrofit2.Call
import retrofit2.Response

class CallbackWithActionTest {

    @Test
    @Suppress("unchecked_cast")
    fun `onFailure should call callback`() {
        var callbackInvocations = 0
        val callback: (result: String) -> Unit = {
            callbackInvocations++
        }

        CallbackWithAction(callback).onFailure(Mockito.mock(Call::class.java) as Call<ResponseBody>,
                Throwable("Failure"))
        assertEquals(1, callbackInvocations)
    }

    @Test
    @Suppress("unchecked_cast")
    fun `onResponse should call callback once`() {
        var callbackInvocations = 0
        val callback: (result: String) -> Unit = {
            callbackInvocations++
        }

        CallbackWithAction(callback).onResponse(Mockito.mock(Call::class.java) as Call<ResponseBody>,
                Mockito.mock(Response::class.java) as Response<ResponseBody>)
        assertEquals(1, callbackInvocations)
    }
}