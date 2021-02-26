package com.jj.androidenergyconsumer.bluetooth

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.jj.androidenergyconsumer.utils.*
import kotlinx.coroutines.flow.SharedFlow
import java.util.concurrent.atomic.AtomicBoolean

sealed class BluetoothBroadcastResult {
    data class FoundDevice(val device: BluetoothDevice) : BluetoothBroadcastResult()
    object DiscoveryFinished : BluetoothBroadcastResult()
}

class BluetoothBroadcastReceiver(private val context: Context) : CustomBroadcastReceiver() {

    private val receiverRegistered = AtomicBoolean(false)
    private val bluetoothScanResults = BufferedMutableSharedFlow<BluetoothBroadcastResult>()

    fun observeBluetoothResults(): SharedFlow<BluetoothBroadcastResult> = bluetoothScanResults

    override fun onReceive(context: Context?, intent: Intent?) {
        logAndPingServer("Received action: ${intent?.action}", tag)
        if (intent?.action == BluetoothDevice.ACTION_FOUND) {
            val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
            logAndPingServer("Discovered device: ${device?.name}, address: ${device?.address}", tag)
            device?.let { bluetoothScanResults.tryEmit(BluetoothBroadcastResult.FoundDevice(device)) }
        }
        if (intent?.action == BluetoothAdapter.ACTION_DISCOVERY_FINISHED) {
            bluetoothScanResults.tryEmit(BluetoothBroadcastResult.DiscoveryFinished)
        }
    }

    override fun register() {
        if (receiverRegistered.compareAndSet(false, true)) context.registerReceiver(this, intentFilter)
    }

    override fun unregister() {
        safelyUnregisterReceiver(context)
        receiverRegistered.set(false)
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