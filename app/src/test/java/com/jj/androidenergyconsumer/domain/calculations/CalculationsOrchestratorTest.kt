package com.jj.androidenergyconsumer.domain.calculations

import com.jj.androidenergyconsumer.TestCoroutineScopeProvider
import com.jj.androidenergyconsumer.app.handlers.HandlersOrchestrator
import com.jj.androidenergyconsumer.domain.coroutines.CoroutineJobContainer
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.capture
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class CalculationsOrchestratorTest {

    private lateinit var calculationsOrchestrator: CalculationsOrchestrator

    @Mock
    private lateinit var calculationsProviderFactory: CalculationsProviderFactory

    private val coroutineScopeProvider = TestCoroutineScopeProvider()

    @Mock
    private lateinit var coroutineJobContainerMock: CoroutineJobContainer

    @Mock
    private lateinit var handlersOrchestratorMock: HandlersOrchestrator

    @Captor
    private lateinit var calculationsTypeCaptor: ArgumentCaptor<CalculationsType>

    @Captor
    private lateinit var calculationsFactorCaptor: ArgumentCaptor<Int>

    @Captor
    private lateinit var handlersAmountCaptor: ArgumentCaptor<Int>

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        calculationsOrchestrator = createCalculationsOrchestrator()
    }

    @Test
    fun `starting new calculations should abort handlers`() {
        calculationsOrchestrator.startCalculations(CalculationsType.ADDITION, 1, 1)
        verify(handlersOrchestratorMock).abortThreads()
    }

    @Test
    fun `starting new calculations should abort collecting job`() {
        calculationsOrchestrator.startCalculations(CalculationsType.ADDITION, 1, 1)
        verify(coroutineJobContainerMock).cancelJob()
    }

    @Test
    fun `aborting calculations should abort handlers`() {
        calculationsOrchestrator.abortCalculations()
        verify(handlersOrchestratorMock).abortThreads()
    }

    @Test
    fun `aborting calculations should abort collecting job`() {
        calculationsOrchestrator.abortCalculations()
        verify(coroutineJobContainerMock).cancelJob()
    }

    @ParameterizedTest
    @MethodSource("calculationsTypes")
    fun `starting new calculations should create calculations provider with proper calculations type`(
            type: CalculationsType) {
        calculationsOrchestrator.startCalculations(type, 1, 10)

        verify(calculationsProviderFactory).createCalculationsProvider(capture(calculationsTypeCaptor), any())
        assertEquals(type, calculationsTypeCaptor.value)
    }

    @ParameterizedTest
    @MethodSource("calculationsFactors")
    fun `starting new calculations should create calculations provider with calculations factor`(factor: Int) {
        calculationsOrchestrator.startCalculations(CalculationsType.MULTIPLICATION, factor, 10)

        verify(calculationsProviderFactory).createCalculationsProvider(any(), capture(calculationsFactorCaptor))
        assertEquals(factor, calculationsFactorCaptor.value)
    }

    @Test
    fun `starting new calculations should launch handlers infinite loop`() {
        calculationsOrchestrator.startCalculations(CalculationsType.ADDITION, 1, 1)

        verify(handlersOrchestratorMock).launchInThreadsInInfiniteLoop(capture(handlersAmountCaptor), anyOrNull())
    }

    @ParameterizedTest
    @MethodSource("amountOfHandlers")
    fun `starting new calculations should launch handlers with proper amount parameter`(amount: Int) {
        calculationsOrchestrator.startCalculations(CalculationsType.ADDITION, 1, amount)

        verify(handlersOrchestratorMock).launchInThreadsInInfiniteLoop(capture(handlersAmountCaptor), anyOrNull())
        assertEquals(amount, handlersAmountCaptor.value)
    }

    @Test
    fun `starting new calculations should start observing calculations results`() {
        val calculationsProviderMock = mock(CalculationsProvider::class.java)
        whenever(calculationsProviderFactory.createCalculationsProvider(any(), any()))
            .thenReturn(calculationsProviderMock)

        calculationsOrchestrator.startCalculations(CalculationsType.ADDITION, 1, 1)

        verify(calculationsProviderMock).observeCalculationsResult()
    }

    private fun createCalculationsOrchestrator() = CalculationsOrchestrator(calculationsProviderFactory,
            coroutineScopeProvider, coroutineJobContainerMock, handlersOrchestratorMock)

    companion object {
        @JvmStatic
        private fun calculationsTypes() = CalculationsType.values().toList()

        @JvmStatic
        private fun calculationsFactors() = IntRange(1, 10)

        @JvmStatic
        private fun amountOfHandlers() = IntRange(1, 10)
    }
}