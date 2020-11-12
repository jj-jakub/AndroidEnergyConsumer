package com.jj.androidenergyconsumer.calculations

import android.content.Intent
import com.jj.androidenergyconsumer.services.CalculationsService

enum class CalculationsType {
    ADDITION, MULTIPLICATION
}

class CalculationsProviderFactory {

    companion object {
        fun createCalculationsProvider(intentWithExtras: Intent, callback: CalculationsCallback): CalculationsProvider {
            val calculationsType = getCalculationsType(intentWithExtras)
            val factor = getCalculationsFactor(intentWithExtras)
            return createCalculationsProvider(calculationsType, callback, factor)
        }

        private fun getCalculationsType(intent: Intent): CalculationsType =
            (intent.getSerializableExtra(CalculationsService.CALCULATIONS_TYPE_EXTRA)
                ?: CalculationsService.DEFAULT_CALCULATIONS_TYPE) as CalculationsType

        private fun getCalculationsFactor(intent: Intent): Int =
            intent.getIntExtra(CalculationsService.CALCULATIONS_FACTOR_EXTRA,
                    CalculationsService.DEFAULT_CALCULATIONS_FACTOR)

        private fun createCalculationsProvider(type: CalculationsType, callback: CalculationsCallback,
                                               factor: Int): CalculationsProvider {
            return when (type) {
                CalculationsType.ADDITION -> AdditionCalculationsProvider(callback, factor)
                CalculationsType.MULTIPLICATION -> MultiplicationCalculationsProvider(callback, factor)
            }
        }
    }
}