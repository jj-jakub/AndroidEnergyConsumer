package com.jj.androidenergyconsumer.app.fragments

import android.annotation.SuppressLint
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.jj.androidenergyconsumer.app.permissions.BackgroundPermissionAbbreviationDialog
import com.jj.androidenergyconsumer.app.permissions.PermissionManager
import com.jj.androidenergyconsumer.app.permissions.PermissionManager.Companion.BACKGROUND_LOCATION_PERMISSION_REQUEST_CODE
import com.jj.androidenergyconsumer.app.permissions.PermissionManager.Companion.FINE_LOCATION_PERMISSION_REQUEST_CODE
import com.jj.androidenergyconsumer.app.utils.SystemVersionChecker
import org.koin.android.ext.android.inject
import java.util.concurrent.atomic.AtomicBoolean

abstract class BaseLauncherFragment : Fragment() {

    private val permissionManager: PermissionManager by inject()
    private val systemVersionChecker: SystemVersionChecker by inject()

    protected var serviceBound = AtomicBoolean(false)
    protected abstract val serviceConnection: ServiceConnection
    protected abstract val activityTitle: String

    private val backgroundPermissionAbbreviationDialog =
        if (systemVersionChecker.isAndroid10OrAbove()) BackgroundPermissionAbbreviationDialog() else null

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

                @SuppressLint("NewApi")
                if (!finePermissionGranted) {
                    permissionManager.requestFineLocationPermission(this)
                } else if (finePermissionGranted && systemVersionChecker.isAndroid10OrAbove()) {
                    backgroundPermissionAbbreviationDialog?.show(this, permissionManager)
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
        if (systemVersionChecker.isAndroid10OrAbove()) {
            backgroundPermissionAbbreviationDialog?.show(this, permissionManager)
        } else onAllLocationPermissionsGranted()
    }
}