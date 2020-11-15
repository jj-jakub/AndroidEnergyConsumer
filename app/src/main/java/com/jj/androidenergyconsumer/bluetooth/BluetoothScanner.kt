package com.jj.androidenergyconsumer.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Context

class BluetoothScanner(context: Context) : IScanner {

    private val bluetoothBroadcastReceiver = BluetoothBroadcastReceiver(context)
    private val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    override fun startScanning() {
        bluetoothBroadcastReceiver.register()
        bluetoothAdapter.startDiscovery()
    }

    override fun stopScanning() {
        bluetoothAdapter.cancelDiscovery()
        bluetoothBroadcastReceiver.unregister()
    }

    override fun isScanning(): Boolean = bluetoothAdapter.isDiscovering
}