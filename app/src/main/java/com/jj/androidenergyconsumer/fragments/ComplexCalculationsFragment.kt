package com.jj.androidenergyconsumer.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.services.CalculationsService
import com.jj.androidenergyconsumer.utils.startService
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
        calculationsOneButton?.setOnClickListener {
            context?.let { context ->
                startService(context, Intent(context, CalculationsService::class.java))
            }
        }
    }
}