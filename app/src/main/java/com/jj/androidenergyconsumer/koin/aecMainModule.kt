package com.jj.androidenergyconsumer.koin

import com.jj.androidenergyconsumer.app.bluetooth.BluetoothBroadcastReceiver
import com.jj.androidenergyconsumer.app.bluetooth.BluetoothScanner
import com.jj.androidenergyconsumer.app.gps.CustomLocationListener
import com.jj.androidenergyconsumer.app.notification.NotificationContainer
import com.jj.androidenergyconsumer.app.permissions.PermissionManager
import com.jj.androidenergyconsumer.app.utils.BatterySettingsLauncher
import com.jj.androidenergyconsumer.app.utils.SystemServicesProvider
import com.jj.androidenergyconsumer.app.utils.SystemVersionChecker
import com.jj.androidenergyconsumer.app.wakelock.WakelockManager
import com.jj.androidenergyconsumer.data.internet.FileDownloader
import com.jj.androidenergyconsumer.data.internet.InternetPingsCreator
import com.jj.androidenergyconsumer.data.rest.InternetPingCallManager
import com.jj.androidenergyconsumer.data.rest.RetrofitClientFactory
import com.jj.androidenergyconsumer.data.streams.BufferedInputStreamFactory
import com.jj.androidenergyconsumer.domain.calculations.CalculationsOrchestrator
import com.jj.androidenergyconsumer.domain.calculations.CalculationsProviderFactory
import com.jj.androidenergyconsumer.domain.coroutines.CoroutineJobContainerFactory
import com.jj.androidenergyconsumer.domain.coroutines.CoroutineScopeProvider
import com.jj.androidenergyconsumer.domain.coroutines.ICoroutineScopeProvider
import com.jj.androidenergyconsumer.domain.multithreading.CoroutinesOrchestrator
import com.jj.androidenergyconsumer.domain.multithreading.ThreadsOrchestrator
import org.koin.dsl.module

val aecMainModule = module {
    single { NotificationContainer(get(), get()) }
    single { WakelockManager(get()) }

    single { BluetoothScanner(BluetoothBroadcastReceiver(get())) }

    single { CustomLocationListener() }

    single { FileDownloader(get(), get()) }

    factory<ICoroutineScopeProvider> { CoroutineScopeProvider() }

    single { RetrofitClientFactory() }
    single { InternetPingCallManager(get()) }
    single { InternetPingsCreator(get(), get(), get()) }

    single { CalculationsProviderFactory() }
    single { CalculationsOrchestrator(get(), get(), coroutinesOrchestrator = get()) }

    single { PermissionManager(get()) }
    single { SystemVersionChecker() }
    single { BatterySettingsLauncher(get(), get()) }

    single { SystemServicesProvider() }
    single { CoroutineJobContainerFactory() }
    factory<ThreadsOrchestrator> { CoroutinesOrchestrator(get(), get()) }
    single { BufferedInputStreamFactory() }
}