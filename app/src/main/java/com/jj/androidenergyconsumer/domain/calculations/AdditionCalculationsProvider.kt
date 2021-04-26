package com.jj.androidenergyconsumer.domain.calculations

import android.util.Log
import com.jj.androidenergyconsumer.domain.coroutines.BufferedMutableSharedFlow
import com.jj.androidenergyconsumer.domain.tag
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.math.abs

class AdditionCalculationsProvider(factor: Int) : CalculationsProvider {

    companion object {
        private const val CALCULATIONS_SUM_THRESHOLD_VALUE = 100000000
    }

    private val calculationsFactor: Int
    override var calculationsAborted: Boolean = false

    //TODO Emit states of calculations? BEFORE, CALCULATING, AFTER?
    override val calculationsResultFlow: MutableSharedFlow<CalculationsResult> = BufferedMutableSharedFlow()

    init {
        if (factor == 0) throw IllegalArgumentException("Factor cannot be equal to 0")
        calculationsFactor = factor
    }

    override fun startCalculationsTask(threadId: Int) {
        var additionSum = 0

        while (true) {
            if (calculationsAborted) break
            additionSum += calculationsFactor

            if (abs(additionSum) > CALCULATIONS_SUM_THRESHOLD_VALUE) {
                onThresholdAchieved(threadId, additionSum)
                break
            }
        }
    }

    private fun onThresholdAchieved(threadId: Int, additionSum: Int) {
        Log.d(tag, "threadId: $threadId additionSum: $additionSum")
        calculationsResultFlow.tryEmit(CalculationsResult(additionSum, threadId))
    }
}