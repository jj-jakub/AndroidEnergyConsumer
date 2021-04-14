package com.jj.androidenergyconsumer.app.fragments

import android.bluetooth.BluetoothDevice
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.app.bluetooth.BluetoothBroadcastResult
import com.jj.androidenergyconsumer.app.bluetooth.BluetoothScanner
import com.jj.androidenergyconsumer.app.services.BluetoothService
import com.jj.androidenergyconsumer.app.services.MyBinder
import com.jj.androidenergyconsumer.databinding.FragmentBluetoothLauncherBinding
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import com.jj.androidenergyconsumer.domain.tag as LogTag

class BluetoothLauncherFragment : BaseLauncherFragment() {

    private lateinit var fragmentBluetoothLauncherBinding: FragmentBluetoothLauncherBinding
    override val activityTitle: String = "Bluetooth launcher"

    private val bluetoothScanner: BluetoothScanner by inject()
    private var bluetoothService: BluetoothService? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentBluetoothLauncherBinding = FragmentBluetoothLauncherBinding.inflate(inflater, container, false)
        return fragmentBluetoothLauncherBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        manageLocationPermission()
    }

    override fun onAllLocationPermissionsGranted() {
        setupFragment()
    }

    override fun onPermissionsNotGranted() {
        fragmentBluetoothLauncherBinding.apply {
            bluetoothScanningStatusValueLabel.text = getString(R.string.permission_not_granted)
            bluetoothScanningStatusValueLabel.setTextColor(Color.RED)
        }
    }

    private fun setupFragment() {
        bindToBluetoothService()
        observeBluetoothResults()
        setButtonsListeners()
        fragmentBluetoothLauncherBinding.apply {
            bluetoothScanningStatusValueLabel.setTextColor(Color.GREEN)
            bluetoothScanningStatusValueLabel.text = getString(R.string.not_running)
        }
    }

    private fun bindToBluetoothService() {
        context?.let { context ->
            val serviceIntent = BluetoothService.getServiceIntent(context)
            context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun setButtonsListeners() {
        fragmentBluetoothLauncherBinding.apply {
            startBluetoothScanningButton.setOnClickListener { startBluetoothService() }
            abortBluetoothScanningButton.setOnClickListener { abortBluetoothService() }
        }
    }

    private fun observeBluetoothResults() {
        with(lifecycleScope) {
            launchWhenResumed {
                bluetoothScanner.observeBluetoothResults().collect { result ->
                    when (result) {
                        is BluetoothBroadcastResult.DiscoveryFinished -> onScanningFinished()
                        is BluetoothBroadcastResult.FoundDevice -> onDeviceFound(result.device)
                    }
                }
            }
        }
    }

    private fun onDeviceFound(device: BluetoothDevice) {
        fragmentBluetoothLauncherBinding.lastScanningResultValue.text =
            "device: ${device.name} - ${device.bluetoothClass?.deviceClass}"
    }

    private fun onScanningFinished() {
        fragmentBluetoothLauncherBinding.lastScanningResultValue.text = "Scanning finished"
    }

    private fun startBluetoothService() {
        bindToBluetoothService()
        context?.let { context -> BluetoothService.startScanning(context) }
    }

    private fun abortBluetoothService() {
        unbindFromService()
        context?.let { context -> BluetoothService.stopScanning(context) }
    }

    override val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, iBinder: IBinder?) {
            Log.d(LogTag, "onServiceConnected")
            val binder = iBinder as MyBinder?
            (binder?.getService() as BluetoothService?)?.let { service ->
                bluetoothService = service
                serviceBound.set(true)
                with(lifecycleScope) {
                    launchWhenResumed { service.observeIsScanning().collect { onScanningStatusChanged(it) } }
                    launchWhenResumed { service.observeErrorMessage().collect { onErrorMessageChanged(it) } }
                }
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            Log.d(LogTag, "onServiceDisconnected")
            serviceBound.set(false)
            bluetoothService = null
        }

        override fun onBindingDied(componentName: ComponentName?) {
            Log.d(LogTag, "onBindingDied")
            serviceBound.set(false)
            bluetoothService = null
            super.onBindingDied(componentName)
        }

        override fun onNullBinding(componentName: ComponentName?) {
            Log.d(LogTag, "onNullBinding")
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
}