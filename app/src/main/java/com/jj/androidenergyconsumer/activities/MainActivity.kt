package com.jj.androidenergyconsumer.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.jj.androidenergyconsumer.databinding.ActivityMainBinding
import com.jj.androidenergyconsumer.permissions.PermissionManager
import com.jj.androidenergyconsumer.permissions.PermissionManager.Companion.WRITE_EXTERNAL_STORAGE_PERMISSION
import com.jj.androidenergyconsumer.permissions.PermissionManager.Companion.WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE

class MainActivity : AppCompatActivity() {

    private lateinit var activityMainBinding: ActivityMainBinding

    private val permissionManager = PermissionManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        handleWriteStoragePermission()
    }

    private fun handleWriteStoragePermission() {
        if (!permissionManager.isWriteExternalStoragePermissionGranted(this)
            && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE_PERMISSION),
                    WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE)
        }
    }
}