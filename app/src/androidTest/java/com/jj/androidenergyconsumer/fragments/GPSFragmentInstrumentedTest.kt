package com.jj.androidenergyconsumer.fragments

import android.location.LocationManager
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.app.fragments.GPSLauncherFragment
import com.jj.androidenergyconsumer.app.gps.CustomLocationListener
import com.jj.androidenergyconsumer.app.permissions.PermissionManager
import com.jj.androidenergyconsumer.app.utils.SystemServicesProvider
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.koin.core.module.Module
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class GPSFragmentInstrumentedTest {

    @RelaxedMockK
    private lateinit var permissionManager: PermissionManager

    @RelaxedMockK
    private lateinit var systemServicesProvider: SystemServicesProvider

    @RelaxedMockK
    private lateinit var locationManager: LocationManager

    private lateinit var mockModule: Module

    @Before
    fun setup() {
        setupMocks()
        launchFragmentInContainer<GPSLauncherFragment>()
    }

    private fun setupMocks() {
        MockKAnnotations.init(this)
        every { permissionManager.areLocationsPermissionGranted(any()) } returns true
        every { systemServicesProvider.getLocationManager(any()) } returns locationManager
        mockModule = module {
            single(override = true) { permissionManager }
            single(override = true) { systemServicesProvider }
        }
        GlobalContext.loadKoinModules(mockModule)
    }

    @Test
    fun requestConstantUpdatesShouldRequestLocationUpdatesWithProperParameters() {
        performClick(R.id.constantGPSWorkButton)

        verify {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0F,
                    ofType(CustomLocationListener::class))
        }

        // TODO In future launch every test in new environment instead of closing service by clicking stop
        performClick(R.id.stopGpsUpdatesButton)
    }

    @Test
    fun requestConstantUpdatesShouldNotGetRequestIntervalParameter() {
        val requestInterval = 256L
        setupRequestIntervalInput(requestInterval)
        performClick(R.id.constantGPSWorkButton)

        verify {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0F,
                    ofType(CustomLocationListener::class))
        }
        performClick(R.id.stopGpsUpdatesButton)
    }

    @Test
    fun requestPeriodicUpdatesShouldRequestLocationUpdatesWithProperParameters() {
        val requestInterval = 512L
        setupRequestIntervalInput(requestInterval)
        performClick(R.id.periodicGPSWorkButton)

        verify {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, requestInterval, 0F,
                    ofType(CustomLocationListener::class))
        }
        performClick(R.id.stopGpsUpdatesButton)
    }

    @Test
    fun requestPeriodicUpdatesShouldRequestLocationUpdatesWithDefaultParametersIfInputIsBlank() {
        performClick(R.id.periodicGPSWorkButton)

        verify {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0F,
                    ofType(CustomLocationListener::class))
        }
        performClick(R.id.stopGpsUpdatesButton)
    }

    @Test
    fun stoppingGPSUpdatesShouldCallRemoveUpdatesOnLocationManager() {
        performClick(R.id.stopGpsUpdatesButton)

        verify { locationManager.removeUpdates(ofType(CustomLocationListener::class)) }
    }

    @After
    fun unloadKoin() {
//       unloadKoinModules(mockModule)
    }

    private fun performClick(viewId: Int) {
        onView(withId(viewId)).perform(click())
    }

    private fun setupRequestIntervalInput(requestInterval: Long) {
        onView(withId(R.id.gpsIntervalInput)).perform(typeText(requestInterval.toString()),
                ViewActions.closeSoftKeyboard())
    }
}