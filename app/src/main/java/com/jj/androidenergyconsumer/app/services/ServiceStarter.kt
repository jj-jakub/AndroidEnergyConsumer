package com.jj.androidenergyconsumer.app.services

import android.app.Service
import android.content.Context
import android.content.Intent
import com.jj.androidenergyconsumer.app.utils.isAndroid8OrHigher

interface ServiceStarter {

    fun startService(context: Context, intent: Intent) {
        when {
            isAndroid8OrHigher() -> context.startForegroundService(intent)
            else -> context.startService(intent)
        }
    }

    fun start(context: Context, action: String? = null) =
        startService(context, getServiceIntent(context).also { intent -> action?.run { intent.action = action } })

    fun start(context: Context, intent: Intent) = startService(context, intent)

    fun getServiceIntent(context: Context): Intent = Intent(context, getServiceClass())

    fun getServiceClass(): Class<out Service>
}