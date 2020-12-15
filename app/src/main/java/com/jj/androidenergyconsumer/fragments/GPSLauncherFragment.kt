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
import androidx.fragment.app.Fragment
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.permissions.PermissionManager
import com.jj.androidenergyconsumer.services.GPSService
import com.jj.androidenergyconsumer.services.MyBinder
import kotlinx.android.synthetic.main.fragment_gps_launcher.*
import java.util.concurrent.atomic.AtomicBoolean

class GPSLauncherFragment : Fragment() {

    companion object {
        fun newInstance(): GPSLauncherFragment = GPSLauncherFragment()
    }

    private var gpsService: GPSService? = null
    private var serviceBound = AtomicBoolean(false)
    private val permissionManager = PermissionManager()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_gps_launcher, container, false)


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
        constantGPSWorkButton?.setOnClickListener { startConstantGPSWork() }
        periodicGPSWorkButton?.setOnClickListener { startPeriodicGPSWork() }
        stopGpsUpdatesButton?.setOnClickListener { stopGPSUpdates() }
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
            gpsIntervalInput.text.toString().toLong()
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
            if (serviceBound.compareAndSet(true, false)) {
                context.unbindService(serviceConnection)
            }
            GPSService.stopGpsService(context)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, iBinder: IBinder?) {
            Log.d(tag, "onServiceConnected")
            val binder = iBinder as MyBinder?
            gpsService = (binder?.getService() as GPSService?)
            serviceBound.set(true)
            gpsService?.isWorking?.observe(this@GPSLauncherFragment, {
                onScanningStatusChanged(it)
            })
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            Log.d(tag, "onServiceDisconnected")
            serviceBound.set(false)
            gpsService = null
        }

        override fun onBindingDied(componentName: ComponentName?) {
            Log.d(tag, "onBindingDied")
            serviceBound.set(false)
            gpsService = null
            super.onBindingDied(componentName)
        }

        override fun onNullBinding(componentName: ComponentName?) {
            Log.d(tag, "onNullBinding")
            super.onNullBinding(componentName)
        }
    }

    private fun onScanningStatusChanged(scanningStatus: Boolean) {
        if (scanningStatus) {
            gpsWorkingStatusValueLabel.text = getString(R.string.running)
            gpsWorkingStatusValueLabel.setTextColor(Color.RED)
        } else {
            gpsWorkingStatusValueLabel.text = getString(R.string.not_running)
            gpsWorkingStatusValueLabel.setTextColor(Color.GREEN)
        }
    }

    private fun onPermissionNotGranted() {
        gpsWorkingStatusValueLabel.text = getString(R.string.permission_not_granted)
        gpsWorkingStatusValueLabel.setTextColor(Color.RED)
    }

    private fun onPermissionGranted() {
        context?.let { setupFragment(it) }
        gpsWorkingStatusValueLabel.setTextColor(Color.GREEN)
        gpsWorkingStatusValueLabel.text = getString(R.string.not_running)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.LOCATION_PERMISSION_REQUEST_CODE
            && grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            onPermissionGranted()
        }
    }
}