package com.jj.androidenergyconsumer

import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiObjectNotFoundException
import androidx.test.uiautomator.UiSelector
import com.jj.androidenergyconsumer.app.activities.MainActivity
import com.jj.androidenergyconsumer.app.utils.BatterySettingsLauncher
import com.jj.androidenergyconsumer.domain.tag
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.java.KoinJavaComponent.inject

@RunWith(AndroidJUnit4::class)
class MainActivityTest {

    companion object {
        private const val ENERGY_OPTIMIZATION_DIALOG_TIMEOUT = 5000L
        private const val ALLOW_STRING_REGEX = "(?i)\\bAllow\\b"
    }

    private lateinit var mDevice: UiDevice

    private val batterySettingsLauncher: BatterySettingsLauncher by inject(BatterySettingsLauncher::class.java)

    @get:Rule
    var rule: ActivityScenarioRule<MainActivity> = ActivityScenarioRule(MainActivity::class.java)

    @Before
    fun setup() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        handleEnergyOptimizationDialog()
    }

    @Test
    fun allFiveButtonsShouldBeVisible() {
        onView(withId(R.id.calculationsButton)).check(matches(isDisplayed()))
        onView(withId(R.id.gpsModuleLauncherButton)).check(matches(isDisplayed()))
        onView(withId(R.id.internetModuleLauncherButton)).check(matches(isDisplayed()))
        onView(withId(R.id.sensorsModuleLauncherButton)).check(matches(isDisplayed()))
        onView(withId(R.id.bluetoothModuleLauncherButton)).check(matches(isDisplayed()))
    }

    private fun handleEnergyOptimizationDialog() {
        if (!batterySettingsLauncher.isAppIgnoringBatteryOptimizations()) {
            try {
                mDevice.findObject(UiSelector().textMatches(ALLOW_STRING_REGEX)).apply {
                    waitForExists(ENERGY_OPTIMIZATION_DIALOG_TIMEOUT)
                    click()
                }
            } catch (e: UiObjectNotFoundException) {
                Log.e(tag, "handleEnergyOptimizationDialog exception", e)
            }
        }
    }
}