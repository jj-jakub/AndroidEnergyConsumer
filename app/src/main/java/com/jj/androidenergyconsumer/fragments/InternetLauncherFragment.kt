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
import com.jj.androidenergyconsumer.databinding.FragmentInternetLauncherBinding
import com.jj.androidenergyconsumer.internet.DownloadProgress
import com.jj.androidenergyconsumer.internet.FileDownloader
import com.jj.androidenergyconsumer.internet.InternetPingsCreator
import com.jj.androidenergyconsumer.internet.InternetPingsCreator.Companion.GOOGLE_URL
import com.jj.androidenergyconsumer.services.InternetService
import com.jj.androidenergyconsumer.services.MyBinder
import com.jj.androidenergyconsumer.utils.roundAsString
import kotlinx.coroutines.flow.collect
import org.koin.android.ext.android.inject
import com.jj.androidenergyconsumer.utils.tag as LogTag

class InternetLauncherFragment : BaseLauncherFragment() {

    private val fileDownloader: FileDownloader by inject()
    private val internetPingsCreator: InternetPingsCreator by inject()

    private lateinit var fragmentInternetLauncherBinding: FragmentInternetLauncherBinding
    override val activityTitle: String = "Internet launcher"

    private var internetService: InternetService? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        fragmentInternetLauncherBinding = FragmentInternetLauncherBinding.inflate(inflater, container, false)
        return fragmentInternetLauncherBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupFragment()
        context?.apply { bindToInternetService(this) }
    }

    private fun setupFragment() {
        setButtonsListeners()
        observeInternetResultFlows()
    }

    private fun observeInternetResultFlows() {
        with(lifecycleScope) {
            launchWhenResumed { fileDownloader.observeDownloadProgress().collect { handleDownloadProgressInfo(it) } }
            launchWhenResumed {
                internetPingsCreator.observeLastCallResult().collect { onCallResponseChanged(it.result) }
            }
        }
    }

    private fun handleDownloadProgressInfo(downloadProgress: DownloadProgress) {
        val processedMessage = "${downloadProgress.progressPercentage}% downloaded, " +
                "${downloadProgress.averageDownloadSpeedKBs.roundAsString()} KB/s"
        onCallResponseChanged(processedMessage)
    }

    private fun setButtonsListeners() {
        fragmentInternetLauncherBinding.apply {
            periodicInternetWorkButton.setOnClickListener { startPeriodicInternetWork() }
            constantInternetWorkButton.setOnClickListener { startConstantInternetWork() }
            stopInternetCallsButton.setOnClickListener { stopInternetWork() }
            startFileDownloadButton.setOnClickListener { startFileDownload() }
            setGoogleUrlButton.setOnClickListener { setGoogleUrlInInput() }
        }
    }

    private fun setGoogleUrlInInput() {
        fragmentInternetLauncherBinding.urlInput.setText(GOOGLE_URL)
    }

    private fun startPeriodicInternetWork() {
        context?.let { context ->
            val millisIntervalFromInput = getMillisFromInput()
            val urlToPing = getUrlFromInput()
            bindToInternetService(context)
            InternetService.startPeriodicPings(context, millisIntervalFromInput, urlToPing)
        }
    }

    private fun startConstantInternetWork() {
        context?.let { context ->
            val urlToPing = getUrlFromInput()
            bindToInternetService(context)
            InternetService.startOneAfterAnotherPings(context, urlToPing)
        }
    }

    private fun stopInternetWork() {
        context?.let { context ->
            unbindFromService(context)
            InternetService.stopInternetService(context)
            resetValues()
        }
    }

    private fun startFileDownload() {
        context?.let { context ->
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
            Log.d(LogTag, "onServiceConnected")
            val binder = iBinder as MyBinder?
            (binder?.getService() as InternetService?)?.let { service ->
                internetService = service
                serviceBound.set(true)
                with(lifecycleScope) {
                    launchWhenResumed { service.observeIsWorking().collect { onWorkingStatusChanged(it) } }
                    launchWhenResumed { service.observeInputErrorMessage().collect { onErrorMessageChanged(it) } }
                }
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName?) {
            Log.d(LogTag, "onServiceDisconnected")
            serviceBound.set(false)
            internetService = null
        }

        override fun onBindingDied(componentName: ComponentName?) {
            Log.d(LogTag, "onBindingDied")
            serviceBound.set(false)
            internetService = null
            super.onBindingDied(componentName)
        }

        override fun onNullBinding(componentName: ComponentName?) {
            Log.d(LogTag, "onNullBinding")
            super.onNullBinding(componentName)
        }
    }

    private fun onErrorMessageChanged(errorMessage: String?) {
        fragmentInternetLauncherBinding.apply {
            urlToPingOrDownloadLabel.text = errorMessage
            urlToPingOrDownloadLabel.setTextColor(Color.RED)
        }
    }

    private fun onCallResponseChanged(callResponse: String) {
        fragmentInternetLauncherBinding.callResponseInfoLabel.visibility = View.VISIBLE
        fragmentInternetLauncherBinding.callResponseInfoValue.text = callResponse
    }

    private fun resetCallResponseLabelAndValue() {
        fragmentInternetLauncherBinding.apply {
            callResponseInfoLabel.visibility = View.INVISIBLE
            callResponseInfoValue.text = ""
        }
    }

    private fun resetUrlLabelText() {
        fragmentInternetLauncherBinding.apply {
            urlToPingOrDownloadLabel.text = getString(R.string.url_to_ping_or_download)
            urlToPingOrDownloadLabel.setTextColor(Color.GRAY)
        }
    }

    private fun resetValues() {
        resetCallResponseLabelAndValue()
        resetUrlLabelText()
    }

    private fun onWorkingStatusChanged(workingStatus: Boolean?) {
        fragmentInternetLauncherBinding.apply {
            if (workingStatus == true) {
                internetWorkingStatusValue.text = getString(R.string.running)
                internetWorkingStatusValue.setTextColor(Color.RED)
            } else {
                internetWorkingStatusValue.text = getString(R.string.not_running)
                internetWorkingStatusValue.setTextColor(Color.GREEN)
            }
        }
    }
}