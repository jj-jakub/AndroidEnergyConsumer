package com.jj.androidenergyconsumer.domain.calculations

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

interface CalculationsProvider {
    val calculationsResultFlow: MutableSharedFlow<CalculationsResult>
    fun observeCalculationsResult(): SharedFlow<CalculationsResult> = calculationsResultFlow

    fun calculationsTask(threadId: Int)
}