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
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.jj.androidenergyconsumer.R
import com.jj.androidenergyconsumer.services.InternetService
import com.jj.androidenergyconsumer.services.MyBinder
import com.jj.androidenergyconsumer.utils.getDateStringWithMillis
import kotlinx.android.synthetic.main.fragment_internet_launcher.*
import java.util.concurrent.atomic.AtomicBoolean

class InternetLauncherFragment : Fragment() {

    companion object {
        fun newInstance(): InternetLauncherFragment = InternetLauncherFragment()
    }

    private var internetService: InternetService? = null
    private var serviceBound = AtomicBoolean(false)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_internet_launcher, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setButtonsListeners()
        context?.apply { bindToInternetService(this) }
    }

    override fun onDestroy() {
        super.onDestroy()
        context?.apply { unbindFromService(this) }
    }

    private fun setButtonsListeners() {
        periodicInternetWorkButton?.setOnClickListener { startPeriodicInternetWork() }
        constantInternetWorkButton?.setOnClickListener { startConstantInternetWork() }
        stopInternetCallsButton?.setOnClickListener { stopInternetWork() }
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

    private fun getMillisFromInput(): Long =
        try {
            internetIntervalInput.text.toString().toLong()
        } catch (e: Exception) {
            Log.e(tag, "Exception while converting input interval", e)
            0
        }

    private fun getUrlFromInput(): String =
        try {
            urlInput.text.toString()
        } catch (e: Exception) {
            Log.e(tag, "Exception while converting input url", e)
            onWrongUrlInput()
            ""
        }

    private fun onWrongUrlInput() {
        urlToPingLabel.text = getString(R.string.url_conversion_error)
    }

    private fun bindToInternetService(context: Context) {
        val serviceIntent = InternetService.getServiceIntent(context)
        context.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun unbindFromService(context: Context) {
        if (serviceBound.compareAndSet(true, false)) {
            context.unbindService(serviceConnection)
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName?, iBinder: IBinder?) {
            Log.d(tag, "onServiceConnected")
            val binder = iBinder as MyBinder?
            internetService = (binder?.getService() as InternetService?)
            serviceBound.set(true)
            internetService?.isWorking?.observe(this@InternetLauncherFragment, {
                onScanningStatusChanged(it)
            })
            internetService?.inputErrorMessage?.observe(this@InternetLauncherFragment, {
                onErrorMessageChanged(it)
            })
            internetService?.callResponse?.observe(this@InternetLauncherFragment, {
                onCallResponseChanged(it)
            })
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
            urlToPingLabel.text = errorMessage
            urlToPingLabel.setTextColor(Color.RED)
        }
        else resetUrlLabelText()
    }

    private fun resetUrlLabelText() {
        urlToPingLabel.text = getString(R.string.url_to_ping)
        urlToPingLabel.setTextColor(Color.GRAY)
    }

    private fun onCallResponseChanged(callResponse: String?) {
        if (callResponse != null) {
            callResponseInfoLabel.visibility = View.VISIBLE
            val responseText = "${getDateStringWithMillis()}; $callResponse"
            callResponseInfoValue.text = responseText
        }
        else resetCallResponseLabelAndValue()
    }

    private fun resetCallResponseLabelAndValue() {
        callResponseInfoLabel.visibility = View.INVISIBLE
        callResponseInfoValue.text = ""
    }

    private fun onScanningStatusChanged(scanningStatus: Boolean?) {
        if (scanningStatus == true) {
            internetWorkingStatusValue.text = getString(R.string.running)
            internetWorkingStatusValue.setTextColor(Color.RED)
        } else {
            internetWorkingStatusValue.text = getString(R.string.not_running)
            internetWorkingStatusValue.setTextColor(Color.GREEN)
        }
    }
}