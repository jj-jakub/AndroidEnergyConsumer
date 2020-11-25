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
import androidx.fragment.app.Fragment
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.calculations.CalculationsType
import com.jj.androidenergyconsumer.services.CalculationsService
import com.jj.androidenergyconsumer.services.CalculationsService.Companion.DEFAULT_CALCULATIONS_FACTOR
import com.jj.androidenergyconsumer.services.CalculationsService.Companion.DEFAULT_NUMBER_OF_HANDLERS
import com.jj.androidenergyconsumer.services.MyBinder
import kotlinx.android.synthetic.main.fragment_calculations_launcher.*
import java.util.concurrent.atomic.AtomicBoolean

class CalculationsFragment : Fragment() {

    companion object {
        fun newInstance(): CalculationsFragment = CalculationsFragment()
    }

    private var calculationsService: CalculationsService? = null
    private var serviceBound = AtomicBoolean(false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_calculations_launcher, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonsListeners()
        context?.let { context -> bindToCalculationsService(context) }
    }

    private fun setButtonsListeners() {
        performAdditionsButton?.setOnClickListener { startCalculationsService(CalculationsType.ADDITION) }
        performMultiplicationsButton?.setOnClickListener {
            startCalculationsService(CalculationsType.MULTIPLICATION)
        }
        abortCalculationsButton?.setOnClickListener { abortCalculationsService() }
    }

    private fun startCalculationsService(type: CalculationsType) {
        val amountOfHandlers = getAmountOfHandlersFromInput()
        val factor = getFactorFromInput()
        context?.let { context ->
            bindToCalculationsService(context)
            CalculationsService.startCalculations(context, type, amountOfHandlers, factor)
        }
    }

    private fun bindToCalculationsService(context: Context) {
        val serviceIntent = CalculationsService.getServiceIntent(context)
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun abortCalculationsService() {
        context?.let { context ->
            if (serviceBound.compareAndSet(true, false)) {
                context.unbindService(serviceConnection)
            }
            CalculationsService.stopCalculations(context)
        }
    }

    private fun getAmountOfHandlersFromInput() = try {
        calculationsHandlersNOInput.text.toString().toInt().apply {
            if (this > 0) return this
            else DEFAULT_NUMBER_OF_HANDLERS
        }
    } catch (nfe: NumberFormatException) {
        Log.e(tag, "Exception when converting input string to int", nfe)
        DEFAULT_NUMBER_OF_HANDLERS
    }

    private fun getFactorFromInput() = try {
        calculationsFactorInput.text.toString().toInt()
    } catch (nfe: NumberFormatException) {
        Log.e(tag, "Exception when converting input string to int", nfe)
        DEFAULT_CALCULATIONS_FACTOR
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, iBinder: IBinder?) {
            Log.d(tag, "onServiceConnected")
            val binder = iBinder as MyBinder?
            calculationsService = (binder?.getService() as CalculationsService?)
            serviceBound.set(true)
            calculationsService?.areCalculationsRunning?.observe(this@CalculationsFragment, {
                onCalculationsStatusChanged(it)
            })
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            Log.d(tag, "onServiceDisconnected")
            serviceBound.set(false)
            calculationsService = null
        }

        override fun onBindingDied(componentName: ComponentName?) {
            Log.d(tag, "onBindingDied")
            serviceBound.set(false)
            calculationsService = null
            super.onBindingDied(componentName)
        }

        override fun onNullBinding(componentName: ComponentName?) {
            Log.d(tag, "onNullBinding")
            super.onNullBinding(componentName)
        }
    }

    private fun onCalculationsStatusChanged(calculationsStatus: Boolean) {
        if (calculationsStatus) {
            calculationsStatusValueLabel.text = getString(R.string.running)
            calculationsStatusValueLabel.setTextColor(Color.RED)
        } else {
            calculationsStatusValueLabel.text = getString(R.string.not_running)
            calculationsStatusValueLabel.setTextColor(Color.GREEN)
        }
    }
}