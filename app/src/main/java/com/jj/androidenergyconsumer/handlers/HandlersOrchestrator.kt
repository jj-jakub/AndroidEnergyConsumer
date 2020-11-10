package com.jj.androidenergyconsumer.handlers

import android.os.HandlerThread
import android.os.Looper
import com.jj.androidenergyconsumer.calculations.CalculationsProvider

class HandlersOrchestrator {

    private val blockLock = Any()

    private val handlerThreadName = "HThread"
    private var handlerThreads: List<HandlerThread>? = null
    private var loopers: List<Looper>? = null
    private var stoppableHandlers: List<StoppableHandler>? = null

    fun launchInEveryHandlerInInfiniteLoop(calculationsProvider: CalculationsProvider) {
        synchronized(blockLock) {
            stoppableHandlers?.forEachIndexed { index, stoppableHandler ->
                stoppableHandler.executeInInfiniteLoop {
                    calculationsProvider.calculationsTask(index, stoppableHandler)
                }
            }
        }
    }

    fun initHandlers(amountOfHandlers: Int) {
        synchronized(blockLock) {
            handlerThreads = createListOfHandlerThreads(amountOfHandlers, handlerThreadName).apply {
                forEach { it.start() }
                stoppableHandlers = createListOfHandlers(this)
            }
        }
    }

    fun disposeHandlers() {
        synchronized(blockLock) {
            stoppableHandlers?.forEach { it.quitHandler() }
            stoppableHandlers = null
            loopers?.forEach { it.quit() }
            loopers = null
            handlerThreads?.forEach { it.quit() }
            handlerThreads = null
        }
    }

    @Suppress("SameParameterValue")
    private fun createListOfHandlerThreads(amountOfHandlers: Int, handlerThreadName: String): List<HandlerThread> {
        val handlersList = mutableListOf<HandlerThread>()
        for (i in 0 until amountOfHandlers) {
            handlersList.add(HandlerThread("$handlerThreadName $i"))
        }
        return handlersList.toList()
    }

    private fun createListOfHandlers(handlerThreadsList: List<HandlerThread>): List<StoppableHandler>? {
        val mutableListOfHandlers = mutableListOf<StoppableHandler>()
        handlerThreadsList.forEach { handlerThread ->
            handlerThread.looper?.let { looper -> mutableListOfHandlers.add(StoppableHandler(looper)) }
        }
        return mutableListOfHandlers.toList()
    }
}