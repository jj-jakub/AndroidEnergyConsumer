package com.jj.androidenergyconsumer.calculations

import com.jj.androidenergyconsumer.handlers.StoppableHandler

interface CalculationsProvider {
    fun calculationsTask(handlerId: Int, stoppableHandler: StoppableHandler)
}