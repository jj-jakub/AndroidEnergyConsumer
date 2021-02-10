package com.jj.androidenergyconsumer.gps

import android.location.Location
import com.jj.androidenergyconsumer.notification.CustomNotification
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class MyLocationListenerTest {

    @Test
    fun `onLocationChanged should notify notification`() {
        val notificationMock = Mockito.mock(CustomNotification::class.java)

        MyLocationListener(notificationMock).onLocationChanged(Mockito.mock(Location::class.java))
        Mockito.verify(notificationMock).notify(Mockito.anyString(), Mockito.anyString(), Mockito.anyString())
    }
}