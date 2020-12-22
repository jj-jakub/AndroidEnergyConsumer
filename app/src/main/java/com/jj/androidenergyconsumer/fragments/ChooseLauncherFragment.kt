package com.jj.androidenergyconsumer.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.activities.MainActivity
import com.jj.androidenergyconsumer.workrequests.WorkScheduler
import kotlinx.android.synthetic.main.fragment_choose_launcher.*

class ChooseLauncherFragment : Fragment() {

    companion object {
        fun newInstance(): ChooseLauncherFragment = ChooseLauncherFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_choose_launcher, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonsListeners()
    }

    private fun setButtonsListeners() {
        gpsModuleLauncherButton?.setOnClickListener { switchFragment(GPSLauncherFragment.newInstance()) }
        internetModuleLauncherButton?.setOnClickListener { switchFragment(InternetLauncherFragment.newInstance()) }
        calculationsButton?.setOnClickListener { switchFragment(CalculationsFragment.newInstance()) }
        bluetoothModuleLauncherButton?.setOnClickListener { switchFragment(BluetoothLauncherFragment.newInstance()) }
        sensorsModuleLauncherButton?.setOnClickListener {
            WorkScheduler(context!!).startSimpleLogJob()
            Log.d(tag, "scheduled")
        }
    }

    private fun switchFragment(fragment: Fragment) = (activity as MainActivity?)?.switchFragment(fragment)
}