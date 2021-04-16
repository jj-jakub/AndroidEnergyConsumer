package com.jj.androidenergyconsumer.app.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jj.androidenergyconsumer.app.permissions.PermissionManager
import com.jj.androidenergyconsumer.app.utils.BatterySettingsLauncher
import com.jj.androidenergyconsumer.databinding.ActivityMainBinding
import org.koin.android.ext.android.inject

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding

    private val permissionManager: PermissionManager by inject()
    private val batterySettingsLauncher: BatterySettingsLauncher by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        permissionManager.requestWriteExternalStoragePermission(this)

        manageBatteryOptimizationsSettings()
    }

    private fun manageBatteryOptimizationsSettings() {
        batterySettingsLauncher.apply {
            if (!isAppIgnoringBatteryOptimizations()) requestToIgnoreBatteryOptimizations()
        }
    }
}