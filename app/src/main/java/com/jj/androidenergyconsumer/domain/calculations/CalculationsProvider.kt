package com.jj.androidenergyconsumer.domain.calculations

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

interface CalculationsProvider {

    var calculationsAborted: Boolean
    val calculationsResultFlow: MutableSharedFlow<CalculationsResult>

    fun observeCalculationsResult(): SharedFlow<CalculationsResult> = calculationsResultFlow

    fun startCalculationsTask(threadId: Int)

    fun abortCalculationsTask() {
        calculationsAborted = true
    }
}