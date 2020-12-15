package com.jj.androidenergyconsumer.bluetooth

import android.bluetooth.BluetoothDevice
import com.jj.androidenergyconsumer.notification.NotificationManager
import com.jj.androidenergyconsumer.utils.getDateStringWithMillis
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag

class BluetoothServiceScanningCallback(private val notificationManager: NotificationManager,
                                       private val onScanningFinished: () -> Unit) : ScanningCallback {

    override fun onDeviceDiscovered(device: BluetoothDevice?) {
        notificationManager.notifyBtServiceNotification("BluetoothService notification",
                "${getDateStringWithMillis()} device: ${device?.name} - ${device?.bluetoothClass?.deviceClass}")
        logAndPingServer("device: ${device?.name} - ${device?.bluetoothClass?.deviceClass}", tag)
    }

    override fun onScanningFinished() {
        onScanningFinished.invoke()
    }
}