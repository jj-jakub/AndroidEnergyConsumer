package com.jj.androidenergyconsumer.domain.calculations

import android.util.Log
import com.jj.androidenergyconsumer.domain.coroutines.BufferedMutableSharedFlow
import com.jj.androidenergyconsumer.domain.tag
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlin.math.abs

class AdditionCalculationsProvider(factor: Int) : CalculationsProvider {

    private val calculationsFactor: Int

    //TODO Emit states of calculations? BEFORE, CALCULATING, AFTER?
    override val calculationsResultFlow: MutableSharedFlow<CalculationsResult> = BufferedMutableSharedFlow()

    init {
        if (factor == 0) throw IllegalArgumentException("Factor cannot be equal to 0")
        calculationsFactor = factor
    }

    override fun calculationsTask(threadId: Int) {
        var variable = 0
        while (true) {
            variable += calculationsFactor
            if (abs(variable) > 100000000) {
                Log.d(tag, "threadId: $threadId variable: $variable")
                calculationsResultFlow.tryEmit(CalculationsResult(variable, threadId))
                break
            }
        }
    }
}