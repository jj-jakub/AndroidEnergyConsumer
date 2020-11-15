package com.jj.androidenergyconsumer.bluetooth

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.jj.androidenergyconsumer.utils.CustomBroadcastReceiver
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag

class BluetoothBroadcastReceiver(private val context: Context) : CustomBroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        logAndPingServer("Received action: ${intent?.action}", tag)
        if (intent?.action == BluetoothDevice.ACTION_FOUND) {
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            logAndPingServer("Discovered device: ${device?.name}, address: ${device?.address}", tag)
        }
    }

    override fun register() {
        context.registerReceiver(this, intentFilter)
    }

    override fun unregister() {
        try {
            context.unregisterReceiver(this)
        } catch (iae: IllegalArgumentException) {
            Log.e(tag, "Error when unregistering receiver", iae)
        }
    }

    private val intentFilter = IntentFilter().apply {
        addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
        addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        addAction(BluetoothDevice.ACTION_FOUND)
        addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
        addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
    }
}