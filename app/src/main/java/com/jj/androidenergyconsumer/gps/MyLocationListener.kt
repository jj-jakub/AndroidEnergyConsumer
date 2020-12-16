package com.jj.androidenergyconsumer.gps

import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import com.jj.androidenergyconsumer.notification.NotificationManager
import com.jj.androidenergyconsumer.utils.getDateStringWithMillis
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag

class MyLocationListener(private val notificationManager: NotificationManager) : LocationListener {

    override fun onLocationChanged(location: Location) {
        logAndPingServer("onLocationChanged, loc: lat: ${location.latitude} - lon: ${location.longitude}", tag)
        notificationManager.notifyGPSServiceNotification("GPSService notification",
                "${getDateStringWithMillis()} loc: lat: ${location.latitude} - lon: ${location.longitude}")
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        logAndPingServer("onStatusChanged, status: $status", tag)
    }

    override fun onProviderEnabled(provider: String) {
        logAndPingServer("onProviderEnabled, provider: $provider", tag)

    }

    override fun onProviderDisabled(provider: String) {
        logAndPingServer("onProviderDisabled, provider: $provider", tag)

    }
}