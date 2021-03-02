package com.jj.androidenergyconsumer.fragments

import android.content.ServiceConnection
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jj.androidenergyconsumer.databinding.FragmentLedControllerBinding

class LEDControllerFragment: BaseLauncherFragment() {

    override val serviceConnection: ServiceConnection
        get() = TODO("Not yet implemented")

    private lateinit var fragmentLedControllerBinding: FragmentLedControllerBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentLedControllerBinding = FragmentLedControllerBinding.inflate(inflater, container, false)
        return fragmentLedControllerBinding.root
    }
}