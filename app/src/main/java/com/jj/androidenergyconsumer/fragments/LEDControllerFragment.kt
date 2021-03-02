package com.jj.androidenergyconsumer.fragments

import android.content.ServiceConnection
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.jj.androidenergyconsumer.databinding.FragmentLedControllerBinding
import com.jj.androidenergyconsumer.utils.showShortToast
import com.jj.androidenergyconsumer.viewmodels.LedControllerViewModel

enum class AvailableLedColors {
    RED, GREEN, BLUE, YELLOW, WHITE, PURPLE, CYAN
}

class LEDControllerFragment : BaseLauncherFragment() {

    override val activityTitle: String = "LED Controller"
    override val serviceConnection: ServiceConnection
        get() = TODO("Not yet implemented")

    private lateinit var fragmentLedControllerBinding: FragmentLedControllerBinding

    private val ledControllerViewModel: LedControllerViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentLedControllerBinding = FragmentLedControllerBinding.inflate(inflater, container, false)
        return fragmentLedControllerBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupButtonListeners()
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

            sendBrightnessButton.setOnClickListener { switchLedsBrightness() }
        }
    }

    private fun switchLeds(color: AvailableLedColors) {
        ledControllerViewModel.sendLedRequest(color)
    }

    private fun switchLedsBrightness() {
        getBrightnessFromInput()?.let { brightness ->
            getUrlFromInput()?.let { url ->
                ledControllerViewModel.sendBrightnessRequest(brightness, url)
            }
        }
    }

    private fun getBrightnessFromInput(): Int? = try {
        fragmentLedControllerBinding.brightnessField.text.toString().toInt()
    } catch (e: Exception) {
        Log.e(tag, "Exception while getting brightness from input")
        onBrightnessInputError()
        null
    }

    private fun onBrightnessInputError() {
        onInputError("Wrong brightness input")
    }


    private fun getUrlFromInput(): String? =
        try {
            fragmentLedControllerBinding.ledNodeIpField.text.toString()
        } catch (e: Exception) {
            Log.e(tag, "Exception while converting input url", e)
            onUrlInputError()
            null
        }

    private fun onUrlInputError() {
        onInputError("Wrong Url input")
    }

    @Suppress("SameParameterValue")
    private fun onInputError(message: String) {
        showShortToast(message)
    }
}