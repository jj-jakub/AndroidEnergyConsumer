package com.jj.androidenergyconsumer.fragments

import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.app.bluetooth.BluetoothScanner
import com.jj.androidenergyconsumer.app.fragments.BluetoothLauncherFragment
import com.jj.androidenergyconsumer.app.permissions.PermissionManager
import com.jj.androidenergyconsumer.utils.performClick
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext
import org.koin.core.module.Module
import org.koin.dsl.module

@RunWith(AndroidJUnit4::class)
class BluetoothFragmentInstrumentedTest {

    @RelaxedMockK
    private lateinit var permissionManager: PermissionManager

    @RelaxedMockK
    private lateinit var bluetoothScanner: BluetoothScanner

    private lateinit var mockModule: Module

    @Before
    fun setup() {
        setupMocks()
        launchFragmentInContainer<BluetoothLauncherFragment>()
    }

    private fun setupMocks() {
        MockKAnnotations.init(this)
        every { permissionManager.areLocationsPermissionGranted(any()) } returns true
        mockModule = module {
            single(override = true) { permissionManager }
            single(override = true) { bluetoothScanner }
        }
        GlobalContext.loadKoinModules(mockModule)
    }

    @Test
    fun startBluetoothScanShouldCallStartScanningOnBluetoothScanner() {
        performClick(R.id.startBluetoothScanningButton)

        verify { bluetoothScanner.startScanning() }

        // TODO In future launch every test in new environment instead of closing service by clicking stop
        performClick(R.id.abortBluetoothScanningButton)
    }

    @Test
    fun stopBluetoothScanShouldCallStopScanningOnBluetoothScanner() {
        performClick(R.id.abortBluetoothScanningButton)

        verify { bluetoothScanner.stopScanning() }

        performClick(R.id.abortBluetoothScanningButton)
    }
}