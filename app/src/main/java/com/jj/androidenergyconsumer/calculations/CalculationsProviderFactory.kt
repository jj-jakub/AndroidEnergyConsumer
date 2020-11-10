package com.jj.androidenergyconsumer.calculations

enum class CalculationsType {
    ADDITION, MULTIPLICATION
}

class CalculationsProviderFactory {
    companion object {
        fun createCalculationsProvider(type: CalculationsType, callback: CalculationsCallback,
                                       factor: Int): CalculationsProvider {
            return when (type) {
                CalculationsType.ADDITION -> AdditionCalculationsProvider(callback, factor)
                CalculationsType.MULTIPLICATION -> MultiplicationCalculationsProvider(callback, factor)
            }
        }
    }
}