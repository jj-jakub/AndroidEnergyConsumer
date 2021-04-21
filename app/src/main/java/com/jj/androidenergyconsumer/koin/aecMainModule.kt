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
import com.jj.androidenergyconsumer.data.rest.InternetPingCallManager
import com.jj.androidenergyconsumer.data.rest.RetrofitClientFactory
import com.jj.androidenergyconsumer.domain.calculations.CalculationsOrchestrator
import com.jj.androidenergyconsumer.domain.calculations.CalculationsProviderFactory
import com.jj.androidenergyconsumer.domain.coroutines.CoroutineScopeProvider
import com.jj.androidenergyconsumer.domain.coroutines.ICoroutineScopeProvider
import com.jj.androidenergyconsumer.domain.internet.FileDownloader
import com.jj.androidenergyconsumer.domain.internet.InternetPingsCreator
import org.koin.dsl.module

val aecMainModule = module {
    single { NotificationContainer(get(), get()) }
    single { WakelockManager(get()) }

    single { BluetoothScanner(BluetoothBroadcastReceiver(get())) }

    single { CustomLocationListener() }

    single { FileDownloader(get()) }

    single<ICoroutineScopeProvider> { CoroutineScopeProvider() }

    single { RetrofitClientFactory() }
    single { InternetPingCallManager(get()) }
    single { InternetPingsCreator(get()) }

    single { CalculationsProviderFactory() }
    single { CalculationsOrchestrator(get(), get()) }

    single { PermissionManager(get()) }
    single { SystemVersionChecker() }
    single { BatterySettingsLauncher(get(), get()) }

    single { SystemServicesProvider() }
}