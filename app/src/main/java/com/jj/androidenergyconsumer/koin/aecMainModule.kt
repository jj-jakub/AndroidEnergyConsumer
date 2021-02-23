package com.jj.androidenergyconsumer.koin

import com.jj.androidenergyconsumer.AECApplication
import com.jj.androidenergyconsumer.notification.NotificationContainer
import org.koin.dsl.module

val aecMainModule = module {
    single { NotificationContainer(AECApplication.instance.applicationContext) }

}