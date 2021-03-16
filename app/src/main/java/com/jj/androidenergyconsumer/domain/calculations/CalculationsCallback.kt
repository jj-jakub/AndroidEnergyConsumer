package com.jj.androidenergyconsumer.domain.calculations

interface CalculationsCallback {
    fun onThresholdAchieved(variable: Int, handlerId: Int)
}