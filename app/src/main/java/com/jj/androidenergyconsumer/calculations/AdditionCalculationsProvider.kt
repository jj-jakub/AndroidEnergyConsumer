package com.jj.androidenergyconsumer.calculations

import android.util.Log
import com.jj.androidenergyconsumer.handlers.StoppableHandler

class AdditionCalculationsProvider(private val calculationsCallback: CalculationsCallback) : CalculationsProvider {

    override fun calculationsTask(handlerId: Int, stoppableHandler: StoppableHandler) {
        var variable = 0
        while (true) {
            variable += 2
            if (variable % 100000000 == 0) {
                Log.d("ABAB", "handlerId: $handlerId variable: $variable")
                if (stoppableHandler.isHandlerStopped().not()) {
                    calculationsCallback.onThresholdAchieved(variable, handlerId)
                }
                break
            }
        }
    }
}