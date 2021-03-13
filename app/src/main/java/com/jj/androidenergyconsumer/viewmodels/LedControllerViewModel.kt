package com.jj.androidenergyconsumer.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.jj.androidenergyconsumer.fragments.AvailableLedColors
import com.jj.androidenergyconsumer.rest.InternetPingCallManager
import com.jj.androidenergyconsumer.rest.LedControlCallManager
import com.jj.androidenergyconsumer.utils.BufferedMutableSharedFlow
import com.jj.androidenergyconsumer.utils.tag
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class LedControllerViewModel(private val internetPingCallManager: InternetPingCallManager) : ViewModel() {

    private val errorMessage: MutableSharedFlow<String> = BufferedMutableSharedFlow()

    private val callCallback = object : Callback<ResponseBody> {
        override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
            Log.d(tag, "LedController call onResponse, code: ${response.code()}, ${call.request().url()}")
        }

        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
            errorMessage.tryEmit(t.message ?: "Call Error")
        }
    }

    fun observeErrorMessage(): SharedFlow<String> = errorMessage

    fun sendLedRequest(color: AvailableLedColors, ip: String) {
        val url = "http://$ip/"
        val urlWithEndpoint = "$url${color.toString().toLowerCase(Locale.ROOT)}"
        internetPingCallManager.ping(urlWithEndpoint, callCallback)
    }

    fun sendBrightnessRequest(brightness: Int, ip: String) {
        val url = "http://$ip"
        createLedControlCallManager(url)?.sendBrightness(brightness, callCallback)
    }

    fun sendRainbowSpeedRequest(rainbowSpeed: Int, ip: String) {
        val url = "http://$ip"
        createLedControlCallManager(url)?.sendRainbowSpeed(rainbowSpeed, callCallback)
    }

    private fun createLedControlCallManager(url: String): LedControlCallManager? = try {
        LedControlCallManager(url)
    } catch (iae: IllegalArgumentException) {
        Log.e(tag, "Exception while creating LedControlCallManager", iae)
        errorMessage.tryEmit(iae.message ?: "URL Error")
        null
    }
}