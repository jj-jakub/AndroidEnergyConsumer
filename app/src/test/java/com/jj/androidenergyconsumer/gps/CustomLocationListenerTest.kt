package com.jj.androidenergyconsumer.gps

import android.location.Location
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class CustomLocationListenerTest {

    // TODO Fix test
    @Disabled("Fix SharedFlow tests")
    @Test
    fun `onLocationChanged should pass info by locationInfoUpdates`() = runBlocking {
        val customLocationListener = CustomLocationListener()
        val allLocationValues = mutableListOf<LocationListenerResult>()

        val collectJob = launch {
            customLocationListener.observeLocationInfoUpdates().collect {
                println("Emitting")
                allLocationValues.add(it)
            }
        }

        customLocationListener.onLocationChanged(Mockito.mock(Location::class.java))
        assertEquals(1, allLocationValues.size)
        collectJob.cancel()
    }
}