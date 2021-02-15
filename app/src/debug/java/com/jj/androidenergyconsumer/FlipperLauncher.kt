package com.jj.androidenergyconsumer

import android.content.Context
import android.util.Log
import com.facebook.flipper.android.AndroidFlipperClient
import com.facebook.flipper.android.utils.FlipperUtils
import com.facebook.flipper.plugins.inspector.DescriptorMapping
import com.facebook.flipper.plugins.inspector.InspectorFlipperPlugin
import com.facebook.flipper.plugins.network.FlipperOkhttpInterceptor
import com.facebook.flipper.plugins.network.NetworkFlipperPlugin
import com.facebook.soloader.SoLoader
import com.jj.androidenergyconsumer.utils.tag
import okhttp3.OkHttpClient

object FlipperLauncher {

    private val networkFlipperPlugin = NetworkFlipperPlugin()

    fun enableFlipper(context: Context) {
        try {
            SoLoader.init(context, false)
            if (FlipperUtils.shouldEnableFlipper(context)) {
                val client = AndroidFlipperClient.getInstance(context)
                client.addPlugin(InspectorFlipperPlugin(context, DescriptorMapping.withDefaults()))
                client.addPlugin(networkFlipperPlugin)
                client.start()
            }
        } catch (e: NullPointerException) {
            Log.e(tag, "Flipper initialization failure")
        }
    }

    fun addFlipperNetworkInterceptor(builder: OkHttpClient.Builder) {
        builder.addInterceptor(FlipperOkhttpInterceptor(networkFlipperPlugin))
    }
}