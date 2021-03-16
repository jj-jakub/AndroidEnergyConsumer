package com.jj.androidenergyconsumer.app.handlers

import android.os.Handler
import android.os.Looper
import androidx.core.os.postDelayed
import java.util.concurrent.atomic.AtomicBoolean

class StoppableLoopedHandler(looper: Looper) : Handler(looper) {

    private val handlerStopped = AtomicBoolean(false)

    fun isHandlerStopped() = handlerStopped.get()

    fun executeInInfiniteLoop(runnable: () -> Unit, delayMs: Long = 0) {
        handlerStopped.set(false)
        firstExecution(runnable, delayMs)
    }

    private fun firstExecution(runnable: () -> Unit, delayMs: Long) {
        if (handlerStopped.get().not()) {
            post { executeAndPostDelayed(runnable, delayMs) }
        }
    }

    private fun executeAndPostDelayed(runnable: () -> Unit, delayMs: Long) {
        if (handlerStopped.get().not()) {
            runnable.invoke()
            postDelayed(delayMs) { executeAndPostDelayed(runnable, delayMs) }
        }
    }

    fun quitHandler() {
        handlerStopped.set(true)
        removeCallbacksAndMessages(null)
    }
}