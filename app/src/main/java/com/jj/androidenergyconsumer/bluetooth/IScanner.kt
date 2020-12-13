package com.jj.androidenergyconsumer.bluetooth

interface IScanner {
    fun startScanning(scanningCallback: ScanningCallback): Boolean
    fun stopScanning()
    fun isScanning(): Boolean
}