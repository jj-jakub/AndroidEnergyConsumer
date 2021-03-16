package com.jj.androidenergyconsumer.koin

import com.jj.androidenergyconsumer.app.bluetooth.BluetoothBroadcastReceiver
import com.jj.androidenergyconsumer.app.bluetooth.BluetoothScanner
import com.jj.androidenergyconsumer.app.gps.CustomLocationListener
import com.jj.androidenergyconsumer.app.notification.NotificationContainer
import com.jj.androidenergyconsumer.app.utils.FileManager
import com.jj.androidenergyconsumer.app.wakelock.WakelockManager
import com.jj.androidenergyconsumer.data.rest.InternetPingCallManager
import com.jj.androidenergyconsumer.domain.calculations.CalculationsOrchestrator
import com.jj.androidenergyconsumer.domain.calculations.CalculationsProviderFactory
import com.jj.androidenergyconsumer.domain.coroutines.CoroutineScopeProvider
import com.jj.androidenergyconsumer.domain.coroutines.ICoroutineScopeProvider
import com.jj.androidenergyconsumer.domain.internet.FileDownloader
import com.jj.androidenergyconsumer.domain.internet.InternetPingsCreator
import org.koin.dsl.module

val aecMainModule = module {
    single { NotificationContainer(get()) }
    single { WakelockManager(get()) }

    single { BluetoothScanner(BluetoothBroadcastReceiver(get())) }

    single { CustomLocationListener() }

    single { FileDownloader(get()) }
    single { FileManager() }

    single<ICoroutineScopeProvider> { CoroutineScopeProvider() }

    single { InternetPingCallManager() }
    single { InternetPingsCreator(get()) }

    single { CalculationsProviderFactory() }
    single { CalculationsOrchestrator(get(), get()) }
}