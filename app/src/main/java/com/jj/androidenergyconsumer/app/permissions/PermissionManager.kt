package com.jj.androidenergyconsumer.app.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.jj.androidenergyconsumer.app.utils.SystemVersionChecker

class PermissionManager(private val systemVersionChecker: SystemVersionChecker) {

    companion object {
        const val FINE_LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION

        @RequiresApi(Build.VERSION_CODES.Q)
        const val BACKGROUND_LOCATION_PERMISSION = Manifest.permission.ACCESS_BACKGROUND_LOCATION
        const val WRITE_EXTERNAL_STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE
        const val FINE_LOCATION_PERMISSION_REQUEST_CODE = 1000
        const val BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE = 1002
        const val WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE = 1003
    }

    fun areLocationsPermissionGranted(context: Context): Boolean =
        isFineLocationPermissionGranted(context) && isBackgroundLocationPermissionGranted(context)

    fun isFineLocationPermissionGranted(context: Context): Boolean = checkPermission(context, FINE_LOCATION_PERMISSION)

    private fun isBackgroundLocationPermissionGranted(context: Context): Boolean =
        if (systemVersionChecker.isAndroid10OrAbove()) {
            checkPermission(context, BACKGROUND_LOCATION_PERMISSION)
        } else true

    private fun isWriteExternalStoragePermissionGranted(context: Context): Boolean =
        checkPermission(context, WRITE_EXTERNAL_STORAGE_PERMISSION)

    private fun checkPermission(context: Context, permission: String) =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun requestFineLocationPermission(fragment: Fragment) {
        fragment.requestPermissions(arrayOf(FINE_LOCATION_PERMISSION), FINE_LOCATION_PERMISSION_REQUEST_CODE)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun requestBackgroundLocationPermission(fragment: Fragment) {
        Log.d("ABAB", "Request BACKGROUND_LOCATION_PERMISSION")
        fragment.requestPermissions(arrayOf(BACKGROUND_LOCATION_PERMISSION),
                BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE)
    }

    fun requestWriteExternalStoragePermission(activity: Activity) {
        if (!isWriteExternalStoragePermissionGranted(activity) && systemVersionChecker.isAndroid6OrAbove()) {
            activity.requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE_PERMISSION),
                    WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
        }
    }
}