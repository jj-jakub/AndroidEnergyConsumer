package com.jj.androidenergyconsumer.fragments

import android.os.Bundle
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
import kotlinx.android.synthetic.main.fragment_calculations_launcher.*

class CalculationsFragment : Fragment() {

    companion object {
        fun newInstance(): CalculationsFragment = CalculationsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_calculations_launcher, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonsListeners()
    }

    private fun setButtonsListeners() {
        performAdditionsButton?.setOnClickListener { startCalculationsService(CalculationsType.ADDITION) }
        performMultiplicationsButton?.setOnClickListener { startCalculationsService(CalculationsType.MULTIPLICATION) }
        abortCalculationsButton?.setOnClickListener { abortCalculationsService() }
    }

    private fun startCalculationsService(type: CalculationsType) {
        val amountOfHandlers = getAmountOfHandlersFromInput()
        val factor = getFactorFromInput()
        context?.let { context -> CalculationsService.startCalculations(context, type, amountOfHandlers, factor) }
    }

    private fun abortCalculationsService() {
        context?.let { context -> CalculationsService.stopCalculations(context) }
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
}