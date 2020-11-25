package com.jj.androidenergyconsumer.gps

import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import com.jj.androidenergyconsumer.notification.NotificationManagerBuilder
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag
import java.util.*

class MyLocationListener(private val notificationManagerBuilder: NotificationManagerBuilder) : LocationListener {

    override fun onLocationChanged(location: Location) {
        logAndPingServer("onStatusChanged, loc: lat: ${location.latitude} - lon: ${location.longitude}", tag)
        notificationManagerBuilder.notifyServiceNotification("GPSService notification",
                "${Date()} loc: lat: ${location.latitude} - lon: ${location.longitude}")
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