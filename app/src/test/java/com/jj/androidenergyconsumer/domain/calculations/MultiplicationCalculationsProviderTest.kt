package com.jj.androidenergyconsumer.domain.calculations

import com.jj.androidenergyconsumer.TestCoroutineScopeProvider
import com.jj.androidenergyconsumer.app.handlers.StoppableLoopedHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class MultiplicationCalculationsProviderTest {

    companion object {
        @JvmStatic
        fun factors(): List<Int> {
            val range = IntRange(-20, 20).toMutableList()
            range.remove(-1)
            range.remove(0)
            range.remove(1)
            return range.toList()
        }

        @JvmStatic
        fun illegalFactors() = listOf(-1, 0, 1)
    }

    @Mock
    private lateinit var stoppableLoopedHandlerMock: StoppableLoopedHandler

    private val testCoroutineScopeProvider = TestCoroutineScopeProvider()
    private val mainTestScope = testCoroutineScopeProvider.getMain()

    private fun createMultiplicationCalculationsProvider(factor: Int): CalculationsProvider =
        MultiplicationCalculationsProvider(factor)

    @BeforeEach
    private fun setupVariables() { //NOSONAR
        MockitoAnnotations.openMocks(this)
    }

    @ParameterizedTest
    @MethodSource("factors")
    fun `calculationsTask should eventually emit calculations result if handler was not stopped`(factor: Int) =
        mainTestScope.runBlockingTest {
            Mockito.`when`(stoppableLoopedHandlerMock.isHandlerStopped()).thenReturn(false)
            val multiplicationCalculationsProvider = createMultiplicationCalculationsProvider(factor)
            val resultsList = mutableListOf<CalculationsResult>()

            val collectingJob = launch {
                multiplicationCalculationsProvider.observeCalculationsResult().toList(resultsList)
            }

            multiplicationCalculationsProvider.calculationsTask(0, stoppableLoopedHandlerMock)
            assertEquals(1, resultsList.size)
            collectingJob.cancel()
        }

    @ParameterizedTest
    @MethodSource("factors")
    fun `calculationsTask should not emit calculations result if handler was stopped`(factor: Int) =
        mainTestScope.runBlockingTest {
            Mockito.`when`(stoppableLoopedHandlerMock.isHandlerStopped()).thenReturn(true)
            val multiplicationCalculationsProvider = createMultiplicationCalculationsProvider(factor)
            val resultsList = mutableListOf<CalculationsResult>()

            val collectingJob = launch {
                multiplicationCalculationsProvider.observeCalculationsResult().toList(resultsList)
            }

            multiplicationCalculationsProvider.calculationsTask(0, stoppableLoopedHandlerMock)
            assertEquals(0, resultsList.size)
            collectingJob.cancel()
        }

    @ParameterizedTest
    @MethodSource("illegalFactors")
    fun `creating provider with one of illegal factors should throw IllegalArgumentException`(factor: Int) {
        Assertions.assertThrows(IllegalArgumentException::class.java) { MultiplicationCalculationsProvider(factor) }
    }
}