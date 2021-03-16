package com.jj.androidenergyconsumer.app.bluetooth

interface IScanner {
    fun startScanning(): Boolean
    fun stopScanning()
    fun isScanning(): Boolean
}