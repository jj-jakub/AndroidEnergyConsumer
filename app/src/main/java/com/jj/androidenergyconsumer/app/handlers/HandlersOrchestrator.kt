package com.jj.androidenergyconsumer.app.handlers

import android.os.HandlerThread

class HandlersOrchestrator : ThreadsOrchestrator {

    private val blockLock = Any()

    private val handlerThreadName = "HThread"
    private var handlerThreads: List<HandlerThread> = listOf()
    private var stoppableHandlers: List<StoppableLoopedHandler> = listOf()

    override fun launchInThreadsInInfiniteLoop(threadsAmount: Int, task: (index: Int) -> Unit) {
        restartHandlers(threadsAmount)
        synchronized(blockLock) {
            stoppableHandlers.forEachIndexed { index, stoppableHandler ->
                stoppableHandler.executeInInfiniteLoop( { task(index) })
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

    private fun createListOfHandlers(handlerThreadsList: List<HandlerThread>): List<StoppableLoopedHandler> {
        val mutableListOfHandlers = mutableListOf<StoppableLoopedHandler>()
        handlerThreadsList.forEach { handlerThread ->
            handlerThread.looper.let { looper -> mutableListOfHandlers.add(StoppableLoopedHandler(looper)) }
        }
        return mutableListOfHandlers.toList()
    }

    override fun abortThreads() {
        disposeHandlers()
    }
}