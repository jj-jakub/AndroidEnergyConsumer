package com.jj.androidenergyconsumer.koin

import com.jj.androidenergyconsumer.notification.NotificationContainer
import com.jj.androidenergyconsumer.wakelock.WakelockManager
import org.koin.dsl.module

val aecMainModule = module {
    single { NotificationContainer(get()) }
    single { WakelockManager(get()) }
}