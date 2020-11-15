package com.jj.androidenergyconsumer.bluetooth

interface IScanner {
    fun startScanning()
    fun stopScanning()
    fun isScanning(): Boolean
}