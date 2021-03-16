package com.jj.androidenergyconsumer.calculations

import com.jj.androidenergyconsumer.app.handlers.StoppableLoopedHandler
import com.jj.androidenergyconsumer.domain.calculations.AdditionCalculationsProvider
import com.jj.androidenergyconsumer.domain.calculations.CalculationsCallback
import com.jj.androidenergyconsumer.domain.calculations.CalculationsProvider
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

class AdditionCalculationsProviderTest {

    companion object {
        @JvmStatic
        fun factors(): List<Int> {
            val range = IntRange(-20, 20).toMutableList()
            range.remove(0)
            return range.toList()
        }
    }

    @Mock
    private lateinit var callbackMock: CalculationsCallback

    @Mock
    private lateinit var stoppableLoopedHandlerMock: StoppableLoopedHandler

    private fun createAdditionCalculationsProvider(callback: CalculationsCallback, factor: Int): CalculationsProvider =
        AdditionCalculationsProvider(callback, factor)

    @BeforeEach
    private fun setupVariables() { //NOSONAR
        MockitoAnnotations.openMocks(this)
    }

    @ParameterizedTest
    @MethodSource("factors")
    fun `calculationsTask should eventually call callback method if handler was not stopped`(factor: Int) {
        Mockito.`when`(stoppableLoopedHandlerMock.isHandlerStopped()).thenReturn(false)
        val additionCalculationsProvider = createAdditionCalculationsProvider(callbackMock, factor)

        additionCalculationsProvider.calculationsTask(0, stoppableLoopedHandlerMock)
        Mockito.verify(callbackMock).onThresholdAchieved(Mockito.anyInt(), Mockito.eq(0))
    }

    @ParameterizedTest
    @MethodSource("factors")
    fun `calculationsTask should not call callback method if handler was stopped`(factor: Int) {
        Mockito.`when`(stoppableLoopedHandlerMock.isHandlerStopped()).thenReturn(true)
        val additionCalculationsProvider = createAdditionCalculationsProvider(callbackMock, factor)

        additionCalculationsProvider.calculationsTask(0, stoppableLoopedHandlerMock)

        Mockito.verify(callbackMock, Mockito.never()).onThresholdAchieved(Mockito.anyInt(), Mockito.eq(0))
    }

    @Test
    fun `creating provider with factor equal to 0 should throw IllegalArgumentException`() {
        assertThrows(IllegalArgumentException::class.java) { AdditionCalculationsProvider(callbackMock, 0) }
    }
}