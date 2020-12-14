package com.jj.androidenergyconsumer.bluetooth

import android.bluetooth.BluetoothAdapter
import android.content.Context
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag

class BluetoothScanner(context: Context) : IScanner {

    private val bluetoothBroadcastReceiver = BluetoothBroadcastReceiver(context)
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()

    override fun startScanning(scanningCallback: ScanningCallback): Boolean {
        logAndPingServer("startScanning", tag)
        bluetoothBroadcastReceiver.register(scanningCallback)
        return bluetoothAdapter?.startDiscovery()?.also { startedDiscovery ->
            if (!startedDiscovery) {
                bluetoothBroadcastReceiver.unregister()
            }
        } ?: false
    }

    override fun stopScanning() {
        logAndPingServer("stopScanning", tag)
        bluetoothAdapter?.cancelDiscovery()
        bluetoothBroadcastReceiver.unregister()
    }

    override fun isScanning(): Boolean = bluetoothAdapter?.isDiscovering ?: false
}