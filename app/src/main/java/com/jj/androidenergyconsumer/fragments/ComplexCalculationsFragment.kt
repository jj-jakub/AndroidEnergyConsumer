package com.jj.androidenergyconsumer.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.services.CalculationsService
import com.jj.androidenergyconsumer.services.CalculationsService.Companion.DEFAULT_NUMBER_OF_HANDLERS
import kotlinx.android.synthetic.main.fragment_complex_calculations.*

class ComplexCalculationsFragment : Fragment() {

    companion object {
        fun newInstance(): ComplexCalculationsFragment = ComplexCalculationsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_complex_calculations, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonsListeners()
    }

    private fun setButtonsListeners() {
        calculationsOneButton?.setOnClickListener { startCalculationsService() }
        abortCalculationsOneButton?.setOnClickListener { abortCalculationsService() }
        calculationsTwoButton?.setOnClickListener { startCalculationsService(getAmountOfHandlersFromInput()) }
        abortCalculationsTwoButton?.setOnClickListener { abortCalculationsService() }
    }

    private fun startCalculationsService(amountOfHandlers: Int = DEFAULT_NUMBER_OF_HANDLERS) {
        context?.let { context ->
            CalculationsService.startCalculations(context, amountOfHandlers)
        }
    }

    private fun abortCalculationsService() {
        context?.let { context ->
            CalculationsService.stopCalculations(context)
        }
    }

    private fun getAmountOfHandlersFromInput() = try {
        calculationsVariableInput.text.toString().toInt().apply {
            if (this > 0) {
                return this
            } else {
                DEFAULT_NUMBER_OF_HANDLERS
            }
        }
    } catch (nfe: NumberFormatException) {
        Log.e(tag, "Exception when converting input string to int", nfe)
        DEFAULT_NUMBER_OF_HANDLERS
    }
}