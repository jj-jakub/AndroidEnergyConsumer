package com.jj.androidenergyconsumer.domain.calculations

import com.jj.androidenergyconsumer.app.services.CalculationsService
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class CalculationsProviderFactoryTest {

    companion object {
        @JvmStatic
        private fun calculationsTypes() = CalculationsType.values().toList()

        @JvmStatic
        private fun orderedExpectedCalculationsProvidersTypes() = listOf(
                AdditionCalculationsProvider::class.java,
                MultiplicationCalculationsProvider::class.java)

        @JvmStatic
        fun calculationsTypesAndExpectedResults() =
            listsToPairs(calculationsTypes(), orderedExpectedCalculationsProvidersTypes())

        private fun <K, V> listsToPairs(list1: List<K>, list2: List<V?>): List<Pair<K, V?>> {
            val finalMap = HashMap<K, V?>()
            list1.forEachIndexed { index, key ->
                val value = list2.getOrNull(index)
                finalMap[key] = value
            }
            return finalMap.toList()
        }
    }

    @ParameterizedTest
    @MethodSource("calculationsTypesAndExpectedResults")
    fun `factory should return addition calculations provider`(typeAndResult: Pair<CalculationsType,
            Class<out CalculationsProvider>>) {

        assertTrue(CalculationsProviderFactory().createCalculationsProvider(typeAndResult.first,
                CalculationsService.DEFAULT_CALCULATIONS_FACTOR)::class.java == typeAndResult.second)
    }
}