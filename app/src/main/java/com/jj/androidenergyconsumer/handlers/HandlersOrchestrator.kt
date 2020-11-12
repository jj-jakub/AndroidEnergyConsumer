package com.jj.androidenergyconsumer.handlers

import android.os.HandlerThread
import com.jj.androidenergyconsumer.calculations.CalculationsProvider

class HandlersOrchestrator {

    private val blockLock = Any()

    private val handlerThreadName = "HThread"
    private var handlerThreads: List<HandlerThread> = listOf()
    private var stoppableHandlers: List<StoppableHandler> = listOf()

    fun launchInEveryHandlerInInfiniteLoop(amountOfHandlers: Int, calculationsProvider: CalculationsProvider) {
        restartHandlers(amountOfHandlers)
        synchronized(blockLock) {
            stoppableHandlers.forEachIndexed { index, stoppableHandler ->
                stoppableHandler.executeInInfiniteLoop {
                    calculationsProvider.calculationsTask(index, stoppableHandler)
                }
            }
        }
    }

    private fun restartHandlers(amountOfHandlers: Int) {
        disposeHandlers()
        initHandlers(amountOfHandlers)
    }

    private fun disposeHandlers() {
        synchronized(blockLock) {
            stoppableHandlers.forEach { it.quitHandler() }
            handlerThreads.forEach { handlerThread ->
                handlerThread.looper.quit()
                handlerThread.quit()
            }
            stoppableHandlers = listOf()
            handlerThreads = listOf()
        }
    }

    private fun initHandlers(amountOfHandlers: Int) {
        synchronized(blockLock) {
            handlerThreads = createListOfHandlerThreads(amountOfHandlers, handlerThreadName).apply {
                forEach { it.start() }
                stoppableHandlers = createListOfHandlers(this)
            }
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

    private fun createListOfHandlers(handlerThreadsList: List<HandlerThread>): List<StoppableHandler> {
        val mutableListOfHandlers = mutableListOf<StoppableHandler>()
        handlerThreadsList.forEach { handlerThread ->
            handlerThread.looper.let { looper -> mutableListOfHandlers.add(StoppableHandler(looper)) }
        }
        return mutableListOfHandlers.toList()
    }

    fun abortHandlers() {
        disposeHandlers()
    }
}