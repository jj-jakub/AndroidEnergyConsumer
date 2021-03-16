package com.jj.androidenergyconsumer.fragments

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.calculations.CalculationsOrchestrator
import com.jj.androidenergyconsumer.calculations.CalculationsResult
import com.jj.androidenergyconsumer.calculations.CalculationsType
import com.jj.androidenergyconsumer.databinding.FragmentCalculationsLauncherBinding
import com.jj.androidenergyconsumer.services.CalculationsService
import com.jj.androidenergyconsumer.services.CalculationsService.Companion.DEFAULT_CALCULATIONS_FACTOR
import com.jj.androidenergyconsumer.services.CalculationsService.Companion.DEFAULT_NUMBER_OF_HANDLERS
import com.jj.androidenergyconsumer.services.MyBinder
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import com.jj.androidenergyconsumer.utils.tag as LogTag

class CalculationsFragment : BaseLauncherFragment() {

    private val calculationsOrchestrator: CalculationsOrchestrator by inject()

    private lateinit var fragmentCalculationsLauncherBinding: FragmentCalculationsLauncherBinding
    override val activityTitle: String = "Calculations launcher"

    private var calculationsService: CalculationsService? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentCalculationsLauncherBinding = FragmentCalculationsLauncherBinding.inflate(inflater, container, false)
        return fragmentCalculationsLauncherBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonsListeners()
        bindToCalculationsService()
        observeCalculationsResult()
    }

    private fun observeCalculationsResult() {
        lifecycleScope.launchWhenResumed {
            calculationsOrchestrator.observeCalculationsResult().collect { onCalculationsResultChanged(it) }
        }
    }

    private fun onCalculationsResultChanged(result: CalculationsResult) {
        val labelString = "handlerId: ${result.handlerId}, variable = ${result.variable}"
        fragmentCalculationsLauncherBinding.calculationsResultValueLabel.text = labelString
    }

    private fun setButtonsListeners() {
        fragmentCalculationsLauncherBinding.apply {
            performAdditionsButton.setOnClickListener { startCalculationsService(CalculationsType.ADDITION) }
            performMultiplicationsButton.setOnClickListener {
                startCalculationsService(CalculationsType.MULTIPLICATION)
            }
            abortCalculationsButton.setOnClickListener {
                abortCalculationsService()
                clearInfoLabels()
            }
        }
    }

    private fun startCalculationsService(type: CalculationsType) {
        val amountOfHandlers = getAmountOfHandlersFromInput()
        val factor = getFactorFromInput()
        bindToCalculationsService()
        context?.let { context -> CalculationsService.startCalculations(context, type, amountOfHandlers, factor) }
    }

    private fun bindToCalculationsService() {
        context?.let { context ->
            val serviceIntent = CalculationsService.getServiceIntent(context)
            context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun abortCalculationsService() {
        unbindFromService()
        context?.let { context -> CalculationsService.stopCalculations(context) }
    }

    private fun clearInfoLabels() {
        with(fragmentCalculationsLauncherBinding) {
            calculationsResultValueLabel.text = ""
            calculationsErrorMessageLabel.text = ""
        }
    }

    private fun getAmountOfHandlersFromInput() = try {
        fragmentCalculationsLauncherBinding.calculationsHandlersNOInput.text.toString().toInt().apply {
            if (this > 0) return this
            else DEFAULT_NUMBER_OF_HANDLERS
        }
    } catch (nfe: NumberFormatException) {
        Log.e(tag, "Exception when converting input string to int", nfe)
        DEFAULT_NUMBER_OF_HANDLERS
    }

    private fun getFactorFromInput() = try {
        fragmentCalculationsLauncherBinding.calculationsFactorInput.text.toString().toInt()
    } catch (nfe: NumberFormatException) {
        Log.e(tag, "Exception when converting input string to int", nfe)
        DEFAULT_CALCULATIONS_FACTOR
    }

    override val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, iBinder: IBinder?) {
            Log.d(LogTag, "onServiceConnected")
            val binder = iBinder as MyBinder?
            (binder?.getService() as CalculationsService?)?.let { service ->
                calculationsService = service
                serviceBound.set(true)
                with(lifecycleScope) {
                    launchWhenResumed { service.observeServiceRunning().collect { onCalculationsStatusChanged(it) } }
                    launchWhenResumed { service.observeErrorMessage().collect { onErrorMessageChanged(it) } }
                }
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            Log.d(LogTag, "onServiceDisconnected")
            serviceBound.set(false)
            calculationsService = null
        }

        override fun onBindingDied(componentName: ComponentName?) {
            Log.d(LogTag, "onBindingDied")
            serviceBound.set(false)
            calculationsService = null
            super.onBindingDied(componentName)
        }

        override fun onNullBinding(componentName: ComponentName?) {
            Log.d(LogTag, "onNullBinding")
            super.onNullBinding(componentName)
        }
    }

    private fun onCalculationsStatusChanged(calculationsStatus: Boolean) {
        fragmentCalculationsLauncherBinding.apply {
            if (calculationsStatus) {
                calculationsStatusValueLabel.text = getString(R.string.running)
                calculationsStatusValueLabel.setTextColor(Color.RED)
            } else {
                calculationsStatusValueLabel.text = getString(R.string.not_running)
                calculationsStatusValueLabel.setTextColor(Color.GREEN)
            }
        }
    }

    private fun onErrorMessageChanged(errorMessage: String?) {
        fragmentCalculationsLauncherBinding.calculationsErrorMessageLabel.text = errorMessage ?: ""
    }
}