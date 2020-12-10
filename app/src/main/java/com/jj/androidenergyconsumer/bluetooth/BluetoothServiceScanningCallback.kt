package com.jj.androidenergyconsumer.bluetooth

import android.bluetooth.BluetoothDevice
import com.jj.androidenergyconsumer.notification.NotificationManagerBuilder
import com.jj.androidenergyconsumer.utils.getDateStringWithMillis
import com.jj.androidenergyconsumer.utils.logAndPingServer
import com.jj.androidenergyconsumer.utils.tag

class BluetoothServiceScanningCallback(private val notificationManagerBuilder: NotificationManagerBuilder,
                                       private val onScanningFinished: () -> Unit) : ScanningCallback {

    override fun onDeviceDiscovered(device: BluetoothDevice?) {
        notificationManagerBuilder.notifyServiceNotification("BluetoothService notification",
                "${getDateStringWithMillis()} device: ${device?.name} - ${device?.bluetoothClass?.deviceClass}")
        logAndPingServer("device: ${device?.name} - ${device?.bluetoothClass?.deviceClass}", tag)
    }

    override fun onScanningFinished() {
        onScanningFinished.invoke()
    }
}