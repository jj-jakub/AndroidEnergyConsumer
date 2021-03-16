package com.jj.androidenergyconsumer.app.fragments

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.app.gps.CustomLocationListener
import com.jj.androidenergyconsumer.app.gps.LocationListenerResult
import com.jj.androidenergyconsumer.app.permissions.PermissionManager
import com.jj.androidenergyconsumer.app.services.GPSService
import com.jj.androidenergyconsumer.app.services.MyBinder
import com.jj.androidenergyconsumer.databinding.FragmentGpsLauncherBinding
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import com.jj.androidenergyconsumer.domain.tag as LogTag

class GPSLauncherFragment : BaseLauncherFragment() {

    private lateinit var fragmentGpsLauncherBinding: FragmentGpsLauncherBinding
    private val customLocationListener: CustomLocationListener by inject()
    override val activityTitle: String = "GPS launcher"

    private var gpsService: GPSService? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentGpsLauncherBinding = FragmentGpsLauncherBinding.inflate(inflater, container, false)
        return fragmentGpsLauncherBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manageLocationPermission()
    }

    private fun manageLocationPermission() {
        activity?.let { activity ->
            if (permissionManager.isLocationPermissionGranted(activity)) {
                onPermissionGranted()
            } else {
                onPermissionNotGranted()
                permissionManager.requestLocationPermission(this)
            }
        }
    }

    private fun setupFragment() {
        bindToGPSService()
        setButtonsListeners()
        observeGPSResults()
    }

    private fun observeGPSResults() {
        with(lifecycleScope) {
            launchWhenResumed {
                customLocationListener.observeLocationInfoUpdates().collect { result ->
                    when (result) {
                        is LocationListenerResult.LocationChanged -> onLocationChanged(result)
                        else -> {
                            /* no-op */
                        }
                    }
                }
            }
        }
    }

    private fun onLocationChanged(result: LocationListenerResult.LocationChanged) {
        val coordinates = "lat: ${result.location.latitude}, lon: ${result.location.longitude}"
        fragmentGpsLauncherBinding.lastGpsResultValue.text = coordinates
    }

    private fun setButtonsListeners() {
        fragmentGpsLauncherBinding.apply {
            constantGPSWorkButton.setOnClickListener { startConstantGPSWork() }
            periodicGPSWorkButton.setOnClickListener { startPeriodicGPSWork() }
            stopGpsUpdatesButton.setOnClickListener { stopGPSUpdates() }
        }
    }

    private fun startConstantGPSWork() {
        bindToGPSService()
        context?.let { context -> GPSService.startConstantUpdates(context) }
    }

    private fun startPeriodicGPSWork() {
        bindToGPSService()
        context?.let { context ->
            val millisIntervalFromInput = getMillisFromInput()
            GPSService.startPeriodicUpdates(context, millisIntervalFromInput)
        }
    }

    private fun getMillisFromInput(): Long =
        try {
            fragmentGpsLauncherBinding.gpsIntervalInput.text.toString().toLong()
        } catch (e: Exception) {
            Log.e(tag, "Exception while converting input interval", e)
            0
        }

    private fun bindToGPSService() {
        context?.let { context ->
            val serviceIntent = GPSService.getServiceIntent(context)
            context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun stopGPSUpdates() {
        unbindFromService()
        context?.let { context -> GPSService.stopGpsService(context) }
    }

    override val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, iBinder: IBinder?) {
            Log.d(LogTag, "onServiceConnected")
            val binder = iBinder as MyBinder?
            (binder?.getService() as GPSService?)?.let { service ->
                gpsService = service
                serviceBound.set(true)
                lifecycleScope.launchWhenResumed {
                    service.observeIsWorking().collect { onWorkingStatusChanged(it) }
                    service.observeErrorMessage().collect { onErrorMessageChanged(it) }
                }
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            Log.d(LogTag, "onServiceDisconnected")
            serviceBound.set(false)
            gpsService = null
        }

        override fun onBindingDied(componentName: ComponentName?) {
            Log.d(LogTag, "onBindingDied")
            serviceBound.set(false)
            gpsService = null
            super.onBindingDied(componentName)
        }

        override fun onNullBinding(componentName: ComponentName?) {
            Log.d(LogTag, "onNullBinding")
            super.onNullBinding(componentName)
        }
    }

    private fun onWorkingStatusChanged(workingStatus: Boolean) {
        fragmentGpsLauncherBinding.apply {
            if (workingStatus) {
                gpsWorkingStatusValueLabel.text = getString(R.string.running)
                gpsWorkingStatusValueLabel.setTextColor(Color.RED)
            } else {
                gpsWorkingStatusValueLabel.text = getString(R.string.not_running)
                gpsWorkingStatusValueLabel.setTextColor(Color.GREEN)
            }
        }
    }

    private fun onErrorMessageChanged(errorMessage: String?) {
        fragmentGpsLauncherBinding.gpsErrorMessageLabel.text = errorMessage ?: ""
    }

    private fun onPermissionNotGranted() {
        fragmentGpsLauncherBinding.apply {
            gpsWorkingStatusValueLabel.text = getString(R.string.permission_not_granted)
            gpsWorkingStatusValueLabel.setTextColor(Color.RED)
        }
    }

    private fun onPermissionGranted() {
        setupFragment()
        fragmentGpsLauncherBinding.apply {
            gpsWorkingStatusValueLabel.setTextColor(Color.GREEN)
            gpsWorkingStatusValueLabel.text = getString(R.string.not_running)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.LOCATION_PERMISSION_REQUEST_CODE
            && grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            onPermissionGranted()
        }
    }
}