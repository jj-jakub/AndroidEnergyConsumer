package com.jj.androidenergyconsumer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jj.androidenergyconsumer.activities.MainActivity
import com.jj.androidenergyconsumer.databinding.FragmentChooseLauncherBinding

class ChooseLauncherFragment : Fragment() {

    companion object {
        fun newInstance(): ChooseLauncherFragment = ChooseLauncherFragment()
    }

    private lateinit var fragmentChooseLauncherBinding: FragmentChooseLauncherBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentChooseLauncherBinding = FragmentChooseLauncherBinding.inflate(inflater, container, false)
        return fragmentChooseLauncherBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonsListeners()
    }

    private fun setButtonsListeners() {
        fragmentChooseLauncherBinding.apply {
            gpsModuleLauncherButton.setOnClickListener { switchFragment(GPSLauncherFragment.newInstance()) }
            internetModuleLauncherButton.setOnClickListener { switchFragment(InternetLauncherFragment.newInstance()) }
            calculationsButton.setOnClickListener { switchFragment(CalculationsFragment.newInstance()) }
            bluetoothModuleLauncherButton.setOnClickListener { switchFragment(BluetoothLauncherFragment.newInstance()) }
        }
    }

    private fun switchFragment(fragment: Fragment) = (activity as MainActivity?)?.switchFragment(fragment)
}