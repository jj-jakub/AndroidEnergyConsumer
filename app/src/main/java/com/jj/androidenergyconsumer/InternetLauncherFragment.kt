package com.jj.androidenergyconsumer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class InternetLauncherFragment: Fragment() {

    companion object {
        fun newInstance(): InternetLauncherFragment = InternetLauncherFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_internet_launcher, container, false)
}