package com.jj.androidenergyconsumer

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setCurrentFragment(savedInstanceState)
    }

    private fun setCurrentFragment(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            val fragment = ChooseLauncherFragment()
            switchFragment(fragment)
        }
    }

    private fun switchFragment(fragment: Fragment) {
        val tag: String = fragment::class.java.simpleName
        supportFragmentManager.beginTransaction().replace(R.id.mainFragmentContainer, fragment, tag).commit()
    }
}