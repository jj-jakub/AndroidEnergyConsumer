package com.jj.androidenergyconsumer.domain.calculations

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