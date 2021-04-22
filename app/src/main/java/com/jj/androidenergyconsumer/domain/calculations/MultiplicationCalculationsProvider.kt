package com.jj.androidenergyconsumer.domain.calculations

import android.util.Log
import com.jj.androidenergyconsumer.domain.coroutines.BufferedMutableSharedFlow
import com.jj.androidenergyconsumer.domain.tag
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.math.abs

class MultiplicationCalculationsProvider(factor: Int) : CalculationsProvider {

    private val calculationsFactor: Int
    override val calculationsResultFlow: MutableSharedFlow<CalculationsResult> = BufferedMutableSharedFlow()

    init {
        if (listOf(-1, 0, 1).contains(factor)) throw IllegalArgumentException("Factor cannot be equal to -1, 0 or 1")
        calculationsFactor = factor
    }

    override fun calculationsTask(threadId: Int) {
        var variable = 1
        while (true) {
            variable *= calculationsFactor
            if (abs(variable) > 100000000) {
                Log.d(tag, "threadId: $threadId variable: $variable")
                calculationsResultFlow.tryEmit(CalculationsResult(variable, threadId))
                break
            }
        }
    }
}