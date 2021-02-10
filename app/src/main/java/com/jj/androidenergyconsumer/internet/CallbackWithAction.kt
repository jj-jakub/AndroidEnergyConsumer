package com.jj.androidenergyconsumer.internet

import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CallbackWithAction(private val onCallFinishedCallback: (result: String) -> Unit) : Callback<ResponseBody> {
    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
        val message = "onFailure, ${t.localizedMessage}"
        onCallFinished(message)
    }

    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
        val message = "onResponse, ${response.code()}"
        onCallFinished(message)
    }

    private fun onCallFinished(message: String) {
        logAndPingServer(message, tag)
        onCallFinishedCallback(message)
    }
}