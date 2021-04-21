package com.jj.androidenergyconsumer.app.utils

import android.content.Context
import android.location.LocationManager

class SystemServicesProvider {
    fun getLocationManager(context: Context) = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager?
}