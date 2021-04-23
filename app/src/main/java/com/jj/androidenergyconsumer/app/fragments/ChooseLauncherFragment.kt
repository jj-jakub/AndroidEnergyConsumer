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

    private var navController: NavController? = null

    private var fragmentChooseLauncherBinding: FragmentChooseLauncherBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        FragmentChooseLauncherBinding.inflate(inflater, container, false).let { binding ->
            fragmentChooseLauncherBinding = binding
            binding.root
        }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentChooseLauncherBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setActivityTitle()
        setCommitHashLabel()
        fragmentChooseLauncherBinding?.root?.let { navController = Navigation.findNavController(it) }
        setButtonsListeners()
    }

    private fun setActivityTitle() {
        activity?.title = getString(R.string.app_name)
    }

    private fun setCommitHashLabel() {
        val labelText = "Revision: ${BuildConfig.currentRevisionHash}, Build number: ${BuildConfig.ciBuildNumber}, " +
                "Version: ${BuildConfig.VERSION_NAME}" + if (BuildConfig.DEBUG) ", Debug" else ""

        fragmentChooseLauncherBinding?.appVersionInfoLabel?.text = labelText
    }

    private fun setButtonsListeners() {
        fragmentChooseLauncherBinding?.apply {
            gpsModuleLauncherButton.setOnClickListener {
                navController?.navigate(R.id.action_chooseLauncherFragment_to_GPSLauncherFragment)
            }
            internetModuleLauncherButton.setOnClickListener {
                navController?.navigate(R.id.action_chooseLauncherFragment_to_internetLauncherFragment)
            }
            calculationsButton.setOnClickListener {
                navController?.navigate(R.id.action_chooseLauncherFragment_to_calculationsFragment)
            }
            bluetoothModuleLauncherButton.setOnClickListener {
                navController?.navigate(R.id.action_chooseLauncherFragment_to_bluetoothLauncherFragment)
            }
        }
    }
}