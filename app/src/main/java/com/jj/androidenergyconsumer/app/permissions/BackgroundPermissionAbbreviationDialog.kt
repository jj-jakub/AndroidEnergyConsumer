package com.jj.androidenergyconsumer.app.permissions

import android.app.AlertDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment

class BackgroundPermissionAbbreviationDialog {

    @RequiresApi(Build.VERSION_CODES.Q)
    fun show(fragment: Fragment, permissionManager: PermissionManager) {
        AlertDialog.Builder(fragment.requireContext()).setTitle("Background location")
            .setMessage("In next step select 'Allow all the time'. " +
                    "App needs this permission for bluetooth scans while it is in background.")
            .setPositiveButton("Next") { _, _ ->
                permissionManager.requestBackgroundLocationPermission(fragment)
            }.create().show()
    }
}