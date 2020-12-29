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
import com.jj.androidenergyconsumer.databinding.FragmentBluetoothLauncherBinding
import com.jj.androidenergyconsumer.permissions.PermissionManager
import com.jj.androidenergyconsumer.services.BluetoothService
import com.jj.androidenergyconsumer.services.MyBinder
import java.util.concurrent.atomic.AtomicBoolean

class BluetoothLauncherFragment : Fragment() {

    private lateinit var fragmentBluetoothLauncherBinding: FragmentBluetoothLauncherBinding

    private var bluetoothService: BluetoothService? = null
    private var serviceBound = AtomicBoolean(false)
    private val permissionManager = PermissionManager()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentBluetoothLauncherBinding = FragmentBluetoothLauncherBinding.inflate(inflater, container, false)
        return fragmentBluetoothLauncherBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { activity ->
            if (permissionManager.isLocationPermissionGranted(activity)) {
                onPermissionGranted()
            } else {
                onPermissionNotGranted()
                requestLocationPermission()
            }
        }
    }

    private fun onPermissionGranted() {
        context?.let { setupFragment(it) }
    }

    private fun setupFragment(context: Context) {
        bindToBluetoothService(context)
        setButtonsListeners()
        fragmentBluetoothLauncherBinding.apply {
            bluetoothScanningStatusValueLabel.setTextColor(Color.GREEN)
            bluetoothScanningStatusValueLabel.text = getString(R.string.not_running)
        }
    }

    private fun bindToBluetoothService(context: Context) {
        val serviceIntent = BluetoothService.getServiceIntent(context)
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun setButtonsListeners() {
        fragmentBluetoothLauncherBinding.apply {
            startBluetoothScanningButton.setOnClickListener { startBluetoothService() }
            abortBluetoothScanningButton.setOnClickListener { abortBluetoothService() }
        }
    }

    private fun startBluetoothService() {
        context?.let { context ->
            bindToBluetoothService(context)
            BluetoothService.startScanning(context)
        }
    }

    private fun abortBluetoothService() {
        context?.let { context ->
            if (serviceBound.compareAndSet(true, false)) {
                context.unbindService(serviceConnection)
            }
            BluetoothService.stopScanning(context)
        }
    }

    private fun onPermissionNotGranted() {
        fragmentBluetoothLauncherBinding.apply {
            bluetoothScanningStatusValueLabel.text = getString(R.string.permission_not_granted)
            bluetoothScanningStatusValueLabel.setTextColor(Color.RED)
        }
    }

    private fun requestLocationPermission() {
        requestPermissions(arrayOf(PermissionManager.LOCATION_PERMISSION),
                PermissionManager.LOCATION_PERMISSION_REQUEST_CODE)
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, iBinder: IBinder?) {
            Log.d(tag, "onServiceConnected")
            val binder = iBinder as MyBinder?
            bluetoothService = (binder?.getService() as BluetoothService?)
            serviceBound.set(true)
            bluetoothService?.isScanning?.observe(this@BluetoothLauncherFragment, {
                onScanningStatusChanged(it)
            })
            bluetoothService?.errorMessage?.observe(this@BluetoothLauncherFragment, {
                onErrorMessageChanged(it)
            })
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            Log.d(tag, "onServiceDisconnected")
            serviceBound.set(false)
            bluetoothService = null
        }

        override fun onBindingDied(componentName: ComponentName?) {
            Log.d(tag, "onBindingDied")
            serviceBound.set(false)
            bluetoothService = null
            super.onBindingDied(componentName)
        }

        override fun onNullBinding(componentName: ComponentName?) {
            Log.d(tag, "onNullBinding")
            super.onNullBinding(componentName)
        }
    }

    private fun onScanningStatusChanged(scanningStatus: Boolean) {
        fragmentBluetoothLauncherBinding.apply {
            if (scanningStatus) {
                bluetoothScanningStatusValueLabel.text = getString(R.string.running)
                bluetoothScanningStatusValueLabel.setTextColor(Color.RED)
            } else {
                bluetoothScanningStatusValueLabel.text = getString(R.string.not_running)
                bluetoothScanningStatusValueLabel.setTextColor(Color.GREEN)
            }
        }
    }

    private fun onErrorMessageChanged(errorMessage: String?) {
        fragmentBluetoothLauncherBinding.bluetoothErrorMessageLabel.text = errorMessage ?: ""
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionManager.LOCATION_PERMISSION_REQUEST_CODE
            && grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            onPermissionGranted()
        }
    }
}