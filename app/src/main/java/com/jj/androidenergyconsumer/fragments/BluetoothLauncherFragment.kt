package com.jj.androidenergyconsumer.fragments

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
import androidx.fragment.app.Fragment
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.services.BluetoothService
import com.jj.androidenergyconsumer.services.MyBinder
import kotlinx.android.synthetic.main.fragment_bluetooth_launcher.*
import java.util.concurrent.atomic.AtomicBoolean

class BluetoothLauncherFragment : Fragment() {

    companion object {
        fun newInstance(): BluetoothLauncherFragment = BluetoothLauncherFragment()
    }

    private var bluetoothService: BluetoothService? = null
    private var serviceBound = AtomicBoolean(false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_bluetooth_launcher, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonsListeners()
        context?.let { context -> bindToBluetoothService(context) }
    }

    private fun setButtonsListeners() {
        startBluetoothScanningButton?.setOnClickListener { startBluetoothService() }
        abortBluetoothScanningButton?.setOnClickListener { abortBluetoothService() }
    }

    private fun startBluetoothService() {
        context?.let { context ->
            bindToBluetoothService(context)
            BluetoothService.startScanning(context)
        }
    }

    private fun bindToBluetoothService(context: Context) {
        val serviceIntent = BluetoothService.getServiceIntent(context)
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun abortBluetoothService() {
        context?.let { context ->
            if (serviceBound.compareAndSet(true, false)) {
                context.unbindService(serviceConnection)
            }
            BluetoothService.stopScanning(context)
        }
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
        if (scanningStatus) {
            bluetoothScanningStatusValueLabel.text = getString(R.string.running)
            bluetoothScanningStatusValueLabel.setTextColor(Color.RED)
        } else {
            bluetoothScanningStatusValueLabel.text = getString(R.string.not_running)
            bluetoothScanningStatusValueLabel.setTextColor(Color.GREEN)
        }
    }
}