package com.jj.androidenergyconsumer.calculations

interface CalculationsCallback {
    fun onThresholdAchieved(variable: Int, handlerId: Int)
}