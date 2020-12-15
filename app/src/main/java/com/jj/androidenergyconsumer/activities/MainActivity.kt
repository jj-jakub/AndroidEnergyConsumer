package com.jj.androidenergyconsumer.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jj.androidenergyconsumer.databinding.ActivityMainBinding
import com.jj.androidenergyconsumer.permissions.PermissionManager
import com.jj.androidenergyconsumer.utils.BatterySettingsLauncher

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding

    private val permissionManager = PermissionManager()
    private lateinit var batterySettingsLauncher: BatterySettingsLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        permissionManager.requestWriteExternalStoragePermission(this)
        batterySettingsLauncher = BatterySettingsLauncher(this)

        manageBatteryOptimizationsSettings()
    }

    private fun manageBatteryOptimizationsSettings() {
        if (!batterySettingsLauncher.isAppIgnoringBatteryOptimizations()) {
            batterySettingsLauncher.requestToIgnoreBatteryOptimizations()
        }
    }
}