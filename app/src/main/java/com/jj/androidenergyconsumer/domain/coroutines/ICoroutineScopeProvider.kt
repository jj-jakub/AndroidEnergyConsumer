package com.jj.androidenergyconsumer.domain.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

interface ICoroutineScopeProvider {
    fun getMain(): CoroutineScope
    fun getIO(): CoroutineScope
    fun getIODispatcher(): CoroutineContext
}