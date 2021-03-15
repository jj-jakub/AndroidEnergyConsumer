package com.jj.androidenergyconsumer.calculations

enum class CalculationsType {
    ADDITION, MULTIPLICATION
}

class CalculationsProviderFactory {
    fun createCalculationsProvider(calculationsType: CalculationsType, factor: Int): CalculationsProvider {
        return when (calculationsType) {
            CalculationsType.ADDITION -> AdditionCalculationsProvider(factor)
            CalculationsType.MULTIPLICATION -> MultiplicationCalculationsProvider(factor)
        }
    }
}