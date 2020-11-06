package com.jj.androidenergyconsumer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class GPSLauncherFragment: Fragment() {

    companion object {
        fun newInstance(): GPSLauncherFragment = GPSLauncherFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_gps_launcher, container, false)
}