package com.jj.androidenergyconsumer.data.rest

import com.jj.androidenergyconsumer.app.utils.logAndPingServer
import com.jj.androidenergyconsumer.domain.getDateStringWithMillis
import com.jj.androidenergyconsumer.domain.tag
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CallbackWithAction(private val onCallFinishedCallback: (result: String) -> Unit) : Callback<ResponseBody> {
    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
        val message = "${getDateStringWithMillis()}; onFailure, ${t.localizedMessage}"
        onCallFinished(message)
    }

    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
        val message = "${getDateStringWithMillis()}; onResponse, ${response.code()}"
        onCallFinished(message)
    }

    private fun onCallFinished(message: String) {
        logAndPingServer(message, tag)
        onCallFinishedCallback(message)
    }
}