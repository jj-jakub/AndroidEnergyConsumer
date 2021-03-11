package com.jj.androidenergyconsumer.handlers

import android.os.HandlerThread

class StoppableLoopedThread {

    private var handlerThread: HandlerThread? = null
    private var stoppableHandler: StoppableLoopedHandler? = null

    fun restartThread() {
        stopThread()
        startThread()
    }

    fun stopThread() {
        stoppableHandler?.quitHandler()
        handlerThread?.apply {
            looper?.quit()
            quit()
        }
    }

    private fun startThread() {
        handlerThread = HandlerThread("InternetThread").apply {
            start()
            stoppableHandler = StoppableLoopedHandler(looper)
        }
    }

    fun executeInInfiniteLoop(function: () -> Unit, periodMs: Long) {
        stoppableHandler?.executeInInfiniteLoop(function, periodMs)
    }

    fun post(function: () -> Unit) {
        stoppableHandler?.post(function)
    }
}