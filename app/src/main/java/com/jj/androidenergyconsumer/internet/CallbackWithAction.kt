package com.jj.androidenergyconsumer.internet

import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CallbackWithAction(private val onCallFinished: (result: String) -> Unit) : Callback<ResponseBody> {
    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
        logAndPingServer("onFailure", tag)
        onCallFinished("onFailure")
    }

    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
        logAndPingServer("onResponse", tag)
        onCallFinished("onResponse")
    }
}