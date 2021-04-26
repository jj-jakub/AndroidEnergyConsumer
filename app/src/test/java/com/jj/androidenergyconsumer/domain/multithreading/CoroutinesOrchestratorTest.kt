package com.jj.androidenergyconsumer.domain.multithreading

import com.jj.androidenergyconsumer.TestCoroutineScopeProvider
import com.jj.androidenergyconsumer.domain.coroutines.CoroutineJobContainer
import com.jj.androidenergyconsumer.domain.coroutines.CoroutineJobContainerFactory
import com.jj.androidenergyconsumer.domain.coroutines.CoroutineScopeProvider
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class CoroutinesOrchestratorTest {

    private lateinit var testCoroutineScopeProvider: TestCoroutineScopeProvider

    private lateinit var coroutinesOrchestrator: ThreadsOrchestrator

    @Mock
    private lateinit var coroutineJobContainerFactory: CoroutineJobContainerFactory

    private val coroutineJobContainers = mutableListOf<CoroutineJobContainer>()

    @Mock
    private lateinit var task: (index: Int) -> Unit

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        whenever(task.invoke(any())).thenReturn(Unit)
        whenever(coroutineJobContainerFactory.create()).thenAnswer {
            spy(CoroutineJobContainer()).also { container -> coroutineJobContainers.add(container) }
        }
        testCoroutineScopeProvider = TestCoroutineScopeProvider()
        coroutinesOrchestrator = CoroutinesOrchestrator(testCoroutineScopeProvider, coroutineJobContainerFactory)
    }

    @ParameterizedTest
    @MethodSource("nonNegativeIntegersList")
    fun `launchOnceInThreads should execute task as many times as there are threads`(threadsAmount: Int) {
        coroutinesOrchestrator.launchOnceInThreads(threadsAmount, task)

        verify(task, times(threadsAmount)).invoke(any()) // Check how many times task was executed
        repeat(threadsAmount) {
            verify(task).invoke(threadsAmount - 1) // Check if executed only once per thread
        }
    }

    @Test
    fun `running jobs should be cancelled if launchOnceInThreads was executed twice`() {
        val threadsAmount = 10
        coroutinesOrchestrator.launchOnceInThreads(threadsAmount, task)
        val currentlyRunningContainers = coroutineJobContainers.toList()

        coroutinesOrchestrator.launchOnceInThreads(threadsAmount, task)

        currentlyRunningContainers.forEach { container -> verify(container).cancelJob() }
    }

    @Test
    fun `newly created jobs should not be cancelled if launchOnceInThreads was executed twice`() {
        val threadsAmount = 20
        coroutinesOrchestrator.launchOnceInThreads(threadsAmount, task)
        coroutineJobContainers.clear()
        coroutinesOrchestrator.launchOnceInThreads(threadsAmount, task)

        coroutineJobContainers.toList().forEach { container -> verify(container, never()).cancelJob() }
    }


    @Test
    fun `calling abortThreads after launchOnceInThreads should result in cancelling jobs`() {
        val threadsAmount = 32
        coroutinesOrchestrator.launchOnceInThreads(threadsAmount, task)
        coroutinesOrchestrator.abortThreads()

        coroutineJobContainers.toList().forEach { container -> verify(container).cancelJob() }
    }

    @Test
    fun `running jobs should be cancelled if launchInThreadsInInfiniteLoop was executed twice`() {
        // TODO fix, Inserting testCoroutineScopeProvider to constructor will eventually cause test to block
        coroutinesOrchestrator = CoroutinesOrchestrator(CoroutineScopeProvider(), coroutineJobContainerFactory)
        val threadsAmount = 10

        coroutinesOrchestrator.launchInThreadsInInfiniteLoop(threadsAmount, task)
        val currentlyRunningContainers = coroutineJobContainers.toList()
        assertEquals(threadsAmount, currentlyRunningContainers.size)

        coroutinesOrchestrator.launchInThreadsInInfiniteLoop(threadsAmount, task)

        currentlyRunningContainers.forEach { container -> verify(container).cancelJob() }
    }

    @Test
    fun `newly created jobs should not be cancelled if launchInThreadsInInfiniteLoop was executed twice`() {
        coroutinesOrchestrator = CoroutinesOrchestrator(CoroutineScopeProvider(), coroutineJobContainerFactory)
        val threadsAmount = 20

        coroutinesOrchestrator.launchInThreadsInInfiniteLoop(threadsAmount, task)
        coroutineJobContainers.clear()
        coroutinesOrchestrator.launchInThreadsInInfiniteLoop(threadsAmount, task)

        coroutineJobContainers.toList().forEach { container -> verify(container, never()).cancelJob() }
    }


    @Test
    fun `calling abortThreads after launchInThreadsInInfiniteLoop should result in cancelling jobs`() {
        coroutinesOrchestrator = CoroutinesOrchestrator(CoroutineScopeProvider(), coroutineJobContainerFactory)
        val threadsAmount = 32

        coroutinesOrchestrator.launchInThreadsInInfiniteLoop(threadsAmount, task)
        coroutinesOrchestrator.abortThreads()

        coroutineJobContainers.toList().forEach { container -> verify(container).cancelJob() }
    }

    companion object {
        @Suppress("unused")
        @JvmStatic
        fun nonNegativeIntegersList() = IntRange(0, 20)
    }
}