package com.jj.androidenergyconsumer.calculations

import android.util.Log
import com.jj.androidenergyconsumer.handlers.StoppableLoopedHandler
import kotlin.math.abs

class MultiplicationCalculationsProvider(private val calculationsCallback: CalculationsCallback, factor: Int) :
    CalculationsProvider {

    private val calculationsFactor: Int

    init {
        if (listOf(-1, 0, 1).contains(factor)) throw IllegalArgumentException("Factor cannot be equal to -1, 0 or 1")
        calculationsFactor = factor
    }

    override fun calculationsTask(handlerId: Int, stoppableHandler: StoppableLoopedHandler) {
        var variable = 1
        while (true) {
            variable *= calculationsFactor
            if (abs(variable) > 100000000) {
                Log.d("ABAB", "handlerId: $handlerId variable: $variable")
                if (stoppableHandler.isHandlerStopped().not()) {
                    calculationsCallback.onThresholdAchieved(variable, handlerId)
                }
                break
            }
        }
    }
}