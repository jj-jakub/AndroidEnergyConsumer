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
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.databinding.FragmentInternetLauncherBinding
import com.jj.androidenergyconsumer.services.InternetService
import com.jj.androidenergyconsumer.services.MyBinder
import com.jj.androidenergyconsumer.utils.getDateStringWithMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class InternetLauncherFragment : BaseLauncherFragment() {

    private lateinit var fragmentInternetLauncherBinding: FragmentInternetLauncherBinding

    private var internetService: InternetService? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentInternetLauncherBinding = FragmentInternetLauncherBinding.inflate(inflater, container, false)
        return fragmentInternetLauncherBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonsListeners()
        context?.apply { bindToInternetService(this) }
    }

    private fun setButtonsListeners() {
        fragmentInternetLauncherBinding.apply {
            periodicInternetWorkButton.setOnClickListener { startPeriodicInternetWork() }
            constantInternetWorkButton.setOnClickListener { startConstantInternetWork() }
            stopInternetCallsButton.setOnClickListener { stopInternetWork() }
            startFileDownloadButton.setOnClickListener { startFileDownload() }
        }
    }

    private fun startPeriodicInternetWork() {
        context?.let { context ->
            resetUrlLabelText()
            val millisIntervalFromInput = getMillisFromInput()
            val urlToPing = getUrlFromInput()
            bindToInternetService(context)
            InternetService.startPeriodicPings(context, millisIntervalFromInput, urlToPing)
        }
    }

    private fun startConstantInternetWork() {
        context?.let { context ->
            resetUrlLabelText()
            val urlToPing = getUrlFromInput()
            bindToInternetService(context)
            InternetService.startOneAfterAnotherPings(context, urlToPing)
        }
    }

    private fun stopInternetWork() {
        context?.let { context ->
            unbindFromService(context)
            InternetService.stopInternetService(context)
        }
    }

    private fun startFileDownload() {
        context?.let { context ->
            resetUrlLabelText()
            val urlToDownloadFrom = getUrlFromInput()
            bindToInternetService(context)
            InternetService.startFileDownload(context, urlToDownloadFrom)
        }
    }

    private fun getMillisFromInput(): Long =
        try {
            fragmentInternetLauncherBinding.internetIntervalInput.text.toString().toLong()
        } catch (e: Exception) {
            Log.e(tag, "Exception while converting input interval", e)
            0
        }

    private fun getUrlFromInput(): String =
        try {
            fragmentInternetLauncherBinding.urlInput.text.toString()
        } catch (e: Exception) {
            Log.e(tag, "Exception while converting input url", e)
            onWrongUrlInput()
            ""
        }

    private fun onWrongUrlInput() {
        fragmentInternetLauncherBinding.urlToPingOrDownloadLabel.text = getString(R.string.url_conversion_error)
    }

    private fun bindToInternetService(context: Context) {
        val serviceIntent = InternetService.getServiceIntent(context)
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, iBinder: IBinder?) {
            Log.d(tag, "onServiceConnected")
            val binder = iBinder as MyBinder?
            (binder?.getService() as InternetService?)?.let { service ->
                internetService = service
                serviceBound.set(true)
                CoroutineScope(Dispatchers.IO).launch {
                    service.observeIsWorking().collect { onScanningStatusChanged(it) }
                    service.observeInputErrorMessage().collect { onErrorMessageChanged(it) }
                    service.observeCallResponse().collect { onCallResponseChanged(it) }
                }
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            Log.d(tag, "onServiceDisconnected")
            serviceBound.set(false)
            internetService = null
        }

        override fun onBindingDied(componentName: ComponentName?) {
            Log.d(tag, "onBindingDied")
            serviceBound.set(false)
            internetService = null
            super.onBindingDied(componentName)
        }

        override fun onNullBinding(componentName: ComponentName?) {
            Log.d(tag, "onNullBinding")
            super.onNullBinding(componentName)
        }
    }

    private fun onErrorMessageChanged(errorMessage: String?) {
        if (errorMessage != null) {
            fragmentInternetLauncherBinding.apply {
                urlToPingOrDownloadLabel.text = errorMessage
                urlToPingOrDownloadLabel.setTextColor(Color.RED)
            }
        } else resetUrlLabelText()
    }

    private fun resetUrlLabelText() {
        fragmentInternetLauncherBinding.apply {
            urlToPingOrDownloadLabel.text = getString(R.string.url_to_ping_or_download)
            urlToPingOrDownloadLabel.setTextColor(Color.GRAY)
        }
    }

    private fun onCallResponseChanged(callResponse: String?) {
        if (callResponse != null) {
            fragmentInternetLauncherBinding.callResponseInfoLabel.visibility = View.VISIBLE
            val responseText = "${getDateStringWithMillis()}; $callResponse"
            fragmentInternetLauncherBinding.callResponseInfoValue.text = responseText
        } else resetCallResponseLabelAndValue()
    }

    private fun resetCallResponseLabelAndValue() {
        fragmentInternetLauncherBinding.apply {
            callResponseInfoLabel.visibility = View.INVISIBLE
            callResponseInfoValue.text = ""
        }
    }

    private fun onScanningStatusChanged(scanningStatus: Boolean?) {
        fragmentInternetLauncherBinding.apply {
            if (scanningStatus == true) {
                internetWorkingStatusValue.text = getString(R.string.running)
                internetWorkingStatusValue.setTextColor(Color.RED)
            } else {
                internetWorkingStatusValue.text = getString(R.string.not_running)
                internetWorkingStatusValue.setTextColor(Color.GREEN)
            }
        }
    }
}