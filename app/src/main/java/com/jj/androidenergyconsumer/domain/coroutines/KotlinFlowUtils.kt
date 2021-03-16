package com.jj.androidenergyconsumer.utils

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow

@Suppress("FunctionName")
fun <T> BufferedMutableSharedFlow(replay: Int = 1, extraBufferCapacity: Int = 1,
                                  onBufferOverflow: BufferOverflow = BufferOverflow.DROP_OLDEST): MutableSharedFlow<T> =
    MutableSharedFlow(replay, extraBufferCapacity, onBufferOverflow)