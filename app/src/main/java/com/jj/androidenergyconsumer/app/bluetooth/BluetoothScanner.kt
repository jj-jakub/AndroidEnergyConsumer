package com.jj.androidenergyconsumer.app.bluetooth

import android.bluetooth.BluetoothAdapter
import com.jj.androidenergyconsumer.app.utils.logAndPingServer
import com.jj.androidenergyconsumer.domain.tag
import kotlinx.coroutines.flow.SharedFlow

class BluetoothScanner(private val bluetoothBroadcastReceiver: BluetoothBroadcastReceiver) : IScanner {

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    fun observeBluetoothResults(): SharedFlow<BluetoothBroadcastResult> =
        bluetoothBroadcastReceiver.observeBluetoothResults()

    override fun startScanning(): Boolean {
        logAndPingServer("startScanning", tag)
        bluetoothBroadcastReceiver.register()
        val startedDiscovery = bluetoothAdapter?.startDiscovery() ?: false
        if (!startedDiscovery) stopScanning()
        logAndPingServer("bluetoothAdapter.startDiscovery() = $startedDiscovery", tag)
        return startedDiscovery
    }

    override fun stopScanning() {
        logAndPingServer("stopScanning", tag)
        bluetoothAdapter?.cancelDiscovery()
        bluetoothBroadcastReceiver.unregister()
    }

    override fun isScanning(): Boolean = bluetoothAdapter?.isDiscovering ?: false
}