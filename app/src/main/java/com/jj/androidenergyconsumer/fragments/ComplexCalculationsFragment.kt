package com.jj.androidenergyconsumer.fragments

import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jj.androidenergyconsumer.R
import kotlinx.android.synthetic.main.fragment_complex_calculations.*

class ComplexCalculationsFragment : Fragment() {

    companion object {
        fun newInstance(): ComplexCalculationsFragment = ComplexCalculationsFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_complex_calculations, container, false)
}