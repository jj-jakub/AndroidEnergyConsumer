package com.jj.androidenergyconsumer.calculations

import com.jj.androidenergyconsumer.handlers.StoppableLoopedHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

interface CalculationsProvider {
    val calculationsResultFlow: MutableSharedFlow<CalculationsResult>
    fun observeCalculationsResult(): SharedFlow<CalculationsResult> = calculationsResultFlow

    fun calculationsTask(handlerId: Int, stoppableHandler: StoppableLoopedHandler)
}