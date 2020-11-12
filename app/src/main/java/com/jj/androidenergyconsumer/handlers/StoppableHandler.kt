package com.jj.androidenergyconsumer.handlers

import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

class StoppableHandler(looper: Looper) : Handler(looper) {

    private val handlerStopped = AtomicBoolean(false)

    fun isHandlerStopped() = handlerStopped.get()

    fun executeInInfiniteLoop(runnable: () -> Unit) {
        handlerStopped.set(false)
        firstExecution(runnable)
    }

    private fun firstExecution(runnable: () -> Unit) {
        if (handlerStopped.get().not()) {
            post { executeAndPost(runnable) }
        }
    }

    private fun executeAndPost(runnable: () -> Unit) {
        if (handlerStopped.get().not()) {
            runnable.invoke()
            post { executeAndPost(runnable) }
        }
    }

    fun quitHandler() {
        handlerStopped.set(true)
        removeCallbacksAndMessages(null)
    }
}