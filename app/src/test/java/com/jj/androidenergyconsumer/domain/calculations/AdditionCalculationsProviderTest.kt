package com.jj.androidenergyconsumer.domain.calculations

import com.jj.androidenergyconsumer.TestCoroutineScopeProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.MockitoAnnotations

@ExperimentalCoroutinesApi
class AdditionCalculationsProviderTest {

    companion object {
        @JvmStatic
        fun factors(): List<Int> {
            val range = IntRange(-20, 20).toMutableList()
            range.remove(0)
            return range.toList()
        }
    }

    private val testCoroutineScopeProvider = TestCoroutineScopeProvider()
    private val mainTestScope = testCoroutineScopeProvider.getMain()

    private fun createAdditionCalculationsProvider(factor: Int): CalculationsProvider =
        AdditionCalculationsProvider(factor)

    @BeforeEach
    private fun setupVariables() { //NOSONAR
        MockitoAnnotations.openMocks(this)
    }

    @ParameterizedTest
    @MethodSource("factors")
    fun `calculationsTask should eventually emit calculations result if handler was not stopped`(factor: Int) =
        mainTestScope.runBlockingTest {
            val additionCalculationsProvider = createAdditionCalculationsProvider(factor)
            val resultsList = mutableListOf<CalculationsResult>()

            val collectingJob = launch {
                additionCalculationsProvider.observeCalculationsResult().toList(resultsList)
            }

            additionCalculationsProvider.startCalculationsTask(0)
            assertEquals(1, resultsList.size)
            collectingJob.cancel()
        }

    @ParameterizedTest
    @MethodSource("factors")
    fun `calculationsTask should not emit calculations result if it was stopped`(factor: Int) =
        mainTestScope.runBlockingTest {
            val additionCalculationsProvider = createAdditionCalculationsProvider(factor)
            additionCalculationsProvider.abortCalculationsTask()
            val resultsList = mutableListOf<CalculationsResult>()

            val collectingJob = launch {
                additionCalculationsProvider.observeCalculationsResult().toList(resultsList)
            }

            additionCalculationsProvider.startCalculationsTask(0)
            assertEquals(0, resultsList.size)
            collectingJob.cancel()
        }

    @Test
    fun `creating provider with factor equal to 0 should throw IllegalArgumentException`() {
        assertThrows(IllegalArgumentException::class.java) { AdditionCalculationsProvider(0) }
    }
}