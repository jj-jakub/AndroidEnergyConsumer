package com.jj.androidenergyconsumer.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.fragments.ChooseLauncherFragment
import com.jj.androidenergyconsumer.permissions.PermissionManager
import com.jj.androidenergyconsumer.permissions.PermissionManager.Companion.WRITE_EXTERNAL_STORAGE_PERMISSION
import com.jj.androidenergyconsumer.permissions.PermissionManager.Companion.WRITE_EXTERNAL_STORAGE_PERMISSION_REQUEST_CODE

class MainActivity : AppCompatActivity() {

    private val permissionManager = PermissionManager()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setCurrentFragment(savedInstanceState)

        permissionManager.requestWriteExternalStoragePermission(this)
    }

    private fun setCurrentFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val fragment = ChooseLauncherFragment.newInstance()
            switchFragment(fragment)
        }
    }

    fun switchFragment(fragment: Fragment) {
        val tag: String = fragment::class.java.simpleName
        supportFragmentManager.beginTransaction().replace(R.id.mainFragmentContainer, fragment, tag)
            .addToBackStack(null).commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0) {
            super.onBackPressed()
        } else {
            supportFragmentManager.popBackStack()
        }
    }
}