package com.jj.androidenergyconsumer.domain.internet

import com.jj.androidenergyconsumer.app.handlers.StoppableLoopedThread
import com.jj.androidenergyconsumer.data.rest.InternetPingCallManager
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.mockito.Mockito

class InternetPingsCreatorTest {

    @Disabled
    @Test
    fun `startOneAfterAnotherPings should execute as many times as received responses`() {
        val internetPingCallManager = Mockito.mock(InternetPingCallManager::class.java)
        val stoppableLoopedThread = Mockito.mock(StoppableLoopedThread::class.java)
//        val captor = ArgumentCaptor<>

//        whenever(stoppableLoopedThread.post())
    }
}