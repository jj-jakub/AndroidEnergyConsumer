package com.jj.androidenergyconsumer.fragments

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
import com.jj.androidenergyconsumer.databinding.FragmentGpsLauncherBinding
import com.jj.androidenergyconsumer.permissions.PermissionManager
import com.jj.androidenergyconsumer.services.GPSService
import com.jj.androidenergyconsumer.services.MyBinder
import kotlinx.coroutines.flow.collect
import com.jj.androidenergyconsumer.utils.tag as LogTag

class GPSLauncherFragment : BaseLauncherFragment() {

    private lateinit var fragmentGpsLauncherBinding: FragmentGpsLauncherBinding

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

    private fun setupFragment(context: Context) {
        setButtonsListeners()
        bindToGPSService(context)
    }

    private fun setButtonsListeners() {
        fragmentGpsLauncherBinding.apply {
            constantGPSWorkButton.setOnClickListener { startConstantGPSWork() }
            periodicGPSWorkButton.setOnClickListener { startPeriodicGPSWork() }
            stopGpsUpdatesButton.setOnClickListener { stopGPSUpdates() }
        }
    }

    private fun startConstantGPSWork() {
        context?.let { context ->
            bindToGPSService(context)
            GPSService.startConstantUpdates(context)
        }
    }

    private fun startPeriodicGPSWork() {
        context?.let { context ->
            val millisIntervalFromInput = getMillisFromInput()
            bindToGPSService(context)
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

    private fun bindToGPSService(context: Context) {
        val serviceIntent = GPSService.getServiceIntent(context)
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun stopGPSUpdates() {
        context?.let { context ->
            unbindFromService(context)
            GPSService.stopGpsService(context)
        }
    }

    override val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, iBinder: IBinder?) {
            Log.d(LogTag, "onServiceConnected")
            val binder = iBinder as MyBinder?
            (binder?.getService() as GPSService?)?.let { service ->
                gpsService = service
                serviceBound.set(true)
                lifecycleScope.launchWhenResumed { service.observeIsWorking().collect { onWorkingStatusChanged(it) } }
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

    private fun onPermissionNotGranted() {
        fragmentGpsLauncherBinding.apply {
            gpsWorkingStatusValueLabel.text = getString(R.string.permission_not_granted)
            gpsWorkingStatusValueLabel.setTextColor(Color.RED)
        }
    }

    private fun onPermissionGranted() {
        context?.let { setupFragment(it) }
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