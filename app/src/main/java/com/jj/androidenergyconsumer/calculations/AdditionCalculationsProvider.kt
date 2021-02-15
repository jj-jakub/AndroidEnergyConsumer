package com.jj.androidenergyconsumer.calculations

import android.util.Log
import com.jj.androidenergyconsumer.handlers.StoppableLoopedHandler
import kotlin.math.abs

class AdditionCalculationsProvider(private val calculationsCallback: CalculationsCallback, factor: Int) :
    CalculationsProvider {

    private val calculationsFactor: Int

    init {
        if (factor == 0) throw IllegalArgumentException("Factor cannot be equal to 0")
        calculationsFactor = factor
    }

    override fun calculationsTask(handlerId: Int, stoppableHandler: StoppableLoopedHandler) {
        var variable = 0
        while (true) {
            variable += calculationsFactor
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