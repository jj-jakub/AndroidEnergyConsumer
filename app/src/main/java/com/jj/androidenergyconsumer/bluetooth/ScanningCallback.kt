package com.jj.androidenergyconsumer.bluetooth

import android.bluetooth.BluetoothDevice

interface ScanningCallback {
    fun onDeviceDiscovered(device: BluetoothDevice?)
}