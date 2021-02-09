package com.jj.androidenergyconsumer.calculations

import com.jj.androidenergyconsumer.handlers.StoppableLoopedHandler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class MultiplicationCalculationsProviderTest {

    companion object {
        @JvmStatic
        fun factors() = IntRange(0, 20).toList()
    }

    @Mock
    private lateinit var callbackMock: CalculationsCallback

    @Mock
    private lateinit var stoppableLoopedHandlerMock: StoppableLoopedHandler

    private fun createMultiplicationCalculationsProvider(callback: CalculationsCallback, factor: Int)
            : CalculationsProvider = MultiplicationCalculationsProvider(callback, factor)

    @BeforeEach
    private fun setupVariables() { //NOSONAR
        MockitoAnnotations.openMocks(this)
    }

    @ParameterizedTest
    @MethodSource("factors")
    fun `calculationsTask should eventually call callback method if handler was not stopped`(factor: Int) {
        Mockito.`when`(stoppableLoopedHandlerMock.isHandlerStopped()).thenReturn(false)
        val multiplicationCalculationsProvider = createMultiplicationCalculationsProvider(callbackMock, factor)

        multiplicationCalculationsProvider.calculationsTask(0, stoppableLoopedHandlerMock)
        Mockito.verify(callbackMock).onThresholdAchieved(Mockito.anyInt(), Mockito.eq(0))
    }

    @ParameterizedTest
    @MethodSource("factors")
    fun `calculationsTask should not call callback method if handler was stopped`(factor: Int) {
        Mockito.`when`(stoppableLoopedHandlerMock.isHandlerStopped()).thenReturn(true)
        val multiplicationCalculationsProvider = createMultiplicationCalculationsProvider(callbackMock, factor)

        multiplicationCalculationsProvider.calculationsTask(0, stoppableLoopedHandlerMock)

        Mockito.verify(callbackMock, Mockito.never()).onThresholdAchieved(Mockito.anyInt(), Mockito.eq(0))
    }
}