package com.jj.androidenergyconsumer.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class CoroutineScopeProvider {

    private val main: CoroutineScope = CoroutineScope(Dispatchers.Main)
    private val io: CoroutineScope = CoroutineScope(Dispatchers.IO)

    fun getMain(): CoroutineScope = main
    fun getIO(): CoroutineScope = io
}