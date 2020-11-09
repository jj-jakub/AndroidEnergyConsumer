package com.jj.androidenergyconsumer.services

import android.app.Service
import android.content.Context
import android.content.Intent
import com.jj.androidenergyconsumer.utils.startService

interface ServiceStarter {

    fun start(context: Context, action: String? = null) =
        startService(context, getServiceIntent(context).also { intent -> action?.run { intent.action = action } })

    fun getServiceIntent(context: Context): Intent = Intent(context, getServiceClass())

    fun getServiceClass(): Class<out Service>
}