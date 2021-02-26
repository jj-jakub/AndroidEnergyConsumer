package com.jj.androidenergyconsumer.gps

import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import com.jj.androidenergyconsumer.utils.BufferedMutableSharedFlow
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag
import kotlinx.coroutines.flow.SharedFlow

sealed class LocationListenerResult {
    data class LocationChanged(val location: Location) : LocationListenerResult()
    data class OnStatusChanged(val provider: String?, val status: Int, val extras: Bundle?) : LocationListenerResult()
    data class OnProviderEnabled(val provider: String) : LocationListenerResult()
    data class OnProviderDisabled(val provider: String) : LocationListenerResult()
}

class CustomLocationListener : LocationListener {

    private val locationInfoUpdates = BufferedMutableSharedFlow<LocationListenerResult>()

    fun observeLocationInfoUpdates(): SharedFlow<LocationListenerResult> = locationInfoUpdates

    override fun onLocationChanged(location: Location) {
        logAndPingServer("onLocationChanged, loc: lat: ${location.latitude} - lon: ${location.longitude}", tag)
        locationInfoUpdates.tryEmit(LocationListenerResult.LocationChanged(location))
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        logAndPingServer("onStatusChanged, status: $status", tag)
        locationInfoUpdates.tryEmit(LocationListenerResult.OnStatusChanged(provider, status, extras))
    }

    override fun onProviderEnabled(provider: String) {
        logAndPingServer("onProviderEnabled, provider: $provider", tag)
        locationInfoUpdates.tryEmit(LocationListenerResult.OnProviderEnabled(provider))
    }

    override fun onProviderDisabled(provider: String) {
        logAndPingServer("onProviderDisabled, provider: $provider", tag)
        locationInfoUpdates.tryEmit(LocationListenerResult.OnProviderDisabled(provider))
    }
}