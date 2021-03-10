package com.jj.androidenergyconsumer.koin

import com.jj.androidenergyconsumer.bluetooth.BluetoothBroadcastReceiver
import com.jj.androidenergyconsumer.bluetooth.BluetoothScanner
import com.jj.androidenergyconsumer.gps.CustomLocationListener
import com.jj.androidenergyconsumer.internet.FileDownloader
import com.jj.androidenergyconsumer.notification.NotificationContainer
import com.jj.androidenergyconsumer.utils.FileManager
import com.jj.androidenergyconsumer.wakelock.WakelockManager
import org.koin.dsl.module

val aecMainModule = module {
    single { NotificationContainer(get()) }
    single { WakelockManager(get()) }

    single { BluetoothScanner(BluetoothBroadcastReceiver(get())) }

    single { CustomLocationListener() }

    single { FileDownloader() }
    single { FileManager() }
}