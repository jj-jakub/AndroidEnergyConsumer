package com.jj.androidenergyconsumer.app.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import com.jj.androidenergyconsumer.domain.tag

class BatterySettingsLauncher(private val context: Context) {

    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager?
    private val packageName = context.packageName

    /**
     * @return true if device is ignoring battery optimizations
     *         false if is not ignoring battery optimizations or state is unknown
     */
    fun isAppIgnoringBatteryOptimizations(): Boolean =
        if (isAndroid6OrHigher()) {
            powerManager?.isIgnoringBatteryOptimizations(packageName) == true
        } else {
            false
        }

    /**
     * @return true if successfully asked to ignore battery optimizations
     *         false if failed to request to ignore battery optimizations
     */
    fun requestToIgnoreBatteryOptimizations(): Boolean {
        return if (isAndroid6OrHigher()) {
            val intent = Intent().apply {
                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                data = Uri.parse("package:$packageName")
            }
            try {
                context.startActivity(intent)
                true
            } catch (e: Exception) {
                Log.e(tag, "Failed to request to ignore battery optimizations", e)
                false
            }
        } else false
    }
}