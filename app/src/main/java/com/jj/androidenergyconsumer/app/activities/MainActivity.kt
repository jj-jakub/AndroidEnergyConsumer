package com.jj.androidenergyconsumer.app.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jj.androidenergyconsumer.app.permissions.PermissionManager
import com.jj.androidenergyconsumer.app.utils.BatterySettingsLauncher
import com.jj.androidenergyconsumer.databinding.ActivityMainBinding

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
        batterySettingsLauncher.apply {
            if (!isAppIgnoringBatteryOptimizations()) requestToIgnoreBatteryOptimizations()
        }
    }
}