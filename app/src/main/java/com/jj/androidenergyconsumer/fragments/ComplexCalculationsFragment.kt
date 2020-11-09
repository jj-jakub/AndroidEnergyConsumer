package com.jj.androidenergyconsumer.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.services.CalculationsService
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
    }

    private fun startCalculationsService() {
        context?.let { context ->
            CalculationsService.startCalculations(context)
        }
    }

    private fun abortCalculationsService() {
        context?.let { context ->
            CalculationsService.stopCalculations(context)
        }
    }
}