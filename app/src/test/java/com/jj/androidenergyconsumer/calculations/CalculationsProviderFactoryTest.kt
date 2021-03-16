package com.jj.androidenergyconsumer.calculations

import android.content.Intent
import com.jj.androidenergyconsumer.app.services.CalculationsService
import com.jj.androidenergyconsumer.domain.calculations.*
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mockito

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
        val intentMock = Mockito.mock(Intent::class.java)
        Mockito.`when`(intentMock.getIntExtra(CalculationsService.CALCULATIONS_FACTOR_EXTRA,
                CalculationsService.DEFAULT_CALCULATIONS_FACTOR))
            .thenReturn(CalculationsService.DEFAULT_CALCULATIONS_FACTOR)

        val calculationsCallbackMock = Mockito.mock(CalculationsCallback::class.java)

        Mockito.`when`(intentMock.getSerializableExtra(CalculationsService.CALCULATIONS_TYPE_EXTRA))
            .thenReturn(typeAndResult.first)
        assertTrue(CalculationsProviderFactory.createCalculationsProvider(intentMock,
                calculationsCallbackMock)::class.java == typeAndResult.second)
    }
}