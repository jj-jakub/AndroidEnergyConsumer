package com.jj.androidenergyconsumer.fragments

import android.content.ServiceConnection
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.jj.androidenergyconsumer.databinding.FragmentLedControllerBinding
import com.jj.androidenergyconsumer.utils.showShortToast
import com.jj.androidenergyconsumer.viewmodels.LedControllerViewModel
import com.jj.androidenergyconsumer.viewmodels.ViewModelFactory
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject

enum class AvailableLedColors {
    RED, GREEN, BLUE, YELLOW, WHITE, PURPLE, CYAN, RAINBOW
}

class LEDControllerFragment : BaseLauncherFragment() {

    override val activityTitle: String = "LED Controller"
    override val serviceConnection: ServiceConnection
        get() = TODO("Not yet implemented")

    private lateinit var fragmentLedControllerBinding: FragmentLedControllerBinding

    private val viewModelFactory: ViewModelFactory by inject()
    private val ledControllerViewModel: LedControllerViewModel by viewModels { viewModelFactory }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentLedControllerBinding = FragmentLedControllerBinding.inflate(inflater, container, false)
        return fragmentLedControllerBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeErrors()
        setupButtonListeners()
    }

    private fun observeErrors() {
        with(lifecycleScope) {
            launchWhenResumed { ledControllerViewModel.observeErrorMessage().collect { onInputError(it) } }
        }
    }

    private fun setupButtonListeners() {
        with(fragmentLedControllerBinding) {
            redLedButton.setOnClickListener { switchLeds(AvailableLedColors.RED) }
            greenLedButton.setOnClickListener { switchLeds(AvailableLedColors.GREEN) }
            blueLedButton.setOnClickListener { switchLeds(AvailableLedColors.BLUE) }
            yellowLedButton.setOnClickListener { switchLeds(AvailableLedColors.YELLOW) }
            whiteLedButton.setOnClickListener { switchLeds(AvailableLedColors.WHITE) }
            purpleLedButton.setOnClickListener { switchLeds(AvailableLedColors.PURPLE) }
            cyanLedButton.setOnClickListener { switchLeds(AvailableLedColors.CYAN) }
            rainbowLedButton.setOnClickListener { switchLeds(AvailableLedColors.RAINBOW) }

            sendBrightnessButton.setOnClickListener { switchLedsBrightness() }
            sendRainbowSpeedButton.setOnClickListener { switchRainbowSpeed() }
        }
    }

    private fun switchLeds(color: AvailableLedColors) {
        getIpFromInput()?.let { ip ->
            ledControllerViewModel.sendLedRequest(color, ip)
        }
    }

    private fun switchLedsBrightness() {
        getBrightnessOrSpeedValueFromInput()?.let { brightness ->
            getIpFromInput()?.let { ip ->
                ledControllerViewModel.sendBrightnessRequest(brightness, ip)
            }
        }
    }

    private fun getBrightnessOrSpeedValueFromInput(): Int? = try {
        fragmentLedControllerBinding.brightnessOrSpeedField.text.toString().toInt()
    } catch (e: Exception) {
        Log.e(tag, "Exception while getting brightness or speed from input")
        onBrightnessOrSpeedInputError()
        null
    }

    private fun onBrightnessOrSpeedInputError() {
        onInputError("Wrong brightness or speed input")
    }

    private fun switchRainbowSpeed() {
        getBrightnessOrSpeedValueFromInput()?.let { rainbowSpeed ->
            getIpFromInput()?.let { ip ->
                ledControllerViewModel.sendRainbowSpeedRequest(rainbowSpeed, ip)
            }
        }
    }

    private fun getIpFromInput(): String? =
        try {
            fragmentLedControllerBinding.ledNodeIpField.text.toString()
        } catch (e: Exception) {
            Log.e(tag, "Exception while converting input IP", e)
            onIpInputError()
            null
        }

    private fun onIpInputError() {
        onInputError("Wrong IP input")
    }

    @Suppress("SameParameterValue")
    private fun onInputError(message: String) {
        showShortToast(message)
    }
}