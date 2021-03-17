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
        verify(handlersOrchestratorMock).abortHandlers()
    }

    @Test
    fun `starting new calculations should abort collecting job`() {
        calculationsOrchestrator.startCalculations(CalculationsType.ADDITION, 1, 1)
        verify(coroutineJobContainerMock).cancelJob()
    }

    @Test
    fun `aborting calculations should abort handlers`() {
        calculationsOrchestrator.abortCalculations()
        verify(handlersOrchestratorMock).abortHandlers()
    }

    @Test
    fun `aborting calculations should abort collecting job`() {
        calculationsOrchestrator.abortCalculations()
        verify(coroutineJobContainerMock).cancelJob()
    }

    //TODO parameterized, split assertions
    @Test
    fun `starting new calculations should create calculations provider with proper parameters`() {
        val calculationsType = CalculationsType.MULTIPLICATION
        val calculationsFactor = 5

        calculationsOrchestrator.startCalculations(calculationsType, calculationsFactor, 10)

        verify(calculationsProviderFactory).createCalculationsProvider(capture(calculationsTypeCaptor),
                capture(calculationsFactorCaptor))
        assertEquals(calculationsType, calculationsTypeCaptor.value)
        assertEquals(calculationsFactor, calculationsFactorCaptor.value)
    }

    //TODO Split assertions
    @Test
    fun `starting new calculations should launch handlers infinite loop`() {
        val handlersAmount = 10

        calculationsOrchestrator.startCalculations(CalculationsType.ADDITION, 1, handlersAmount)

        verify(handlersOrchestratorMock).launchInEveryHandlerInInfiniteLoop(capture(handlersAmountCaptor), anyOrNull())
        assertEquals(handlersAmount, handlersAmountCaptor.value)
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
}