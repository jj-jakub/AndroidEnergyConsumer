package com.jj.androidenergyconsumer.fragments

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.closeSoftKeyboard
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.app.fragments.CalculationsFragment
import com.jj.androidenergyconsumer.app.services.CalculationsService.Companion.DEFAULT_CALCULATIONS_FACTOR
import com.jj.androidenergyconsumer.app.services.CalculationsService.Companion.DEFAULT_NUMBER_OF_THREADS
import com.jj.androidenergyconsumer.domain.calculations.CalculationsOrchestrator
import com.jj.androidenergyconsumer.domain.calculations.CalculationsResult
import com.jj.androidenergyconsumer.domain.calculations.CalculationsType
import com.jj.androidenergyconsumer.domain.coroutines.BufferedMutableSharedFlow
import com.jj.androidenergyconsumer.utils.performClick
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext.loadKoinModules
import org.koin.core.module.Module
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class CalculationsFragmentInstrumentedTest {

    @RelaxedMockK
    private lateinit var calculationsOrchestrator: CalculationsOrchestrator

    private lateinit var calculationsResultFlow: MutableSharedFlow<CalculationsResult>

    private lateinit var mockModule: Module

    @Before
    fun setup() {
        setupMocks()
        launchFragmentInContainer<CalculationsFragment>()
    }

    private fun setupMocks() {
        MockKAnnotations.init(this)
        calculationsResultFlow = BufferedMutableSharedFlow()
        every { calculationsOrchestrator.observeCalculationsResult() } returns calculationsResultFlow
        mockModule = module { single(override = true) { calculationsOrchestrator } }
        loadKoinModules(mockModule)
    }

    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.jj.androidenergyconsumer", appContext.packageName)
    }

    @Test
    fun checkIfAdditionCalculationsAreLaunchedWithProperParameters() {
        setupFactorAndHandlersInputs(2, 16)
        performClick(R.id.performAdditionsButton)

        verify { calculationsOrchestrator.startCalculations(CalculationsType.ADDITION, 2, 16) }
        performClick(R.id.abortCalculationsButton)
    }

    @Test
    fun checkIfMultiplicationCalculationsAreLaunchedWithProperParameters() {
        setupFactorAndHandlersInputs(4, 32)
        performClick(R.id.performMultiplicationsButton)

        verify { calculationsOrchestrator.startCalculations(CalculationsType.MULTIPLICATION, 4, 32) }
        performClick(R.id.abortCalculationsButton)
    }

    @Test
    fun checkIfAdditionCalculationsAreAbortedSuccessfully() {
        setupFactorAndHandlersInputs(4, 32)
        performClick(R.id.performAdditionsButton)
        performClick(R.id.abortCalculationsButton)

        verify { calculationsOrchestrator.abortCalculations() }
    }

    @Test
    fun checkIfMultiplicationCalculationsAreAbortedSuccessfully() {
        setupFactorAndHandlersInputs(4, 32)
        performClick(R.id.performMultiplicationsButton)
        performClick(R.id.abortCalculationsButton)

        verify { calculationsOrchestrator.abortCalculations() }
    }

    @Test
    fun checkIfAdditionCalculationsWillLaunchWithDefaultParametersIfInputsAreBlank() {
        onView(withId(R.id.calculationsFactorInput)).perform(typeText(""))
        onView(withId(R.id.calculationsThreadsNOInput)).perform(typeText(""), closeSoftKeyboard())
        performClick(R.id.performAdditionsButton)

        verify {
            calculationsOrchestrator.startCalculations(CalculationsType.ADDITION, DEFAULT_CALCULATIONS_FACTOR,
                    DEFAULT_NUMBER_OF_THREADS)
        }
        performClick(R.id.abortCalculationsButton)
    }

    @Test
    fun checkIfMultiplicationCalculationsWillLaunchWithDefaultParametersIfInputsAreBlank() {
        onView(withId(R.id.calculationsFactorInput)).perform(typeText(""))
        onView(withId(R.id.calculationsThreadsNOInput)).perform(typeText(""), closeSoftKeyboard())
        performClick(R.id.performMultiplicationsButton)

        verify {
            calculationsOrchestrator.startCalculations(CalculationsType.MULTIPLICATION, DEFAULT_CALCULATIONS_FACTOR,
                    DEFAULT_NUMBER_OF_THREADS)
        }
        performClick(R.id.abortCalculationsButton)
    }

    private fun setupFactorAndHandlersInputs(factor: Int, handlers: Int) {
        onView(withId(R.id.calculationsFactorInput)).perform(typeText(factor.toString()))
        onView(withId(R.id.calculationsThreadsNOInput)).perform(typeText(handlers.toString()), closeSoftKeyboard())
    }

    @After
    fun unloadKoin() {
//       unloadKoinModules(mockModule)
    }
}