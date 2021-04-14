package com.jj.androidenergyconsumer.app.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.jj.androidenergyconsumer.app.permissions.PermissionManager
import com.jj.androidenergyconsumer.app.permissions.PermissionManager.Companion.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
import com.jj.androidenergyconsumer.app.permissions.PermissionManager.Companion.FINE_LOCATION_PERMISSION_REQUEST_CODE
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseLauncherFragment : Fragment() {

    private val permissionManager = PermissionManager()
    protected var serviceBound = AtomicBoolean(false)
    protected abstract val serviceConnection: ServiceConnection
    protected abstract val activityTitle: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.title = activityTitle
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindFromService()
    }

    protected fun unbindFromService() {
        if (serviceBound.compareAndSet(true, false)) {
            context?.unbindService(serviceConnection)
        }
    }

    protected fun manageLocationPermission() {
        activity?.let { activity ->
            if (permissionManager.areLocationsPermissionGranted(activity)) {
                onAllLocationPermissionsGranted()
            } else {
                onPermissionsNotGranted()
                val finePermissionGranted = permissionManager.isFineLocationPermissionGranted(activity)
                val isAndroid10OrAbove = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

                @SuppressLint("NewApi")
                if (finePermissionGranted && isAndroid10OrAbove) {
                    showBackgroundPermissionAbbreviationDialog()
                } else if (!finePermissionGranted) {
                    permissionManager.requestFineLocationPermission(this)
                }
            }
        }
    }

    open fun onAllLocationPermissionsGranted() {
        /* no-op */
    }

    open fun onPermissionsNotGranted() {
        /* no-op */
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun showBackgroundPermissionAbbreviationDialog() {
        AlertDialog.Builder(requireContext()).setTitle("Background location").setMessage(
                "In next step select 'Allow all the time'. App needs this permission for bluetooth scans while it is in background.")
            .setPositiveButton("Next") { _, _ ->
                permissionManager.requestBackgroundLocationPermission(this)
            }.create().show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when {
            requestCode == FINE_LOCATION_PERMISSION_REQUEST_CODE && allResultsGranted(grantResults) -> {
                onFinePermissionGranted()
            }
            requestCode == BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE && allResultsGranted(grantResults) -> {
                onAllLocationPermissionsGranted()
            }
        }
    }

    private fun allResultsGranted(grantResults: IntArray) =
        grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }

    private fun onFinePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            showBackgroundPermissionAbbreviationDialog()
        } else {
            onAllLocationPermissionsGranted()
        }
    }
}