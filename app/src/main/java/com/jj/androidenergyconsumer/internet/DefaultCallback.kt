package com.jj.androidenergyconsumer.internet

import android.util.Log
import com.jj.androidenergyconsumer.utils.tag
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DefaultCallback : Callback<ResponseBody> {
    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
        Log.e(tag, "Sending units to server failed", t)
    }

    override fun onResponse(call: Call<ResponseBody>,
                            response: Response<ResponseBody>) {
        Log.d(tag, "Sending to server successful, response: $response")
    }
}