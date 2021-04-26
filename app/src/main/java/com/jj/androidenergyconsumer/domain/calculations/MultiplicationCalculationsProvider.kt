package com.jj.androidenergyconsumer.domain.calculations

import android.util.Log
import com.jj.androidenergyconsumer.domain.coroutines.BufferedMutableSharedFlow
import com.jj.androidenergyconsumer.domain.tag
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.math.abs

class MultiplicationCalculationsProvider(factor: Int) : CalculationsProvider {

    companion object {
        private const val CALCULATIONS_SUM_THRESHOLD_VALUE = 100000000
    }

    private val calculationsFactor: Int
    override var calculationsAborted: Boolean = false
    override val calculationsResultFlow: MutableSharedFlow<CalculationsResult> = BufferedMutableSharedFlow()

    init {
        if (listOf(-1, 0, 1).contains(factor)) throw IllegalArgumentException("Factor cannot be equal to -1, 0 or 1")
        calculationsFactor = factor
    }

    override fun startCalculationsTask(threadId: Int) {
        var multiplicationsResult = 1

        while (true) {
            if (calculationsAborted) break
            multiplicationsResult *= calculationsFactor

            if (abs(multiplicationsResult) > CALCULATIONS_SUM_THRESHOLD_VALUE) {
                onThresholdAchieved(threadId, multiplicationsResult)
                break
            }
        }
    }

    private fun onThresholdAchieved(threadId: Int, multiplicationsResult: Int) {
        Log.d(tag, "threadId: $threadId multiplicationsResult: $multiplicationsResult")
        calculationsResultFlow.tryEmit(CalculationsResult(multiplicationsResult, threadId))
    }
}