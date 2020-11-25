package com.jj.androidenergyconsumer.calculations

import com.jj.androidenergyconsumer.handlers.StoppableLoopedHandler

interface CalculationsProvider {
    fun calculationsTask(handlerId: Int, stoppableHandler: StoppableLoopedHandler)
}