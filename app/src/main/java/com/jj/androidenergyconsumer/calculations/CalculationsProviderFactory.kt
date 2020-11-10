package com.jj.androidenergyconsumer.calculations

enum class CalculationsType {
    ADDITION, MULTIPLICATION
}

class CalculationsProviderFactory {
    companion object {
        fun createCalculationsProvider(type: CalculationsType, callback: CalculationsCallback): CalculationsProvider {
            return when (type) {
                CalculationsType.ADDITION -> AdditionCalculationsProvider(callback)
                CalculationsType.MULTIPLICATION -> MultiplicationCalculationsProvider(callback)
            }
        }
    }
}