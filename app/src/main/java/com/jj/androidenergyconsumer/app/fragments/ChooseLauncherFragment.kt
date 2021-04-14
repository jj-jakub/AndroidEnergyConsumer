package com.jj.androidenergyconsumer.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.jj.androidenergyconsumer.BuildConfig
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.databinding.FragmentChooseLauncherBinding

class ChooseLauncherFragment : Fragment() {

    private lateinit var navController: NavController
    private lateinit var fragmentChooseLauncherBinding: FragmentChooseLauncherBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentChooseLauncherBinding = FragmentChooseLauncherBinding.inflate(inflater, container, false)
        return fragmentChooseLauncherBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActivityTitle()
        setCommitHashLabel()
        navController = Navigation.findNavController(fragmentChooseLauncherBinding.root)
        setButtonsListeners()
    }

    private fun setActivityTitle() {
        activity?.title = getString(R.string.app_name)
    }

    private fun setCommitHashLabel() {
        val labelText = "Revision: ${BuildConfig.currentRevisionHash}"
        fragmentChooseLauncherBinding.currentRevisionHashLabel.text = labelText
    }

    private fun setButtonsListeners() {
        fragmentChooseLauncherBinding.apply {
            gpsModuleLauncherButton.setOnClickListener {
                navController.navigate(R.id.action_chooseLauncherFragment_to_GPSLauncherFragment)
            }
            internetModuleLauncherButton.setOnClickListener {
                navController.navigate(R.id.action_chooseLauncherFragment_to_internetLauncherFragment)
            }
            calculationsButton.setOnClickListener {
                navController.navigate(R.id.action_chooseLauncherFragment_to_calculationsFragment)
            }
            bluetoothModuleLauncherButton.setOnClickListener {
                navController.navigate(R.id.action_chooseLauncherFragment_to_bluetoothLauncherFragment)
            }
        }
    }
}