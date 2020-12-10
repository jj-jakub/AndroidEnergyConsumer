package com.jj.androidenergyconsumer.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class PermissionManager {

    companion object {
        const val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
        const val WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        const val WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1001
    }

    fun isLocationPermissionGranted(context: Context): Boolean = checkPermission(context, LOCATION_PERMISSION)

    fun isWriteExternalStoragePermissionGranted(context: Context): Boolean =
        checkPermission(context, WRITE_EXTERNAL_STORAGE_PERMISSION)

    private fun checkPermission(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}