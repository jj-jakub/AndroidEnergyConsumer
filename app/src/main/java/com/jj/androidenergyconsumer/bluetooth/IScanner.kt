package com.jj.androidenergyconsumer.bluetooth

interface IScanner {
    fun startScanning(scanningCallback: ScanningCallback)
    fun stopScanning()
    fun isScanning(): Boolean
}