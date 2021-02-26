package com.jj.androidenergyconsumer.bluetooth

interface IScanner {
    fun startScanning(): Boolean
    fun stopScanning()
    fun isScanning(): Boolean
}