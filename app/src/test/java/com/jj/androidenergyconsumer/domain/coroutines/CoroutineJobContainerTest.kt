package com.jj.androidenergyconsumer.domain.coroutines

import kotlinx.coroutines.Job
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

class CoroutineJobContainerTest {

    private lateinit var coroutineJobContainer: CoroutineJobContainer

    @BeforeEach
    fun setup() {
        coroutineJobContainer = CoroutineJobContainer()
    }

    @Test
    fun `cancelJob should call cancel on previously set job`() {
        val jobMock = mock(Job::class.java)

        coroutineJobContainer.setCurrentJob(jobMock)
        coroutineJobContainer.cancelJob()

        verify(jobMock).cancel()
    }

    @Test
    fun `cancelJob should call cancel on latest set current job`() {
        val jobMock = mock(Job::class.java)
        val secondJobMock = mock(Job::class.java)

        coroutineJobContainer.setCurrentJob(jobMock)
        coroutineJobContainer.setCurrentJob(secondJobMock)
        coroutineJobContainer.cancelJob()

        verify(jobMock, never()).cancel()
        verify(secondJobMock).cancel()
    }
}