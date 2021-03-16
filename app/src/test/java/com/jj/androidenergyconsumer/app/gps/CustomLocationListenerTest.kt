package com.jj.androidenergyconsumer.app.gps

import android.location.Location
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito

@ExperimentalCoroutinesApi
class CustomLocationListenerTest {

    @Test
    fun `onLocationChanged should pass info by locationInfoUpdates`() = runBlockingTest {
        val customLocationListener = CustomLocationListener()
        val allLocationValues = mutableListOf<LocationListenerResult>()

        val collectingJob = launch {
            customLocationListener.observeLocationInfoUpdates().collect { allLocationValues.add(it) }
        }

        customLocationListener.onLocationChanged(Mockito.mock(Location::class.java))
        assertEquals(1, allLocationValues.size)
        collectingJob.cancel()
    }
}